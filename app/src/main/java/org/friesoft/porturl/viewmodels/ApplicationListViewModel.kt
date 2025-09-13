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

sealed class DashboardItem {
    abstract val key: String

    data class CategoryItem(val category: Category) : DashboardItem() {
        override val key: String = "category_${category.id}"
    }

    data class ApplicationItem(val application: Application, val parentCategoryId: Long) : DashboardItem() {
        override val key: String = "app_${parentCategoryId}_${application.id}"
    }
}

data class ApplicationListState(
    val dashboardItems: List<DashboardItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationListState())
    val uiState = _uiState.asStateFlow()

    private var persistenceJob: Job? = null
    private val debounceTime = 1000L // 1 seconds

    // State to hold the final drop position
    private var lastToIndex: Int? = null

    init {
        loadData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                persistenceJob?.join()

                val applications = applicationRepository.getAllApplications()
                val categories = categoryRepository.getAllCategories()
                _uiState.update {
                    it.copy(
                        dashboardItems = buildDashboardItems(applications, categories),
                    )
                }
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
                val applications = applicationRepository.getAllApplications()
                val categories = categoryRepository.getAllCategories()
                _uiState.update {
                    it.copy(
                        dashboardItems = buildDashboardItems(applications, categories),
                    )
                }
            } catch (e: Exception) {
                Log.e("AppListViewModel", "Failed to load data", e)
                _uiState.update { it.copy(error = "Failed to load data: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun buildDashboardItems(applications: List<Application>, categories: List<Category>): List<DashboardItem> {
        val dashboardItems = mutableListOf<DashboardItem>()
        val categorizedAppsMap = mutableMapOf<Long, MutableList<Application>>()
        val allCategorizedAppIds = mutableSetOf<Long>()

        categories.forEach { category ->
            categorizedAppsMap[category.id] = mutableListOf()
        }

        applications.forEach { app ->
            var isCategorized = false
            app.applicationCategories.forEach { link ->
                link.category?.id?.let { categoryId ->
                    if (categorizedAppsMap.containsKey(categoryId)) {
                        categorizedAppsMap[categoryId]?.add(app)
                        isCategorized = true
                    }
                }
            }
            if (isCategorized) {
                app.id?.let { allCategorizedAppIds.add(it) }
            }
        }

        categories.sortedBy { it.sortOrder }.forEach { category ->
            dashboardItems.add(DashboardItem.CategoryItem(category))
            val appsForCategory = categorizedAppsMap[category.id]
            if (appsForCategory != null) {
                val sortedApps = appsForCategory.sortedBy { app ->
                    app.applicationCategories.find { it.category?.id == category.id }?.sortOrder ?: Int.MAX_VALUE
                }
                sortedApps.forEach { app ->
                    dashboardItems.add(DashboardItem.ApplicationItem(app, category.id))
                }
            }
        }

        applications.forEach { app ->
            if (app.id != null && app.id !in allCategorizedAppIds) {
                dashboardItems.add(DashboardItem.ApplicationItem(app, 0L))
            }
        }

        return dashboardItems
    }

    /**
     * Called continuously by the UI while an item is being dragged.
     * This function only handles the visual reordering of the list.
     */
    fun onDrag(fromIndex: Int, toIndex: Int) {
        val currentItems = _uiState.value.dashboardItems.toMutableList()
        currentItems.add(toIndex, currentItems.removeAt(fromIndex))
        _uiState.update { it.copy(dashboardItems = currentItems) }
        lastToIndex = toIndex // Keep track of the last position
    }

    /**
     * Called by the UI when the user releases a dragged item.
     * This function handles the "merge" cleanup and triggers persistence.
     */
    fun onDragEnd() {
        val toIndex = lastToIndex
        if (toIndex == null) return // No drag occurred or was registered

        val currentItems = _uiState.value.dashboardItems.toMutableList()
        val movedItem = currentItems.getOrNull(toIndex)

        // --- Cleanup for merge ---
        if (movedItem is DashboardItem.ApplicationItem) {
            val newParentCategory = currentItems.subList(0, toIndex + 1).lastOrNull { it is DashboardItem.CategoryItem } as? DashboardItem.CategoryItem
            if (newParentCategory != null) {
                val isAlreadyInCategory = movedItem.application.applicationCategories.any { it.category?.id == newParentCategory.category.id }
                if (isAlreadyInCategory && movedItem.parentCategoryId != newParentCategory.category.id) {
                    val stationaryItem = currentItems.find {
                        it is DashboardItem.ApplicationItem &&
                                it.application.id == movedItem.application.id &&
                                it.parentCategoryId == newParentCategory.category.id
                    }
                    if (stationaryItem != null) {
                        currentItems.remove(stationaryItem)
                    }
                }
            }
        }

        _uiState.update { it.copy(dashboardItems = currentItems) }
        debouncedPersist(currentItems)
        lastToIndex = null // Reset for the next drag
    }

    enum class MoveDirection { UP, DOWN }

    fun moveCategory(categoryId: Long, direction: MoveDirection) {
        val currentItems = _uiState.value.dashboardItems

        val groups = mutableListOf<List<DashboardItem>>()
        var currentGroup = mutableListOf<DashboardItem>()

        currentItems.forEach { item ->
            if (item is DashboardItem.CategoryItem && currentGroup.isNotEmpty()) {
                groups.add(currentGroup.toList())
                currentGroup.clear()
            }
            currentGroup.add(item)
        }
        if (currentGroup.isNotEmpty()) {
            groups.add(currentGroup.toList())
        }

        val groupIndexToMove = groups.indexOfFirst {
            (it.firstOrNull() as? DashboardItem.CategoryItem)?.category?.id == categoryId
        }

        if (groupIndexToMove == -1) return

        val newGroups = groups.toMutableList()
        val groupToMove = newGroups.removeAt(groupIndexToMove)

        when {
            direction == MoveDirection.UP && groupIndexToMove > 0 -> {
                newGroups.add(groupIndexToMove - 1, groupToMove)
            }
            direction == MoveDirection.DOWN && groupIndexToMove < newGroups.size -> {
                newGroups.add(groupIndexToMove + 1, groupToMove)
            }
            else -> return
        }

        val newDashboardItems = newGroups.flatten()
        _uiState.update { it.copy(dashboardItems = newDashboardItems) }
        debouncedPersist(newDashboardItems)
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
        val applicationsToUpdate = mutableMapOf<Long, Application>()
        var currentCategory: Category? = null
        var categoryOrder = 0
        var appOrder = 0

        items.forEach { item ->
            when (item) {
                is DashboardItem.CategoryItem -> {
                    currentCategory = item.category
                    appOrder = 0
                    if (item.category.sortOrder != categoryOrder) {
                        categoriesToUpdate.add(item.category.copy(sortOrder = categoryOrder))
                    }
                    categoryOrder++
                }
                is DashboardItem.ApplicationItem -> {
                    val appToUpdate = applicationsToUpdate[item.application.id] ?: item.application
                    val newCategoryForThisLayout = currentCategory
                    val originalParentId = item.parentCategoryId
                    var appNeedsUpdate = false
                    var newCategoryLinks = appToUpdate.applicationCategories.toMutableList()

                    if (newCategoryForThisLayout != null) {
                        if (newCategoryForThisLayout.id != originalParentId) {
                            // MOVE to a new category.
                            newCategoryLinks.removeAll { it.category?.id == originalParentId }

                            val existingLinkIndex = newCategoryLinks.indexOfFirst { it.category?.id == newCategoryForThisLayout.id }
                            if (existingLinkIndex != -1) {
                                // App is already in the target category, just update the sort order.
                                newCategoryLinks[existingLinkIndex] = newCategoryLinks[existingLinkIndex].copy(sortOrder = appOrder)
                            } else {
                                // App is not in the target category, add it.
                                newCategoryLinks.add(ApplicationCategory(category = newCategoryForThisLayout, sortOrder = appOrder))
                            }
                            appNeedsUpdate = true

                        } else {
                            // REORDER within the same category.
                            val linkIndex = newCategoryLinks.indexOfFirst { it.category?.id == newCategoryForThisLayout.id }
                            if (linkIndex != -1 && newCategoryLinks[linkIndex].sortOrder != appOrder) {
                                newCategoryLinks[linkIndex] = newCategoryLinks[linkIndex].copy(sortOrder = appOrder)
                                appNeedsUpdate = true
                            }
                        }
                    } else { // App was moved to the uncategorized area
                        if (appToUpdate.applicationCategories.any { it.category?.id == originalParentId }) {
                            newCategoryLinks.removeAll { it.category?.id == originalParentId }
                            appNeedsUpdate = true
                        }
                    }

                    if (appNeedsUpdate) {
                        applicationsToUpdate[appToUpdate.id!!] = appToUpdate.copy(applicationCategories = newCategoryLinks)
                    }
                    appOrder++
                }
            }
        }
        if (categoriesToUpdate.isNotEmpty()) categoryRepository.reorderCategories(categoriesToUpdate)
        if (applicationsToUpdate.isNotEmpty()) applicationRepository.reorderApplications(applicationsToUpdate.values.toList())
    }

    fun sortAppsAlphabetically(categoryId: Long) {
        val currentItems = _uiState.value.dashboardItems.toMutableList()
        val categoryIndex = currentItems.indexOfFirst { it is DashboardItem.CategoryItem && it.category.id == categoryId }

        if (categoryIndex == -1) return

        val appsStart = categoryIndex + 1
        var appsEnd = appsStart
        while (appsEnd < currentItems.size) {
            val item = currentItems[appsEnd]
            if (item is DashboardItem.ApplicationItem && item.application.applicationCategories.any { it.category?.id == categoryId }) {
                appsEnd++
            } else {
                break
            }
        }

        if (appsStart >= appsEnd) return

        val appSublist = currentItems.subList(appsStart, appsEnd)
        val originalApps = appSublist.map { (it as DashboardItem.ApplicationItem).application }
        val sortedApps = originalApps.sortedBy { it.name }

        val applicationsToUpdate = sortedApps.mapIndexed { index, app ->
            val updatedCategories = app.applicationCategories.map { appCategory ->
                if (appCategory.category?.id == categoryId) {
                    appCategory.copy(sortOrder = index)
                } else {
                    appCategory
                }
            }
            app.copy(applicationCategories = updatedCategories)
        }

        val newAppItems = applicationsToUpdate.map { DashboardItem.ApplicationItem(it, categoryId) }
        val newDashboardItems = currentItems.subList(0, appsStart) + newAppItems + currentItems.subList(appsEnd, currentItems.size)
        _uiState.update { it.copy(dashboardItems = newDashboardItems) }

        // Persist the specific changes directly, bypassing the general-purpose persist function.
        persistenceJob?.cancel()
        persistenceJob = viewModelScope.launch {
            delay(debounceTime)
            try {
                if (applicationsToUpdate.isNotEmpty()) {
                    applicationRepository.reorderApplications(applicationsToUpdate)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save alphabetical sort order.") }
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

