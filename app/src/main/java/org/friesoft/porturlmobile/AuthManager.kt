package com.example.oidcapp

import android.app.Activity
import android.content.Context
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationException
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import android.content.Intent
import android.net.Uri

class AuthManager(private val context: Context) {
    private val authService = AuthorizationService(context)

    // Replace with your OIDC provider details
    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("https://YOUR_OIDC_ISSUER/.well-known/openid-configuration"),
        Uri.parse("https://YOUR_OIDC_ISSUER/oauth/token")
    )
    private val clientId = "android-client"
    private val redirectUri = Uri.parse("com.example.oidcapp:/oauth2redirect")
    private val scope = "openid profile email"

    fun getAuthRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            AuthorizationRequest.RESPONSE_TYPE_CODE,
            redirectUri
        ).setScope(scope).build()
    }

    fun performAuthRequest(activity: Activity, request: AuthorizationRequest, requestCode: Int) {
        val intent = authService.getAuthorizationRequestIntent(request)
        activity.startActivityForResult(intent, requestCode)
    }

    fun handleAuthResponse(
        data: Intent?,
        onSuccess: (TokenResponse) -> Unit,
        onError: (AuthorizationException?) -> Unit
    ) {
        val resp = AuthorizationResponse.fromIntent(data!!)
        val ex = AuthorizationException.fromIntent(data)
        if (resp != null) {
            val tokenRequest = resp.createTokenExchangeRequest()
            authService.performTokenRequest(tokenRequest) { tokenResponse, exception ->
                if (tokenResponse != null) onSuccess(tokenResponse)
                else onError(exception)
            }
        } else {
            onError(ex)
        }
    }
}