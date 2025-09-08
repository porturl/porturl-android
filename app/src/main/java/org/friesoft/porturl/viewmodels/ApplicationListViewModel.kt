package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.CategoryRepository
import javax.inject.Inject

// A sealed interface to represent the different types of items in our unified grid
sealed interface DashboardItem {
    data class Header(val category: Category) : DashboardItem
    data class App(val application: Application, val parentCategoryId: Long) : DashboardItem
}

data class DashboardUiState(
    // The flattened list of all items (headers and apps) to be displayed in the grid
    val dashboardItems: List<DashboardItem> = emptyList(),
    val isLoading: Boolean = true,
    // We still keep the original lists for easier manipulation
    val categories: List<Category> = emptyList(),
    val applications: List<Application> = emptyList()
)

@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val appRepository: ApplicationRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val _applications = MutableStateFlow<List<Application>>(emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        _isLoading, _categories, _applications
    ) { isLoading, categories, applications ->
        val items = mutableListOf<DashboardItem>()
        // Sort categories by their defined sort order before building the list
        categories.sortedBy { it.sortOrder }.forEach { category ->
            items.add(DashboardItem.Header(category))
            // Find apps for this category and sort them
            val appsForCategory = applications
                .filter { app -> app.categories.any { it.id == category.id } }
                .sortedBy { it.sortOrder } // Assuming custom sort for now
            items.addAll(appsForCategory.map { DashboardItem.App(it, category.id) })
        }
        DashboardUiState(items, isLoading, categories, applications)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = categoryRepository.getAllCategories()
                _applications.value = appRepository.getAllApplications()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles all drag-and-drop movements in the unified grid.
     */
    fun onMove(fromIndex: Int, toIndex: Int) {
        val items = uiState.value.dashboardItems.toMutableList()
        val movedItem = items.removeAt(fromIndex)
        items.add(toIndex, movedItem)

        when (movedItem) {
            is DashboardItem.Header -> reorderCategories(items)
            is DashboardItem.App -> reorderApplication(items, movedItem)
        }
    }

    private fun reorderCategories(newList: List<DashboardItem>) {
        val newCategoryOrder = newList.filterIsInstance<DashboardItem.Header>().map { it.category }
        // Optimistically update the UI
        _categories.value = newCategoryOrder

        viewModelScope.launch {
            try {
                val updatedCategories = newCategoryOrder.mapIndexed { index, category -> category.copy(sortOrder = index) }
                updatedCategories.forEach { categoryRepository.updateCategory(it) }
            } catch (e: Exception) {
                // On failure, revert to the server state
                refreshDashboard()
            }
        }
    }

    /**
     * Handles reordering an application, preserving its many-to-many category relationships
     * and ensuring an application always has at least one category.
     */
    private fun reorderApplication(newList: List<DashboardItem>, movedAppItem: DashboardItem.App) {
        // 1. Get the original, unmodified state of the moved application
        val originalAppsById = _applications.value.associateBy { it.id!! }
        val movedAppOriginal = originalAppsById[movedAppItem.application.id!!] ?: return

        // 2. Determine the new parent category based on the drop position in the visual list
        val newParentCategory = newList.subList(0, newList.indexOf(movedAppItem))
            .filterIsInstance<DashboardItem.Header>()
            .lastOrNull()?.category

        // 3. Calculate the final list of categories for the moved application
        val finalCategories = movedAppOriginal.categories.toMutableSet()
        if (movedAppItem.parentCategoryId != newParentCategory?.id) {
            // The application was moved to a new category. Update its memberships.
            // Only remove the old category if the app has other memberships, or if we are adding a valid new one.
            if (finalCategories.size > 1 || newParentCategory != null) {
                finalCategories.removeAll { it.id == movedAppItem.parentCategoryId }
            }
            if (newParentCategory != null) {
                finalCategories.add(newParentCategory)
            }
            // Failsafe: an app must have at least one category. If it's somehow empty, revert the change.
            if (finalCategories.isEmpty()) {
                val originalParent = _categories.value.find { it.id == movedAppItem.parentCategoryId }
                if (originalParent != null) finalCategories.add(originalParent)
            }
        }

        // 4. Create a map of all applications that will need their sortOrder updated.
        // Start with the moved app, which now has its final, correct category list.
        val applicationsToUpdate = mutableMapOf<Long, Application>(
            movedAppOriginal.id!! to movedAppOriginal.copy(categories = finalCategories.toList())
        )

        // 5. Iterate through the new visual list *only* to calculate the new sort orders.
        var currentSortOrder = 0
        newList.forEach { item ->
            when (item) {
                is DashboardItem.Header -> currentSortOrder = 0 // Reset for each new category section
                is DashboardItem.App -> {
                    val appId = item.application.id!!
                    // Get the latest state of the app (either the one we just updated, or the original)
                    val appToUpdate = applicationsToUpdate[appId] ?: originalAppsById[appId]!!

                    // Always update the sort order based on its new visual position
                    applicationsToUpdate[appId] = appToUpdate.copy(sortOrder = currentSortOrder)
                    currentSortOrder++
                }
            }
        }

        // 6. Filter out apps that ultimately didn't change to minimize backend calls.
        val changedApps = applicationsToUpdate.values.filter { updatedApp ->
            updatedApp != originalAppsById[updatedApp.id]
        }

        // 7. Perform an optimistic UI update with only the changed applications.
        val finalAppList = _applications.value.map { app ->
            applicationsToUpdate[app.id] ?: app
        }
        _applications.value = finalAppList

        // 8. In the background, send only the changed applications to the backend.
        viewModelScope.launch {
            try {
                changedApps.forEach { updatedApp ->
                    appRepository.updateApplication(updatedApp.id!!, updatedApp)
                }
            } catch (e: Exception) {
                // If any backend update fails, revert the UI to the last known server state.
                refreshDashboard()
            }
        }
    }

    /**
     * Deletes an application by its ID and refreshes the dashboard.
     */
    fun deleteApplication(id: Long) {
        viewModelScope.launch {
            try {
                appRepository.deleteApplication(id)
                refreshDashboard()
            } catch (e: Exception) { /* Handle error */ }
        }
    }

    /**
     * Deletes a category by its ID and refreshes the dashboard.
     */
    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(id)
                refreshDashboard()
            } catch (e: Exception) { /* Handle error */ }
        }
    }
}

