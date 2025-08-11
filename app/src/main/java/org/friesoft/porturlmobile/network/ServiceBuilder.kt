package org.friesoft.porturlmobile.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private var token: String? = null
    private var baseUrl: String? = null
    private var retrofit: Retrofit? = null

    fun setToken(t: String?) {
        token = t
        retrofit = null // Force rebuild
    }

    fun setBaseUrl(url: String) {
        if (url.endsWith("/")) baseUrl = url else baseUrl = "$url/"
        retrofit = null // Force rebuild
    }

    fun <T> buildService(context: Context, service: Class<T>): T {
        if (baseUrl == null) throw IllegalStateException("Base URL not set")

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder().apply {
                token?.let { header("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(request)
        }).build()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl!!)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        return retrofit!!.create(service)
    }
}