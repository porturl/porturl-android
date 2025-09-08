package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

/**
 * Represents the complete UI state for the main dashboard screen.
 */
data class DashboardUiState(
    val dashboardItems: List<DashboardItem> = emptyList(),
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val applications: List<Application> = emptyList()
)

/**
 * ViewModel for the main ApplicationListScreen. It fetches all necessary data,
 * constructs the UI state, and handles user interactions like reordering.
 */
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
        categories.sortedBy { it.sortOrder }.forEach { category ->
            items.add(DashboardItem.Header(category))
            val appsForCategory = applications
                .mapNotNull { app ->
                    app.applicationCategories.find { it.category.id == category.id }
                        ?.let { app to it.sortOrder }
                }
                .sortedBy { it.second }
                .map { DashboardItem.App(it.first, category.id) }
            items.addAll(appsForCategory)
        }
        DashboardUiState(items, isLoading, categories, applications)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init { refreshDashboard() }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = categoryRepository.getAllCategories()
                _applications.value = appRepository.getAllApplications()
            } catch (e: Exception) {
                // Handle error
            } finally { _isLoading.value = false }
        }
    }

    fun onMove(fromIndex: Int, toIndex: Int) {
        val items = uiState.value.dashboardItems.toMutableList()
        val movedItem = items.removeAt(fromIndex)
        items.add(toIndex, movedItem)

        when (movedItem) {
            is DashboardItem.Header -> reorderCategories(items)
            // ** THE FIX IS HERE **
            // The method was renamed to `reorderApplication` (singular) and now
            // correctly passes the `movedItem` so the logic knows which app to update.
            is DashboardItem.App -> reorderApplication(items, movedItem)
        }
    }

    private fun reorderCategories(newList: List<DashboardItem>) {
        val newCategoryOrder = newList.filterIsInstance<DashboardItem.Header>().map { it.category }
        _categories.value = newCategoryOrder // Optimistic UI update

        viewModelScope.launch {
            try {
                val updatedCategories = newCategoryOrder.mapIndexed { index, category -> category.copy(sortOrder = index) }
                updatedCategories.forEach { categoryRepository.updateCategory(it) }
            } catch (e: Exception) {
                refreshDashboard() // Revert on failure
            }
        }
    }

    private fun reorderApplication(newList: List<DashboardItem>, movedAppItem: DashboardItem.App) {
        val originalAppsById = _applications.value.associateBy { it.id!! }
        val finalApps = _applications.value.toMutableList()

        val movedApp = originalAppsById[movedAppItem.application.id!!]!!.let {
            val newParentCategory = newList.subList(0, newList.indexOf(movedAppItem))
                .filterIsInstance<DashboardItem.Header>().lastOrNull()?.category

            val newCategories = it.applicationCategories.toMutableSet()
            if (movedAppItem.parentCategoryId != newParentCategory?.id) {
                newCategories.removeAll { it.category.id == movedAppItem.parentCategoryId }
                if (newParentCategory != null) {
                    newCategories.add(org.friesoft.porturl.data.model.ApplicationCategory(newParentCategory, 0))
                }
            }
            it.copy(applicationCategories = newCategories.toList())
        }

        val movedAppIndex = finalApps.indexOfFirst { it.id == movedApp.id }
        if (movedAppIndex != -1) finalApps[movedAppIndex] = movedApp

        val appsToUpdate = mutableMapOf<Long, Application>()
        var currentCategory: Category? = null
        var currentSortOrder = 0

        newList.forEach { item ->
            when (item) {
                is DashboardItem.Header -> {
                    currentCategory = item.category
                    currentSortOrder = 0
                }
                is DashboardItem.App -> {
                    val appToUpdate = (appsToUpdate[item.application.id] ?: finalApps.find { it.id == item.application.id })!!

                    val existingLink = appToUpdate.applicationCategories.find { it.category.id == currentCategory?.id }
                    if (existingLink != null && existingLink.sortOrder != currentSortOrder) {
                        val updatedLinks = appToUpdate.applicationCategories.map {
                            if (it.category.id == currentCategory?.id) it.copy(sortOrder = currentSortOrder) else it
                        }
                        appsToUpdate[appToUpdate.id!!] = appToUpdate.copy(applicationCategories = updatedLinks)
                    }
                    currentSortOrder++
                }
            }
        }

        val finalOptimisticList = finalApps.map { appsToUpdate[it.id] ?: it }
        _applications.value = finalOptimisticList

        viewModelScope.launch {
            try {
                appRepository.reorderApplications(appsToUpdate.values.toList())
            } catch (e: Exception) {
                refreshDashboard()
            }
        }
    }

    fun deleteApplication(id: Long) {
        viewModelScope.launch {
            try { appRepository.deleteApplication(id); refreshDashboard() }
            catch (e: Exception) { /* Handle error */ }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try { categoryRepository.deleteCategory(id); refreshDashboard() }
            catch (e: Exception) { /* Handle error */ }
        }
    }
}

