package org.friesoft.porturl.data.repository

import android.util.Log
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

// Data class to parse the JSON response from /actuator/info
data class AppConfig(
    @SerializedName("auth") val auth: AuthInfo,
    @SerializedName("telemetry") val telemetry: TelemetryInfo?
)

data class AuthInfo(
    @SerializedName("issuer-uri") val issuerUri: String
)

data class TelemetryInfo(
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("healthy") val healthy: Boolean
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

    suspend fun getIssuerUri(): String {
        return configService.getAppConfig().auth.issuerUri
    }

    suspend fun getTelemetryStatus(): TelemetryInfo? {
        return try {
            configService.getAppConfig().telemetry
        } catch (e: Exception) {
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

            val tempRetrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(tempOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
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
