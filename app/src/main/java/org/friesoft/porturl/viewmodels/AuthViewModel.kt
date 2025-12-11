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
import org.friesoft.porturl.data.auth.AuthStateManager
import org.friesoft.porturl.data.auth.AuthService
import org.friesoft.porturl.data.auth.SessionExpiredNotifier
import org.friesoft.porturl.data.repository.ConfigRepository
import org.friesoft.porturl.data.repository.SettingsRepository
import org.friesoft.porturl.data.repository.UserRepository
import org.friesoft.porturl.ui.navigation.Routes
import javax.inject.Inject

/**
 * ViewModel responsible for managing authentication state and logic.
 *
 * This ViewModel coordinates the login/logout process and provides the
 * current authentication state to the UI (Composable screens).
 *
 * @property authService The service responsible for handling the AppAuth authorization flow.
 * @property tokenManager The manager for persisting and retrieving the AuthState.
 */
import org.friesoft.porturl.data.repository.ImageRepository

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val authStateManager: AuthStateManager,
    private val sessionNotifier: SessionExpiredNotifier, // Inject the notifier
    private val settingsRepository: SettingsRepository,
    private val configRepository: ConfigRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    // Private mutable state flow to hold the current authentication state.
    private val _authState = MutableStateFlow(AuthState())
    // Publicly exposed, read-only state flow for the UI to observe.
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<org.friesoft.porturl.data.model.User?>(null)
    val currentUser: StateFlow<org.friesoft.porturl.data.model.User?> = _currentUser.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // New state to control the visibility of the session expired dialog
    private val _showSessionExpiredDialog = MutableStateFlow(false)
    val showSessionExpiredDialog = _showSessionExpiredDialog.asStateFlow()

    // New state to hold login-specific error messages
    private val _loginError = MutableStateFlow<Int?>(null)
    val loginError = _loginError.asStateFlow()

    private val _isBackendUrlValid = MutableStateFlow(false)
    val isBackendUrlValid = _isBackendUrlValid.asStateFlow()


    init {
        // When the ViewModel is created, determine the starting screen.
        // If the user's AuthState is authorized, go to the app list. Otherwise, go to login.
        viewModelScope.launch {
            _authState.value = authStateManager.current
            checkBackendUrlValid()
            checkIsAdmin()
            fetchCurrentUser()
        }

        // Listen for session expiration events from the notifier
        viewModelScope.launch {
            sessionNotifier.sessionExpiredEvents.collect {
                // Immediately update the auth state to unauthorized.
                // This ensures the UI will react correctly and navigate away
                // from authenticated screens.
                _authState.value = AuthState()
                _currentUser.value = null

                // Then, show the informational dialog to the user.
                _showSessionExpiredDialog.value = true
            }
        }
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            if (_authState.value.isAuthorized) {
                try {
                    _currentUser.value = userRepository.getCurrentUser()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to fetch current user", e)
                }
            } else {
                _currentUser.value = null
            }
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

    // Function to be called by the UI when the dialog is dismissed
    fun onSessionExpiredDialogDismissed() {
        _showSessionExpiredDialog.value = false
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    /**
     * Checks if the backend URL is set and updates the corresponding state.
     */
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
                    _isAdmin.value = roles.contains("ROLE_ADMIN")
                } catch (e: Exception) {
                    _isAdmin.value = false
                }
            } else {
                _isAdmin.value = false
            }
        }
    }

    /**
     * Initiates the login flow.
     *
     * @param launcher The ActivityResultLauncher that will receive the result from the
     * authorization intent (the custom browser tab).
     */
    fun login(launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            try {
                // Clear any old errors before attempting to log in
                clearLoginError()
                // This will throw an exception if the backend is down
                authService.performAuthorizationRequest(launcher)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed: Could not get SSO config from backend.", e)
                _loginError.value = R.string.authviewmodel_error_could_not_connect_backend
            }
        }
    }

    /**
     * Handles the redirect intent from the custom tab after the user has logged in.
     *
     * @param intent The intent containing the authorization response.
     */
    fun handleAuthorizationResponse(intent: Intent) {
        viewModelScope.launch {
            // Process the response to get a new AuthState (with tokens).
            val authState = authService.handleAuthorizationResponse(intent)
            authStateManager.replace(authState)
            // Update the in-memory state to trigger UI updates.
            _authState.value = authState
            checkIsAdmin()
        }
    }

    /**
     * Logs the user out by performing a remote logout with Keycloak and then
     * clearing the local authentication state.
     *
     * @param launcher The ActivityResultLauncher to launch the browser intent for logout.
     */
    fun logout(launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            val currentState = authStateManager.current
            if (currentState.isAuthorized && currentState.idToken != null) {
                // Perform the remote logout by opening the browser
                authService.performEndSessionRequest(currentState.idToken!!, launcher)
            }
            // Clear the local tokens regardless
            authStateManager.clearAuthState()
            _authState.value = AuthState()
            _isAdmin.value = false
            checkBackendUrlValid()
        }
    }
}