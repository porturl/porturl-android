package org.friesoft.porturl.viewmodels

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.User
import org.friesoft.porturl.data.auth.AuthStateManager
import org.friesoft.porturl.data.auth.AuthService
import org.friesoft.porturl.data.auth.SessionExpiredNotifier
import org.friesoft.porturl.data.repository.ConfigRepository
import org.friesoft.porturl.data.repository.SettingsRepository
import org.friesoft.porturl.data.repository.UserRepository
import org.friesoft.porturl.data.repository.ImageRepository
import javax.inject.Inject

/**
 * ViewModel responsible for managing authentication state and logic.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val authStateManager: AuthStateManager,
    private val sessionNotifier: SessionExpiredNotifier,
    private val settingsRepository: SettingsRepository,
    private val configRepository: ConfigRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _userRoles = MutableStateFlow<List<String>>(emptyList())
    val userRoles: StateFlow<List<String>> = _userRoles.asStateFlow()

    private val _showSessionExpiredDialog = MutableStateFlow(false)
    val showSessionExpiredDialog = _showSessionExpiredDialog.asStateFlow()

    private val _loginError = MutableStateFlow<Int?>(null)
    val loginError = _loginError.asStateFlow()

    private val _isBackendUrlValid = MutableStateFlow(false)
    val isBackendUrlValid = _isBackendUrlValid.asStateFlow()

    init {
        viewModelScope.launch {
            _authState.value = authStateManager.current
            checkBackendUrlValid()
            checkIsAdmin()
            fetchCurrentUser()
        }

        viewModelScope.launch {
            sessionNotifier.sessionExpiredEvents.collect {
                _authState.value = AuthState()
                _currentUser.value = null
                _isAdmin.value = false
                _userRoles.value = emptyList()
                _showSessionExpiredDialog.value = true
            }
        }
    }

    private suspend fun fetchCurrentUser() {
        if (_authState.value.isAuthorized) {
            try {
                val user = userRepository.getCurrentUser()
                Log.d("AuthViewModel", "Fetched user: ${user.email}, imageUrl: ${user.imageUrl}")
                _currentUser.value = user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to fetch current user", e)
                _currentUser.value = null
            }
        } else {
            _currentUser.value = null
        }
    }

    fun updateUserImage(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val filename = imageRepository.uploadImage(uri)
                if (filename != null) {
                    val updatedUser = userRepository.updateCurrentUser(filename)
                    _currentUser.value = updatedUser
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update user image", e)
            }
        }
    }

    fun onSessionExpiredDialogDismissed() {
        _showSessionExpiredDialog.value = false
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun checkBackendUrlValid() {
        viewModelScope.launch {
            val url = settingsRepository.backendUrl.first()
            _isBackendUrlValid.value = configRepository.validateBackendUrl(url)
        }
    }

    fun checkIsAdmin() {
        viewModelScope.launch {
            if (_authState.value.isAuthorized) {
                try {
                    val roles = userRepository.getCurrentUserRoles()
                    _userRoles.value = roles
                    _isAdmin.value = roles.contains("ROLE_ADMIN")
                } catch (e: Exception) {
                    _userRoles.value = emptyList()
                    _isAdmin.value = false
                }
            } else {
                _userRoles.value = emptyList()
                _isAdmin.value = false
            }
        }
    }

    fun login(launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            try {
                clearLoginError()
                authService.performAuthorizationRequest(launcher)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed: Could not get SSO config from backend.", e)
                _loginError.value = R.string.authviewmodel_error_could_not_connect_backend
            }
        }
    }

    fun handleAuthorizationResponse(intent: Intent) {
        viewModelScope.launch {
            val authState = authService.handleAuthorizationResponse(intent)
            authStateManager.replace(authState)
            _authState.value = authState
            checkIsAdmin()
            fetchCurrentUser()
        }
    }

    fun logout(launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            val currentState = authStateManager.current
            if (currentState.isAuthorized && currentState.idToken != null) {
                authService.performEndSessionRequest(currentState.idToken!!, launcher)
            }
            authStateManager.clearAuthState()
            _authState.value = AuthState()
            _isAdmin.value = false
            _userRoles.value = emptyList()
            _currentUser.value = null
            checkBackendUrlValid()
        }
    }
}
