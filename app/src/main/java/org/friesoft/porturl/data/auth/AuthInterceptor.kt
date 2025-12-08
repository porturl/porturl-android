package org.friesoft.porturl.data.auth

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
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
    private val authStateManager: AuthStateManager,
    private val sessionNotifier: SessionExpiredNotifier,
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authState = authStateManager.current
        var request = chain.request()

        // --- DEBUG LOGGING START ---
        Log.d("AuthInterceptor", "Intercepting request for: ${request.url}")
        Log.d("AuthInterceptor", "Checking auth state. Is Authorized: ${authState.isAuthorized}")
        if (authState.isAuthorized) {
            Log.d("AuthInterceptor", "Access Token Expiration: ${authState.accessTokenExpirationTime}")
            Log.d("AuthInterceptor", "Refresh Token is null: ${authState.refreshToken == null}")
        }
        // --- DEBUG LOGGING END ---

        if (authState.isAuthorized) {
            val latch = CountDownLatch(1)
            var newRequest: okhttp3.Request? = null
            val authService = AuthorizationService(context)

            try {
                authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
                    try {
                        if (ex != null) {
                            Log.e("AuthInterceptor", "Token refresh failed.", ex)
                            runBlocking {
                                authStateManager.clearAuthState()
                                sessionNotifier.notifySessionExpired()
                            }
                        } else if (accessToken != null) {
                            Log.d("AuthInterceptor", "Fresh token obtained: ${accessToken.take(10)}...")
                            newRequest = chain.request().newBuilder()
                                .header("Authorization", "Bearer $accessToken")
                                .build()
                        } else {
                            Log.w("AuthInterceptor", "No access token and no exception.")
                        }
                        authStateManager.replace(authState)
                    } finally {
                        latch.countDown()
                    }
                }
                latch.await()
            } finally {
                authService.dispose()
            }

            newRequest?.let { request = it }
        }

        return chain.proceed(request)
    }
}
