package org.friesoft.porturl.data.repository

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

// Data class to parse the JSON response from /actuator/info
@Serializable
data class AppConfig(
    @SerialName("auth") val auth: AuthInfo,
    @SerialName("build") val build: BuildInfo? = null,
    @SerialName("telemetry") val telemetry: TelemetryInfo? = null
)

@Serializable
data class AuthInfo(
    @SerialName("issuer-uri") val issuerUri: String
)

@Serializable
data class BuildInfo(
    @SerialName("version") val version: String
)

@Serializable
data class TelemetryInfo(
    @SerialName("enabled") val enabled: Boolean = false,
    @SerialName("healthy") val healthy: Boolean = false
)

// A simple interface for the config service
interface ConfigService {
    @GET("actuator/info")
    suspend fun getAppConfig(): AppConfig
}

@Singleton
class ConfigRepository @Inject constructor(
    // It's important this Retrofit instance does NOT have the AuthInterceptor
    @param:Named("unauthenticated_retrofit") private val retrofit: Retrofit
) {
    // Lazy create the service
    private val configService: ConfigService by lazy {
        retrofit.create(ConfigService::class.java)
    }

    suspend fun getAppConfig(): AppConfig {
        return configService.getAppConfig()
    }

    suspend fun getIssuerUri(): String {
        return getAppConfig().auth.issuerUri
    }

    suspend fun getTelemetryStatus(): TelemetryInfo? {
        return try {
            getAppConfig().telemetry
        } catch (e: Exception) {
            Log.e("ConfigRepository", "Failed to fetch telemetry status", e)
            null
        }
    }

    /**
     * Validates a given backend URL by attempting to connect and fetch the /actuator/info endpoint.
     *
     * @param url The base URL of the backend to validate.
     * @return True if the connection is successful, false otherwise.
     */
    suspend fun validateBackendUrl(url: String): Boolean {
        return try {
            // Create a temporary, unauthenticated client with a short timeout for this check.
            val tempOkHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val contentType = "application/json".toMediaType()
            val json = Json { ignoreUnknownKeys = true }
            val tempRetrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(tempOkHttpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            val tempService = tempRetrofit.create(ConfigService::class.java)
            // We just need to see if the call succeeds. The response is not used.
            tempService.getAppConfig()
            true
        } catch (e: Exception) {
            Log.w("ConfigRepository", "Validation failed for URL: $url", e)
            false
        }
    }
}
