package org.friesoft.porturl.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.Category
import org.friesoft.porturl.client.model.CategoryReorderRequest
import org.friesoft.porturl.client.model.MoveApplicationRequest
import org.friesoft.porturl.data.auth.AuthService
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.CategoryRepository
import javax.inject.Inject

/**
 * Represents a distinct item on the dashboard UI. It can be a category header
 * or an application tile. The 'key' is used for stable identification in lazy layouts.
 */
sealed class DashboardItem {
    abstract val key: String

    data class CategoryItem(val category: Category) : DashboardItem() {
        override val key: String = "category_${category.id}"
    }

    data class ApplicationItem(val application: Application, val parentCategoryId: Long) : DashboardItem() {
        override val key: String = "app_${parentCategoryId}_${application.id}"
    }
}

/**
 * Represents the complete state of the Application List screen.
 */
data class ApplicationListState(
    val allItems: List<DashboardItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val groupedDashboardItems: Map<Category, List<Application>>
        get() {
            val groupedMap = mutableMapOf<Category, MutableList<Application>>()
            var currentCategory: Category? = null

            allItems.forEach { item ->
                when (item) {
                    is DashboardItem.CategoryItem -> {
                        currentCategory = item.category
                        groupedMap.getOrPut(currentCategory) { mutableListOf() }
                    }
                    is DashboardItem.ApplicationItem -> {
                        currentCategory?.let {
                            groupedMap.getOrPut(it) { mutableListOf() }.add(item.application)
                        }
                    }
                }
            }

            if (searchQuery.isBlank()) return groupedMap

            val lowerCaseQuery = searchQuery.lowercase()
            val filteredMap = mutableMapOf<Category, List<Application>>()

            groupedMap.forEach { (category, applications) ->
                val categoryNameMatches = category.name?.lowercase()?.contains(lowerCaseQuery) == true
                val matchingApps = applications.filter { app ->
                    categoryNameMatches || 
                    app.name?.lowercase()?.contains(lowerCaseQuery) == true || 
                    app.url?.lowercase()?.contains(lowerCaseQuery) == true
                }

                if (categoryNameMatches || matchingApps.isNotEmpty()) {
                    filteredMap[category] = matchingApps
                }
            }
            return filteredMap
        }
}

@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val categoryRepository: CategoryRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationListState())
    val uiState = _uiState.asStateFlow()

    private var persistenceJob: Job? = null
    private val debounceTime = 1000L

    private val categoryAppsMap = mutableMapOf<Long, List<Application>>()
    private var currentCategories = listOf<Category>()

    init {
        loadData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearData() {
        Log.d("AppListViewModel", "Clearing data")
        categoryAppsMap.clear()
        currentCategories = emptyList()
        _uiState.update { it.copy(allItems = emptyList(), error = null) }
    }

    fun refreshData() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                authService.forceTokenRefresh()
                persistenceJob?.join()
                loadAllItemsFromRepositories()
            } catch (e: Exception) {
                Log.e("AppListViewModel", "Failed to refresh data", e)
                _uiState.update { it.copy(error = "Failed to refresh data: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                loadAllItemsFromRepositories()
            } catch (e: Exception) {
                Log.e("AppListViewModel", "Failed to load data", e)
                _uiState.update { it.copy(error = "Failed to load data: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadAllItemsFromRepositories() {
        val categories = categoryRepository.getAllCategories()
        currentCategories = categories
        
        _uiState.update {
            it.copy(allItems = buildDashboardItems(categories, emptyMap()))
        }

        categories.forEach { category ->
            category.id?.let { id ->
                viewModelScope.launch {
                    try {
                        val apps = categoryRepository.getApplicationsByCategory(id)
                        categoryAppsMap[id] = apps
                        updateUiItems()
                    } catch (e: Exception) {
                        Log.e("AppListViewModel", "Failed to load apps for category $id", e)
                    }
                }
            }
        }
    }

    private fun updateUiItems() {
        _uiState.update {
            it.copy(allItems = buildDashboardItems(currentCategories, categoryAppsMap))
        }
    }

    private fun buildDashboardItems(
        categories: List<Category>, 
        appsMap: Map<Long, List<Application>>
    ): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        categories.sortedBy { it.sortOrder }.forEach { category ->
            dashboardItems.add(DashboardItem.CategoryItem(category))
            val apps = appsMap[category.id ?: -1L]
            apps?.forEach { app ->
                dashboardItems.add(DashboardItem.ApplicationItem(app, category.id ?: -1L))
            }
        }
        return dashboardItems
    }

    fun moveApplication(appId: Long?, fromCatId: Long, toCatId: Long, targetIndexInCat: Int) {
        if (appId == null || appId == -1L) return
        
        val currentItems = _uiState.value.allItems.toMutableList()
        val itemToMoveIndex = currentItems.indexOfFirst {
            it is DashboardItem.ApplicationItem && it.application.id == appId && it.parentCategoryId == fromCatId
        }
        if (itemToMoveIndex == -1) return
        val itemToMove = currentItems.removeAt(itemToMoveIndex) as DashboardItem.ApplicationItem

        val targetCategoryIndex = currentItems.indexOfFirst {
            it is DashboardItem.CategoryItem && it.category.id == toCatId
        }
        if (targetCategoryIndex == -1) return
        val insertionIndex = targetCategoryIndex + 1 + targetIndexInCat

        val newItem = itemToMove.copy(parentCategoryId = toCatId)
        currentItems.add(insertionIndex, newItem)

        // UI Update
        _uiState.update { it.copy(allItems = currentItems) }
        
        // Remote Update
        viewModelScope.launch {
            try {
                if (fromCatId == toCatId) {
                    // Reorder within category
                    val activeCatItems = currentItems.filter { it is DashboardItem.ApplicationItem && it.parentCategoryId == toCatId }
                        .map { (it as DashboardItem.ApplicationItem).application.id ?: -1L }
                    categoryRepository.reorderApplicationsInCategory(toCatId, activeCatItems)
                } else {
                    // Move between categories
                    applicationRepository.moveApplication(appId, MoveApplicationRequest(
                        fromCategoryId = fromCatId,
                        toCategoryId = toCatId,
                        targetIndex = targetIndexInCat
                    ))
                }
            } catch (e: Exception) {
                Log.e("AppListViewModel", "Failed to move/reorder application", e)
                refreshData() // Rollback on error
            }
        }
    }

    fun moveCategoryByDirection(categoryId: Long, direction: MoveDirection) {
        val currentItems = _uiState.value.allItems.toMutableList()
        val moveStartIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == categoryId }
        if (moveStartIndex == -1) return
        var moveEndIndex = moveStartIndex + 1
        while (moveEndIndex < currentItems.size && currentItems[moveEndIndex] is DashboardItem.ApplicationItem) {
            moveEndIndex++
        }
        val groupToMove = currentItems.subList(moveStartIndex, moveEndIndex).toList()

        when (direction) {
            MoveDirection.UP, MoveDirection.LEFT -> {
                if (moveStartIndex == 0) return
                var prevBlockStartIndex = moveStartIndex - 1
                while (prevBlockStartIndex >= 0 && currentItems[prevBlockStartIndex] is DashboardItem.ApplicationItem) {
                    prevBlockStartIndex--
                }
                if (prevBlockStartIndex < 0) return
                val prevGroup = currentItems.subList(prevBlockStartIndex, moveStartIndex).toList()
                currentItems.subList(prevBlockStartIndex, moveEndIndex).clear()
                currentItems.addAll(prevBlockStartIndex, groupToMove)
                currentItems.addAll(prevBlockStartIndex + groupToMove.size, prevGroup)
            }
            MoveDirection.DOWN, MoveDirection.RIGHT -> {
                if (moveEndIndex >= currentItems.size) return
                val nextBlockStartIndex = moveEndIndex
                var nextBlockEndIndex = nextBlockStartIndex + 1
                while (nextBlockEndIndex < currentItems.size && currentItems[nextBlockEndIndex] is DashboardItem.ApplicationItem) {
                    nextBlockEndIndex++
                }
                val nextGroup = currentItems.subList(nextBlockStartIndex, nextBlockEndIndex).toList()
                currentItems.subList(moveStartIndex, nextBlockEndIndex).clear()
                currentItems.addAll(moveStartIndex, nextGroup)
                currentItems.addAll(moveStartIndex + nextGroup.size, groupToMove)
            }
        }
        _uiState.update { it.copy(allItems = currentItems) }
        persistCategoryOrder(currentItems)
    }

    enum class MoveDirection { UP, DOWN, LEFT, RIGHT }

    fun moveCategory(fromCatId: Long, targetCategoryIndex: Int) {
        val currentItems = _uiState.value.allItems.toMutableList()
        val fromStartIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == fromCatId }
        if (fromStartIndex == -1) return
        var fromEndIndex = fromStartIndex + 1
        while (fromEndIndex < currentItems.size && currentItems[fromEndIndex] is DashboardItem.ApplicationItem) {
            fromEndIndex++
        }
        val blockToMove = currentItems.subList(fromStartIndex, fromEndIndex).toList()
        currentItems.subList(fromStartIndex, fromEndIndex).clear()

        var categoryCount = 0
        var insertAtIndex = currentItems.size
        for (i in currentItems.indices) {
            if (currentItems[i] is DashboardItem.CategoryItem) {
                if (categoryCount == targetCategoryIndex) {
                    insertAtIndex = i
                    break
                }
                categoryCount++
            }
        }
        if (insertAtIndex >= 0 && insertAtIndex <= currentItems.size) {
            currentItems.addAll(insertAtIndex, blockToMove)
            _uiState.update { it.copy(allItems = currentItems) }
            persistCategoryOrder(currentItems)
        }
    }

    private fun persistCategoryOrder(items: List<DashboardItem>) {
        val requests = mutableListOf<CategoryReorderRequest>()
        var order = 0
        items.forEach { item ->
            if (item is DashboardItem.CategoryItem) {
                item.category.id?.let { id ->
                    requests.add(CategoryReorderRequest(id, order))
                }
                order++
            }
        }
        viewModelScope.launch {
            try {
                categoryRepository.reorderCategories(requests)
            } catch (e: Exception) {
                Log.e("AppListViewModel", "Failed to persist category order", e)
            }
        }
    }

    fun deleteApplication(id: Long) {
        viewModelScope.launch {
            try {
                applicationRepository.deleteApplication(id)
                refreshData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete application.") }
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(id)
                refreshData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete category.") }
            }
        }
    }
}
