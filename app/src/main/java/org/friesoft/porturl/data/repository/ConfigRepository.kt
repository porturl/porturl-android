package org.friesoft.porturl.data.repository

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

// Data class to parse the JSON response from /actuator/info
data class AppConfig(
    @SerializedName("auth") val auth: AuthInfo
)

data class AuthInfo(
    @SerializedName("issuer-uri") val issuerUri: String
)

// A simple interface for the config service
interface ConfigService {
    @GET("actuator/info")
    suspend fun getAppConfig(): AppConfig
}

@Singleton
class ConfigRepository @Inject constructor(
    // It's important this Retrofit instance does NOT have the AuthInterceptor
    @Named("unauthenticated_retrofit") private val retrofit: Retrofit
) {
    // Lazy create the service
    private val configService: ConfigService by lazy {
        retrofit.create(ConfigService::class.java)
    }

    suspend fun getIssuerUri(): String {
        return configService.getAppConfig().auth.issuerUri
    }
}
