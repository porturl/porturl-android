package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.repository.ApplicationRepository
import javax.inject.Inject

/**
 * ViewModel for the ApplicationListScreen.
 *
 * This class is responsible for fetching the list of applications from the
 * [ApplicationRepository], managing the UI's loading state, and handling
 * deletion requests.
 */
@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val repository: ApplicationRepository
) : ViewModel() {

    // Private mutable state flow to hold the list of applications.
    private val _applications = MutableStateFlow<List<Application>>(emptyList())
    // Publicly exposed, read-only state flow for the UI to observe.
    val applications: StateFlow<List<Application>> = _applications

    // Private mutable state flow for the loading state.
    private val _isLoading = MutableStateFlow(true)
    // Publicly exposed, read-only state flow for the UI to observe the loading status.
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Fetch applications as soon as the ViewModel is created.
        loadApplications()
    }

    /**
     * Fetches the list of applications from the repository and updates the state flows.
     */
    private fun loadApplications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // On success, update the applications list.
                _applications.value = repository.getAllApplications()
            } catch (e: Exception) {
                // In a real app, you would handle this error, e.g., by showing a message.
                _applications.value = emptyList() // Clear list on error
            } finally {
                // Ensure the loading indicator is hidden when the operation completes.
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes an application by its ID and then refreshes the list.
     * @param id The unique identifier of the application to delete.
     */
    fun deleteApplication(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteApplication(id)
                // After a successful deletion, refresh the list to reflect the change.
                loadApplications()
            } catch (e: Exception) {
                // In a real app, you would handle this error, e.g., by showing a message.
            }
        }
    }
}
