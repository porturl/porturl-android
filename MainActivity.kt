package com.example.oidcapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.TokenResponse

class MainActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private var accessToken: String? = null
    private val AUTH_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authManager = AuthManager(this)

        val loginBtn = findViewById<Button>(R.id.loginButton)
        val statusText = findViewById<TextView>(R.id.statusText)

        loginBtn.setOnClickListener {
            val authRequest = authManager.getAuthRequest()
            authManager.performAuthRequest(this, authRequest, AUTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            authManager.handleAuthResponse(data,
                onSuccess = { tokenResponse: TokenResponse ->
                    accessToken = tokenResponse.accessToken
                    findViewById<TextView>(R.id.statusText).text = "Authenticated!"
                    // Now you can call the API
                    val apiService = ApiService(accessToken!!)
                    Thread {
                        val result = apiService.getProtectedResource()
                        runOnUiThread {
                            findViewById<TextView>(R.id.statusText).text = result ?: "Error"
                        }
                    }.start()
                },
                onError = {
                    findViewById<TextView>(R.id.statusText).text = "Auth failed"
                }
            )
        }
    }
}