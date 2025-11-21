package org.friesoft.porturl.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val applicationRepository: ApplicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve the userId from the navigation arguments.
    // Depending on how the route is defined, this might need to be parsed.
    private val userId: Long = checkNotNull(savedStateHandle.get<String>("userId")?.toLongOrNull()) {
        "userId argument is missing or invalid"
    }

    data class UiState(
        val userRoles: List<String> = emptyList(),
        val allApplications: List<Application> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val roles = userRepository.getUserRoles(userId)
                val apps = applicationRepository.getAllApplications()
                _uiState.value = UiState(
                    userRoles = roles,
                    allApplications = apps,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleRole(app: Application, role: String, isChecked: Boolean) {
        if (isChecked) assignRole(app, role) else unassignRole(app, role)
    }

    private fun assignRole(app: Application, role: String) {
        viewModelScope.launch {
             try {
                val appId = app.id ?: return@launch
                userRepository.assignRole(appId, userId, role)
                // Refresh roles
                val newRoles = userRepository.getUserRoles(userId)
                _uiState.value = _uiState.value.copy(userRoles = newRoles)
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun unassignRole(app: Application, role: String) {
        viewModelScope.launch {
             try {
                val appId = app.id ?: return@launch
                userRepository.unassignRole(appId, userId, role)
                // Refresh roles
                val newRoles = userRepository.getUserRoles(userId)
                _uiState.value = _uiState.value.copy(userRoles = newRoles)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun hasRole(app: Application, role: String): Boolean {
        // Logic to determine if the user has the role based on the backend convention
        // Example: app name "Grafana", role "admin" -> "ROLE_GRAFANA_ADMIN"
        val sanitizedAppName = app.name.uppercase().replace(Regex("[^A-Z0-9]"), "_")
        val expectedRole = "ROLE_${sanitizedAppName}_${role.uppercase()}"
        return _uiState.value.userRoles.contains(expectedRole)
    }
}
