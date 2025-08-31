package org.friesoft.porturl.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.repository.ApplicationRepository
import javax.inject.Inject

/**
 * ViewModel for the ApplicationDetailScreen.
 *
 * It handles the business logic for fetching, creating, and updating an application,
 * and exposes the state to the UI.
 */
@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val repository: ApplicationRepository
) : ViewModel() {

    /**
     * Represents the different states the UI can be in.
     */
    sealed class UiState {
        object Loading : UiState()
        data class Success(val application: Application) : UiState()
    }

    // Private mutable state flow for the UI state.
    private val _applicationState = MutableStateFlow<UiState>(UiState.Success(Application(null, "", "")))
    // Publicly exposed, read-only state flow for the UI to observe.
    val applicationState: StateFlow<UiState> = _applicationState

    // Stores the original application object when editing to track changes.
    private val originalApplication = mutableStateOf<Application?>(null)

    // SharedFlow for one-time events like navigating back.
    val finishScreen = MutableSharedFlow<Boolean>()
    // SharedFlow for one-time events like showing an error message.
    val errorMessage = MutableSharedFlow<String>()

    /**
     * Loads an application by its ID for editing, or prepares a new one for creation.
     * @param id The ID of the application. If -1L, it prepares for creation.
     */
    fun loadApplication(id: Long) {
        if (id == -1L) {
            // This is 'Create' mode.
            originalApplication.value = Application(id = null, name = "", url = "")
            _applicationState.value = UiState.Success(originalApplication.value!!)
            return
        }

        // This is 'Edit' mode.
        viewModelScope.launch {
            _applicationState.value = UiState.Loading
            try {
                val app = repository.getApplicationById(id)
                originalApplication.value = app
                _applicationState.value = UiState.Success(app)
            } catch (e: Exception) {
                errorMessage.emit("Failed to load application details.")
                finishScreen.emit(true) // Go back if we can't load the data.
            }
        }
    }

    /**
     * Saves the application. Either creates a new one or updates an existing one.
     * @param name The name of the application.
     * @param url The URL of the application.
     */
    fun saveApplication(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) {
            viewModelScope.launch { errorMessage.emit("Name and URL cannot be empty.") }
            return
        }

        val appToSave = originalApplication.value!!.copy(name = name, url = url)

        viewModelScope.launch {
            try {
                if (appToSave.id == null) {
                    repository.createApplication(appToSave)
                } else {
                    repository.updateApplication(appToSave.id!!, appToSave)
                }
                finishScreen.emit(true) // Navigate back on successful save.
            } catch (e: Exception) {
                errorMessage.emit("Failed to save application.")
            }
        }
    }
}
