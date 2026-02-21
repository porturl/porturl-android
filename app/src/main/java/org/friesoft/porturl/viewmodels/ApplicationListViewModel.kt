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
                    app.name?.lowercase()?.contains(lowerCaseQuery) == true || app.url?.lowercase()?.contains(lowerCaseQuery) == true
                }

                if (categoryNameMatches || matchingApps.isNotEmpty()) {
                    filteredMap[category] = if (categoryNameMatches) applications else matchingApps
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

    init {
        loadData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearData() {
        Log.d("AppListViewModel", "Clearing data")
        _uiState.update { it.copy(allItems = emptyList(), error = null) }
    }

    fun refreshData() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            Log.d("AppListViewModel", "refreshData() called")
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                Log.d("AppListViewModel", "Attempting forceTokenRefresh()")
                authService.forceTokenRefresh()
                Log.d("AppListViewModel", "forceTokenRefresh() successful, loading items")
                persistenceJob?.join()
                loadAllItemsFromRepositories()
                Log.d("AppListViewModel", "Data refreshed successfully")
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
            Log.d("AppListViewModel", "loadData() called")
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                loadAllItemsFromRepositories()
                Log.d("AppListViewModel", "Data loaded successfully")
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
        _uiState.update {
            it.copy(allItems = buildDashboardItems(categories))
        }
    }

    private fun buildDashboardItems(categories: List<Category>): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        categories.sortedBy { it.sortOrder }.forEach { category ->
            dashboardItems.add(DashboardItem.CategoryItem(category))
            category.applications?.forEach { app ->
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

        // If target category was alphabetical, switch it to custom because we just performed a manual move
        val targetCatIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == toCatId }
        if (targetCatIndex != -1) {
            val catItem = currentItems[targetCatIndex] as DashboardItem.CategoryItem
            if (catItem.category.applicationSortMode == Category.ApplicationSortMode.ALPHABETICAL) {
                currentItems[targetCatIndex] = DashboardItem.CategoryItem(
                    catItem.category.copy(applicationSortMode = Category.ApplicationSortMode.CUSTOM)
                )
            }
        }

        if (fromCatId != toCatId) {
            val duplicateIndex = currentItems.indexOfFirst {
                it is DashboardItem.ApplicationItem && it.application.id == appId && it.parentCategoryId == toCatId && it !== newItem
            }
            if (duplicateIndex != -1) currentItems.removeAt(duplicateIndex)
        }

        _uiState.update { it.copy(allItems = currentItems) }
        debouncedPersist(currentItems)
    }

    enum class MoveDirection { UP, DOWN, LEFT, RIGHT }

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
        debouncedPersist(currentItems)
    }

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
            debouncedPersist(currentItems)
        }
    }

    private fun debouncedPersist(items: List<DashboardItem>) {
        persistenceJob?.cancel()
        persistenceJob = viewModelScope.launch {
            delay(debounceTime)
            persistDashboardOrder(items)
        }
    }

    private suspend fun persistDashboardOrder(items: List<DashboardItem>) {
        val categoriesToUpdate = mutableListOf<Category>()
        val categoryAppMap = mutableMapOf<Long, MutableList<Application>>()
        var currentCategory: Category? = null
        var categoryOrder = 0

        items.forEach { item ->
            when (item) {
                is DashboardItem.CategoryItem -> {
                    currentCategory = item.category
                    categoryAppMap[currentCategory.id ?: -1L] = mutableListOf()
                    categoriesToUpdate.add(item.category.copy(sortOrder = categoryOrder))
                    categoryOrder++
                }
                is DashboardItem.ApplicationItem -> {
                    currentCategory?.let { cat ->
                        categoryAppMap[cat.id ?: -1L]?.add(item.application)
                    }
                }
            }
        }

        val categoriesWithApps = categoriesToUpdate.map { cat ->
            cat.copy(applications = categoryAppMap[cat.id ?: -1L])
        }

        try {
            categoryRepository.reorderCategories(categoriesWithApps)
            applicationRepository.reorderApplications(categoriesWithApps)
        } catch (e: Exception) {
            Log.e("AppListViewModel", "Failed to persist order", e)
            _uiState.update { it.copy(error = "Failed to save new order.") }
        }
    }

    fun sortAppsAlphabetically(categoryId: Long) {
        val currentItems = _uiState.value.allItems.toMutableList()
        val categoryIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == categoryId }
        if (categoryIndex == -1) return

        val categoryItem = currentItems[categoryIndex] as DashboardItem.CategoryItem
        val updatedCategory = categoryItem.category.copy(applicationSortMode = Category.ApplicationSortMode.ALPHABETICAL)
        currentItems[categoryIndex] = DashboardItem.CategoryItem(updatedCategory)

        // Only update the mode and persist, let backend do the sorting on next load
        _uiState.update { it.copy(allItems = currentItems) }
        debouncedPersist(currentItems)
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