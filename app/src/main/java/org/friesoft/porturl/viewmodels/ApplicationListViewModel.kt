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
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.ApplicationCategory
import org.friesoft.porturl.data.model.Category
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
 *
 * @param allItems The single source of truth for the dashboard layout. A flat list
 * containing all categories and applications in their correct display order.
 * @param searchQuery The current text entered by the user in the search field.
 * @param isLoading True when performing the initial data load.
 * @param isRefreshing True when performing a pull-to-refresh action.
 * @param error A message describing the last error that occurred, if any.
 */
data class ApplicationListState(
    val allItems: List<DashboardItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /**
     * A computed property that transforms the flat `allItems` list into a structured map,
     * suitable for rendering a multi-column UI where each category is a column.
     * It also incorporates the search logic.
     */
    val groupedDashboardItems: Map<Category, List<Application>>
        get() {
            val groupedMap = mutableMapOf<Category, MutableList<Application>>()
            var currentCategory: Category? = null

            // First, populate the map with all items in their original order.
            allItems.forEach { item ->
                when (item) {
                    is DashboardItem.CategoryItem -> {
                        currentCategory = item.category
                        groupedMap.getOrPut(currentCategory!!) { mutableListOf() }
                    }
                    is DashboardItem.ApplicationItem -> {
                        currentCategory?.let {
                            groupedMap.getOrPut(it) { mutableListOf() }.add(item.application)
                        }
                    }
                }
            }

            // If there's no search query, return the fully populated map.
            if (searchQuery.isBlank()) {
                return groupedMap
            }

            // If there is a search query, filter the map's contents.
            val lowerCaseQuery = searchQuery.lowercase()
            val filteredMap = mutableMapOf<Category, List<Application>>()

            groupedMap.forEach { (category, applications) ->
                val categoryNameMatches = category.name.lowercase().contains(lowerCaseQuery)
                val matchingApps = applications.filter { app ->
                    app.name.lowercase().contains(lowerCaseQuery) || app.url.lowercase().contains(lowerCaseQuery)
                }

                // A category column should be visible if its name matches OR it has apps that match.
                if (categoryNameMatches || matchingApps.isNotEmpty()) {
                    // If the category name matches, show all its apps. Otherwise, show only the matching apps.
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationListState())
    val uiState = _uiState.asStateFlow()

    private var persistenceJob: Job? = null
    private val debounceTime = 1000L // 1 second debounce for persistence

    init {
        loadData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                // Ensure any pending save operation completes before refreshing.
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
        val applications = applicationRepository.getAllApplications()
        val categories = categoryRepository.getAllCategories()
        _uiState.update {
            it.copy(allItems = buildDashboardItems(applications, categories))
        }
    }

    /**
     * Builds the flat list of dashboard items from the raw data sources.
     * This method correctly handles apps belonging to multiple categories.
     */
    private fun buildDashboardItems(applications: List<Application>, categories: List<Category>): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val appsByCategoryId = mutableMapOf<Long, MutableList<Application>>()

        // Efficiently populate the map of which apps belong to which category.
        applications.forEach { app ->
            app.applicationCategories.forEach { appCategory ->
                appCategory.category?.id?.let { catId ->
                    appsByCategoryId.getOrPut(catId) { mutableListOf() }.add(app)
                }
            }
        }

        // Build the final flat list, respecting both category and application sort orders.
        categories.sortedBy { it.sortOrder }.forEach { category ->
            dashboardItems.add(DashboardItem.CategoryItem(category))
            val appsForCategory = appsByCategoryId[category.id]
                ?.sortedBy { app ->
                    app.applicationCategories.find { it.category?.id == category.id }?.sortOrder ?: Int.MAX_VALUE
                }
            appsForCategory?.forEach { app ->
                dashboardItems.add(DashboardItem.ApplicationItem(app, category.id))
            }
        }
        return dashboardItems
    }

    /**
     * Handles moving an application from one category to another as a result of a drag-and-drop operation.
     */
    fun moveApplication(appId: Long, fromCatId: Long, toCatId: Long) {
        if (fromCatId == toCatId) return

        val currentItems = _uiState.value.allItems.toMutableList()

        // Find the item to move.
        val itemToMoveIndex = currentItems.indexOfFirst {
            it is DashboardItem.ApplicationItem && it.application.id == appId && it.parentCategoryId == fromCatId
        }
        if (itemToMoveIndex == -1) return

        val itemToMove = currentItems.removeAt(itemToMoveIndex) as DashboardItem.ApplicationItem
        val newItem = itemToMove.copy(parentCategoryId = toCatId)

        // Find the end of the target category block.
        val targetCategoryIndex = currentItems.indexOfFirst {
            it is DashboardItem.CategoryItem && it.category.id == toCatId
        }
        if (targetCategoryIndex == -1) return

        var targetInsertionIndex = targetCategoryIndex + 1
        while (targetInsertionIndex < currentItems.size && currentItems[targetInsertionIndex] is DashboardItem.ApplicationItem) {
            targetInsertionIndex++
        }

        // Insert the item.
        currentItems.add(targetInsertionIndex, newItem)

        // Clean up duplicates if the app now exists twice visually under the same category.
        val stationaryItem = currentItems.find {
            it is DashboardItem.ApplicationItem && it.application.id == appId && it.parentCategoryId == toCatId && it !== newItem
        }
        stationaryItem?.let { currentItems.remove(it) }

        _uiState.update { it.copy(allItems = currentItems) }
        debouncedPersist(currentItems)
    }


    enum class MoveDirection { UP, DOWN }

    fun moveCategory(categoryId: Long, direction: MoveDirection) {
        val currentItems = _uiState.value.allItems.toMutableList()

        // Find the start and end index of the category block (header + apps).
        val categoryIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == categoryId }
        if (categoryIndex == -1) return
        var endIndex = categoryIndex + 1
        while (endIndex < currentItems.size && currentItems[endIndex] is DashboardItem.ApplicationItem) {
            endIndex++
        }
        val groupToMove = currentItems.subList(categoryIndex, endIndex).toList()

        // Determine the target index to move the block to.
        val targetIndex = when (direction) {
            MoveDirection.UP -> {
                if (categoryIndex == 0) return
                val prevItem = currentItems[categoryIndex - 1]
                currentItems.indexOfFirst {
                    it is DashboardItem.CategoryItem && it.category.id == (prevItem as? DashboardItem.ApplicationItem)?.parentCategoryId
                }.takeIf { it != -1 } ?: (categoryIndex - 1)
            }
            MoveDirection.DOWN -> {
                if (endIndex >= currentItems.size) return
                endIndex
            }
        }

        // Perform the move.
        currentItems.removeAll(groupToMove)
        currentItems.addAll(targetIndex.coerceAtMost(currentItems.size), groupToMove)

        _uiState.update { it.copy(allItems = currentItems) }
        debouncedPersist(currentItems)
    }

    private fun debouncedPersist(items: List<DashboardItem>) {
        persistenceJob?.cancel()
        persistenceJob = viewModelScope.launch {
            delay(debounceTime)
            persistDashboardOrder(items)
        }
    }

    /**
     * Persists the new order of categories and applications to the repository.
     * This function correctly handles the many-to-many relationship of applications to categories.
     */
    private suspend fun persistDashboardOrder(items: List<DashboardItem>) {
        val categoriesToUpdate = mutableListOf<Category>()
        val applicationsToUpdate = mutableMapOf<Long, Application>()
        var currentCategory: Category? = null
        var categoryOrder = 0
        var appOrder = 0

        items.forEach { item ->
            when (item) {
                is DashboardItem.CategoryItem -> {
                    currentCategory = item.category
                    appOrder = 0 // Reset app order for the new category.
                    if (item.category.sortOrder != categoryOrder) {
                        categoriesToUpdate.add(item.category.copy(sortOrder = categoryOrder))
                    }
                    categoryOrder++
                }
                is DashboardItem.ApplicationItem -> {
                    val app = applicationsToUpdate[item.application.id] ?: item.application
                    val updatedLinks = app.applicationCategories.toMutableList()
                    var needsUpdate = false

                    val newParentCategory = currentCategory!!

                    // Find the link for the app within its *new* visual category.
                    val linkIndex = updatedLinks.indexOfFirst { it.category?.id == newParentCategory.id }

                    if (linkIndex != -1) {
                        // Case 1: The app is already in this category.
                        // We only need to check if its sort order has changed.
                        if (updatedLinks[linkIndex].sortOrder != appOrder) {
                            updatedLinks[linkIndex] = updatedLinks[linkIndex].copy(sortOrder = appOrder)
                            needsUpdate = true
                        }
                    } else {
                        // Case 2: The app was dragged into a new category it wasn't in before.
                        // Add a new relationship link. We DO NOT remove old ones.
                        updatedLinks.add(ApplicationCategory(category = newParentCategory, sortOrder = appOrder))
                        needsUpdate = true
                    }

                    if (needsUpdate) {
                        applicationsToUpdate[app.id!!] = app.copy(applicationCategories = updatedLinks)
                    }
                    appOrder++
                }
            }
        }

        try {
            if (categoriesToUpdate.isNotEmpty()) categoryRepository.reorderCategories(categoriesToUpdate)
            if (applicationsToUpdate.isNotEmpty()) applicationRepository.reorderApplications(applicationsToUpdate.values.toList())
        } catch (e: Exception) {
            Log.e("AppListViewModel", "Failed to persist order", e)
            _uiState.update { it.copy(error = "Failed to save new order.") }
        }
    }

    fun sortAppsAlphabetically(categoryId: Long) {
        val currentItems = _uiState.value.allItems.toMutableList()
        val categoryIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == categoryId }
        if (categoryIndex == -1) return

        // Isolate the apps for the target category.
        val appsForCategory = currentItems
            .drop(categoryIndex + 1)
            .takeWhile { it is DashboardItem.ApplicationItem }
            .map { it as DashboardItem.ApplicationItem }

        if (appsForCategory.isEmpty()) return

        // Sort them alphabetically and create updated ApplicationItem models.
        val sortedAppItems = appsForCategory
            .sortedBy { it.application.name }
            .mapIndexed { index, item -> item.copy(
                application = item.application.copy(
                    applicationCategories = item.application.applicationCategories.map { ac ->
                        if (ac.category?.id == categoryId) ac.copy(sortOrder = index) else ac
                    }
                ))
            }

        // Splice the sorted apps back into the main list.
        val newItems = currentItems.take(categoryIndex + 1) + sortedAppItems + currentItems.drop(categoryIndex + 1 + appsForCategory.size)
        _uiState.update { it.copy(allItems = newItems) }

        // Debounce the persistence of the sorted apps.
        val appsToUpdate = sortedAppItems.map { it.application }
        persistenceJob?.cancel()
        persistenceJob = viewModelScope.launch {
            delay(debounceTime)
            try {
                if (appsToUpdate.isNotEmpty()) applicationRepository.reorderApplications(appsToUpdate)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save alphabetical sort order.") }
            }
        }
    }

    fun deleteApplication(id: Long) {
        viewModelScope.launch {
            try {
                applicationRepository.deleteApplication(id)
                refreshData() // Refresh to get the updated list.
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete application.") }
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(id)
                refreshData() // Refresh to get the updated list.
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete category.") }
            }
        }
    }
}

