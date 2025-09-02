// FILE: app/src/main/java/org/friesoft/porturl/di/AppModule.kt

package org.friesoft.porturl.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.friesoft.porturl.data.auth.AuthInterceptor
import org.friesoft.porturl.data.remote.ApiService
import org.friesoft.porturl.data.repository.SettingsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- CLIENT FOR UNAUTHENTICATED CALLS (e.g., fetching config) ---
    @Singleton
    @Provides
    @Named("unauthenticated_client")
    fun provideUnauthenticatedOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder().addInterceptor(logging).build()
    }

    @Singleton
    @Provides
    @Named("unauthenticated_retrofit")
    fun provideUnauthenticatedRetrofit(
        @Named("unauthenticated_client") okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        val backendUrl = settingsRepository.getBackendUrlBlocking() // A helper might be needed here
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- CLIENT FOR AUTHENTICATED API CALLS ---
    @Singleton
    @Provides
    @Named("authenticated_client")
    fun provideAuthenticatedOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Singleton
    @Provides
    @Named("authenticated_retrofit")
    fun provideAuthenticatedRetrofit(
        @Named("authenticated_client") okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        val backendUrl = settingsRepository.getBackendUrlBlocking()
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the final ApiService instance for the rest of the app to use.
     * This service is guaranteed to be built with the authenticated Retrofit client.
     */
    @Singleton
    @Provides
    fun provideApiService(@Named("authenticated_retrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}