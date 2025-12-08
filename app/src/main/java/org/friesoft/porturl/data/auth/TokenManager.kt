package org.friesoft.porturl.data.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(private val authStateManager: AuthStateManager) {

    fun getAuthState() = authStateManager.current

    fun saveAuthState(authState: net.openid.appauth.AuthState) {
        authStateManager.replace(authState)
    }

    fun clearAuthState() {
        authStateManager.clearAuthState()
    }
}
