package org.friesoft.porturl.data.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine // Import this
import kotlinx.coroutines.withContext
import net.openid.appauth.*
import org.friesoft.porturl.data.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume // Import this
import kotlin.coroutines.resumeWithException // Import this
import kotlin.coroutines.suspendCoroutine
import androidx.core.net.toUri
import org.friesoft.porturl.data.repository.ConfigRepository

/**
 * Manages the OAuth 2.0 authorization flow using the AppAuth library.
 */
@Singleton
class AuthService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configRepository: ConfigRepository // Inject the new repository
) {
    private val authService = AuthorizationService(context)

    // Cache the configuration to avoid fetching it on every login attempt
    private var authConfig: AuthorizationServiceConfiguration? = null

    private suspend fun getAuthServiceConfig(): AuthorizationServiceConfiguration {
        // Use the cached config if available
        if (authConfig != null) {
            return authConfig!!
        }
        // Otherwise, fetch the issuer URI from the backend
        val issuerUri = configRepository.getIssuerUri().toUri()
        // Use suspendCancellableCoroutine to bridge the callback
        return suspendCancellableCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(
                issuerUri
            ) { serviceConfiguration, ex ->
                if (ex != null) {
                    continuation.resumeWithException(ex)
                } else if (serviceConfiguration != null) {
                    authConfig = serviceConfiguration // Cache the result
                    continuation.resume(serviceConfiguration)
                } else {
                    // This case should ideally not happen if ex is null,
                    // but handle it defensively.
                    continuation.resumeWithException(
                        IllegalStateException("Failed to fetch configuration and no exception was provided.")
                    )
                }
            }
            // Optional: Handle cancellation if needed
            continuation.invokeOnCancellation { /* Cleanup if necessary */ }
        }
    }

    suspend fun performAuthorizationRequest(launcher: ActivityResultLauncher<Intent>) {
        val config = getAuthServiceConfig()
        val authRequest = AuthorizationRequest.Builder(
            config,
            "porturl-android-client", // Your Keycloak client ID
            ResponseTypeValues.CODE,
            Uri.parse("org.friesoft.porturl:/oauth2redirect") // Your redirect URI
        ).setScope("openid profile email")
            .build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)
    }

    suspend fun handleAuthorizationResponse(intent: Intent): AuthState {
        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        val authState = AuthState(resp, ex)

        if (resp != null) {
            val tokenResponse = exchangeAuthorizationCode(resp)
            authState.update(tokenResponse, ex)
        }
        return authState
    }

    private suspend fun exchangeAuthorizationCode(response: AuthorizationResponse): TokenResponse? {
        // You're already correctly using suspendCoroutine here for performTokenRequest
        return suspendCoroutine { continuation ->
            authService.performTokenRequest(response.createTokenExchangeRequest()) { resp, ex ->
                if (ex != null) {
                    continuation.resumeWithException(ex)
                } else {
                    continuation.resume(resp)
                }
            }
        }
    }
}
