package com.example.oidcapp

import okhttp3.OkHttpClient
import okhttp3.Request

class ApiService(private val accessToken: String) {
    private val apiUrl = "https://your.api.endpoint/api/resource"
    private val client = OkHttpClient()

    fun getProtectedResource(): String? {
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) response.body?.string() else null
        }
    }
}