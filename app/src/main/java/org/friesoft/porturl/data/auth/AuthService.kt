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

/**
 * Manages the OAuth 2.0 authorization flow using the AppAuth library.
 */
@Singleton
class AuthService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val authService = AuthorizationService(context)

    private suspend fun getAuthServiceConfig(): AuthorizationServiceConfiguration {
        val issuerUri = settingsRepository.keycloakUrl.first().toUri()
        // Use suspendCancellableCoroutine to bridge the callback-based API
        return suspendCancellableCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(
                issuerUri
            ) { serviceConfiguration, ex ->
                if (serviceConfiguration != null) {
                    continuation.resume(serviceConfiguration)
                } else {
                    continuation.resumeWithException(
                        ex ?: IllegalStateException("Unknown error fetching configuration")
                    )
                }
            }

            // Handle cancellation if the coroutine is cancelled
            continuation.invokeOnCancellation {
                // You might want to cancel the underlying network request if possible,
                // but AppAuth doesn't directly expose a way to cancel fetchFromIssuer.
            }
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
