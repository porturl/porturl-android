package org.friesoft.porturl.data.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.*
import org.friesoft.porturl.data.repository.ConfigRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Manages the OAuth 2.0 authorization flow using the AppAuth library.
 */
@Singleton
class AuthService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configRepository: ConfigRepository
) {
    private val authService = AuthorizationService(context)

    // Cache the configuration to avoid fetching it on every login attempt
    private var authConfig: AuthorizationServiceConfiguration? = null

    private suspend fun getAuthServiceConfig(): AuthorizationServiceConfiguration {
        if (authConfig != null) {
            return authConfig!!
        }
        val issuerUri = configRepository.getIssuerUri().toUri()
        return suspendCoroutine { continuation ->
            AuthorizationServiceConfiguration.fetchFromIssuer(issuerUri) { config, ex ->
                if (config != null) {
                    authConfig = config
                    continuation.resume(config)
                } else {
                    continuation.resumeWithException(ex ?: RuntimeException("Failed to fetch OIDC configuration."))
                }
            }
        }
    }

    suspend fun performAuthorizationRequest(launcher: ActivityResultLauncher<Intent>) {
        val config = getAuthServiceConfig()
        val authRequest = AuthorizationRequest.Builder(
            config,
            "porturl-android-client",
            ResponseTypeValues.CODE,
            "org.friesoft.porturl:/oauth2redirect".toUri()
        ).setScope("openid profile email")
            .build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)
    }

    suspend fun handleAuthorizationResponse(intent: Intent): AuthState {
        val resp = AuthorizationResponse.fromIntent(intent)
        val authEx = AuthorizationException.fromIntent(intent)
        val authState = AuthState(resp, authEx)

        if (resp != null) {
            try {
                // This suspendCoroutine will now throw an exception on failure
                val tokenResponse = suspendCoroutine<TokenResponse> { continuation ->
                    authService.performTokenRequest(resp.createTokenExchangeRequest()) { response, tokenEx ->
                        when {
                            response != null -> continuation.resume(response)
                            tokenEx != null -> continuation.resumeWithException(tokenEx)
                            else -> continuation.resumeWithException(RuntimeException("Unknown token request error"))
                        }
                    }
                }
                // If the token exchange was successful, update the state with the new tokens
                authState.update(tokenResponse, null)
            } catch (tokenEx: AuthorizationException) {
                // If the token exchange failed, update the state with that specific exception
                // We explicitly cast null to TokenResponse? to resolve the compiler ambiguity.
                authState.update(null as TokenResponse?, tokenEx)
            }
        }
        return authState
    }

    /**
     * Creates and launches an intent to log the user out of the Keycloak session.
     */
    suspend fun performEndSessionRequest(idToken: String, launcher: ActivityResultLauncher<Intent>) {
        val config = getAuthServiceConfig()
        // The postLogoutRedirectUri tells Keycloak where to redirect after logout.
        // It must be a valid redirect URI configured in your Keycloak client.
        val endSessionRequest = EndSessionRequest.Builder(config)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri("org.friesoft.porturl:/oauth2redirect".toUri())
            .build()

        val endSessionIntent = authService.getEndSessionRequestIntent(endSessionRequest)
        launcher.launch(endSessionIntent)
    }
}

