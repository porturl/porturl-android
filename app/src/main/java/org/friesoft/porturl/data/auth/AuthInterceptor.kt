package org.friesoft.porturl.data.auth

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
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

        // Only try to add a token if the user is actually authorized.
        if (authState.isAuthorized) {
            // A CountDownLatch is used to block the interceptor thread until the
            // asynchronous token refresh operation is complete.
            val latch = CountDownLatch(1)
            var newRequest: Request? = null

            // This is the key AppAuth method. It executes the given action with a fresh token.
            // If the token is expired, it will automatically use the refresh token to get a new one.
            authState.performActionWithFreshTokens(AuthorizationService(context)) { accessToken, _, ex ->
                if (ex == null && accessToken != null) {
                    // Token is valid and fresh. Rebuild the request with the Authorization header.
                    newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()
                }
                // Release the latch, allowing the interceptor thread to continue.
                latch.countDown()
            }

            try {
                // Wait for the token refresh callback to complete.
                latch.await()
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

