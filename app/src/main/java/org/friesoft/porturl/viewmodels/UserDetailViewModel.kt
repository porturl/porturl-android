package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.ApplicationWithRolesDto
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val applicationRepository: ApplicationRepository
) : ViewModel() {

    private var userId: Long = -1

    data class UiState(
        val userRoles: List<String> = emptyList(),
        val allApplications: List<ApplicationWithRolesDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadUser(userIdStr: String) {
        val id = userIdStr.toLongOrNull()
        if (id == null) {
            _uiState.value = _uiState.value.copy(error = "Invalid User ID")
            return
        }
        this.userId = id
        loadData()
    }

    private fun loadData() {
        if (userId == -1L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val roles = userRepository.getUserRoles(userId)
                val apps = applicationRepository.getAllApplicationsWithRoles()
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
                val newRoles = userRepository.getUserRoles(userId)
                _uiState.value = _uiState.value.copy(userRoles = newRoles)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun hasRole(app: Application, role: String): Boolean {
        if (!app.clientId.isNullOrBlank()) {
            // Linked apps: APP_{id}_{role}
            return _uiState.value.userRoles.contains("APP_${app.id}_$role")
        }
        return false // Unlinked apps no longer have functional roles besides access
    }

    fun hasAccess(app: Application): Boolean {
        val accessRoleName = "APP_${app.name?.uppercase()?.replace(Regex("[^A-Z0-9]"), "_")}_ACCESS"
        return _uiState.value.userRoles.contains(accessRoleName)
    }

    fun toggleAccess(app: Application, isChecked: Boolean) {
        val accessRoleName = "APP_${app.name?.uppercase()?.replace(Regex("[^A-Z0-9]"), "_")}_ACCESS"
        if (isChecked) assignRole(app, accessRoleName) else unassignRole(app, accessRoleName)
    }
}