package org.friesoft.porturl.data.auth

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An OkHttp Interceptor that adds the Authorization header with a valid bearer token to API requests.
 *
 * This interceptor is responsible for:
 * 1. Retrieving the current authentication state.
 * 2. If authenticated, using AppAuth's `performActionWithFreshTokens` to ensure the access
 * token is not expired. This will automatically refresh the token if necessary.
 * 3. Adding the fresh token to the request's `Authorization` header.
 * 4. Handling the asynchronous nature of token refresh within a synchronous interceptor chain
 * using a CountDownLatch.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionNotifier: SessionExpiredNotifier, // Injected the new notifier
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authState = tokenManager.getAuthState()
        var request: Request = chain.request()

        // --- DEBUG LOGGING START ---
        Log.d("AuthInterceptor", "Intercepting request for: ${request.url}")
        Log.d("AuthInterceptor", "Checking auth state. Is Authorized: ${authState.isAuthorized}")
        if (authState.isAuthorized) {
            Log.d("AuthInterceptor", "Access Token Expiration: ${authState.accessTokenExpirationTime}")
            Log.d("AuthInterceptor", "Refresh Token is null: ${authState.refreshToken == null}")
        }
        // --- DEBUG LOGGING END ---

        // Only try to refresh if we are authorized and have a refresh token
        if (authState.isAuthorized && authState.refreshToken != null) {
            // A CountDownLatch is used to block the interceptor thread until the
            // asynchronous token refresh operation is complete.
            val latch = CountDownLatch(1)
            var newRequest: Request? = null

            // performActionWithFreshTokens will automatically refresh the token if it's expired.
            // The `authState` object is updated in-place with the new token information.
            authState.performActionWithFreshTokens(AuthorizationService(context)) { accessToken, _, ex ->
                if (ex != null) {
                    // Token refresh failed. This can happen if the refresh token has also expired.
                    Log.e("AuthInterceptor", "Token refresh failed", ex)
                    GlobalScope.launch {
                        // Clear the invalid tokens immediately
                        tokenManager.clearAuthState()
                        // Notify the rest of the app that the session is gone
                        sessionNotifier.notifySessionExpired()
                    }

                } else if (accessToken != null) {
                    // The token is valid or was successfully refreshed.
                    // Build a new request with the Authorization header.
                    newRequest = chain.request().newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                }
                // Release the latch, allowing the interceptor thread to continue.
                latch.countDown()
            }

            try {
                // Wait for the token refresh process to complete
                latch.await()

                // Persist the potentially updated AuthState to disk.
                // This saves the new access token and (if rotated) the new refresh token.
                tokenManager.saveAuthState(authState)

                if (newRequest != null) {
                    request = newRequest!!
                }
            } catch (e: InterruptedException) {
                // Handle potential interruption of the waiting thread.
                Thread.currentThread().interrupt()
            }
        }

        // Proceed with either the original request or the new one with the auth header.
        return chain.proceed(request)

    }
}

