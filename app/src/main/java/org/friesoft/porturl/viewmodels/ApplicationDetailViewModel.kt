package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.CategoryRepository
import javax.inject.Inject

/**
 * ViewModel for the ApplicationDetailScreen.
 *
 * It handles the business logic for fetching, creating, and updating an application,
 * and exposes the state to the UI.
 */
@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val categoryRepository: CategoryRepository // Inject the new repository
) : ViewModel() {

    // The UI state now includes a list of all available categories for the selection UI.
    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val application: Application,
            val allCategories: List<Category> = emptyList()
        ) : UiState()
    }

    // Private mutable state flow for the UI state.
    private val _applicationState = MutableStateFlow<UiState>(UiState.Loading)
    // Publicly exposed, read-only state flow for the UI to observe.
    val applicationState: StateFlow<UiState> = _applicationState

    // Stores the original application object when editing to track changes.
    private var originalApplication: Application? = null

    // SharedFlow for one-time events like navigating back.
    val finishScreen = MutableSharedFlow<Boolean>()
    // SharedFlow for one-time events like showing an error message.
    val errorMessage = MutableSharedFlow<String>()

    /**
     * Loads an application by its ID for editing, or prepares a new one for creation.
     * @param id The ID of the application. If -1L, it prepares for creation.
     */
    fun loadApplication(id: Long) {
        viewModelScope.launch {
            _applicationState.value = UiState.Loading
            try {
                // Fetch both the application and all available categories in parallel
                val allCategories = categoryRepository.getAllCategories()
                val app = if (id == -1L) {
                    // For a new app, create a blank template
                    Application(null, "", "", emptyList(), 0, null, null, null, null, null, null)
                } else {
                    applicationRepository.getApplicationById(id)
                }
                originalApplication = app
                _applicationState.value = UiState.Success(app, allCategories)
            } catch (e: Exception) {
                errorMessage.emit("Failed to load application data.")
                finishScreen.emit(true)
            }
        }
    }

    /**
     * Saves the application. Either creates a new one or updates an existing one.
     * @param name The name of the application.
     * @param url The URL of the application.
     */
    fun saveApplication(
        name: String,
        url: String,
        sortOrder: Int,
        selectedCategoryIds: Set<Long>,
        iconLarge: String?,
        iconMedium: String?,
        iconThumbnail: String?
    ) {
        if (name.isBlank() || url.isBlank()) {
            viewModelScope.launch { errorMessage.emit("Name and URL cannot be empty.") }
            return
        }

        // Reconstruct the full Category objects from the selected IDs
        val selectedCategories = (_applicationState.value as? UiState.Success)
            ?.allCategories
            ?.filter { it.id in selectedCategoryIds }
            ?: emptyList()

        val appToSave = originalApplication!!.copy(
            name = name,
            url = url,
            sortOrder = sortOrder,
            categories = selectedCategories,
            iconLarge = iconLarge?.takeIf { it.isNotBlank() },
            iconMedium = iconMedium?.takeIf { it.isNotBlank() },
            iconThumbnail = iconThumbnail?.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            try {
                if (appToSave.id == null) {
                    applicationRepository.createApplication(appToSave)
                } else {
                    applicationRepository.updateApplication(appToSave.id!!, appToSave)
                }
                finishScreen.emit(true)
            } catch (e: Exception) {
                errorMessage.emit("Failed to save application.")
            }
        }
    }
}
