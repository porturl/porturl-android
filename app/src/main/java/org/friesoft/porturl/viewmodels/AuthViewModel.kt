package org.friesoft.porturl.viewmodels

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import org.friesoft.porturl.data.auth.AuthService
import org.friesoft.porturl.data.auth.TokenManager
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
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Private mutable state flow to hold the current authentication state.
    private val _authState = MutableStateFlow(tokenManager.getAuthState())
    // Publicly exposed, read-only state flow for the UI to observe.
    val authState: StateFlow<AuthState> = _authState

    // Private mutable state flow for the initial navigation route.
    private val _startDestination = MutableStateFlow("")
    // Publicly exposed, read-only state flow for the AppNavigation composable to observe.
    val startDestination: StateFlow<String> = _startDestination

    init {
        // When the ViewModel is created, determine the starting screen.
        // If the user's AuthState is authorized, go to the app list. Otherwise, go to login.
        _startDestination.value = if (_authState.value.isAuthorized) Routes.APP_LIST else Routes.LOGIN
    }

    /**
     * Initiates the login flow.
     *
     * @param launcher The ActivityResultLauncher that will receive the result from the
     * authorization intent (the custom browser tab).
     */
    fun login(launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            authService.performAuthorizationRequest(launcher)
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
            val newState = authService.handleAuthorizationResponse(intent)
            // Persist the new state.
            tokenManager.saveAuthState(newState)
            // Update the in-memory state to trigger UI updates.
            _authState.value = newState
        }
    }

    /**
     * Logs the user out by clearing the authentication state.
     */
    fun logout() {
        // Clear the persisted AuthState.
        tokenManager.clearAuthState()
        // Update the in-memory state to a new, empty AuthState.
        // This will cause observers (like the ApplicationListScreen) to react and navigate to Login.
        _authState.value = AuthState()
    }
}