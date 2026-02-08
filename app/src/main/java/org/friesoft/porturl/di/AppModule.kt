package org.friesoft.porturl.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.friesoft.porturl.AppLocaleManager
import org.friesoft.porturl.client.api.ApplicationApi
import org.friesoft.porturl.client.api.CategoryApi
import org.friesoft.porturl.client.api.ImageApi
import org.friesoft.porturl.client.api.UserApi
import org.friesoft.porturl.data.auth.AuthInterceptor
import org.friesoft.porturl.data.auth.AuthStateManager
import org.friesoft.porturl.data.repository.SettingsRepository
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

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
        settingsRepository: SettingsRepository,
        json: Json
    ): Retrofit {
        val backendUrl = settingsRepository.getBackendUrlBlocking()
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
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
        settingsRepository: SettingsRepository,
        json: Json
    ): Retrofit {
        val backendUrl = settingsRepository.getBackendUrlBlocking()
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Singleton
    @Provides
    fun provideApplicationApi(@Named("authenticated_retrofit") retrofit: Retrofit): ApplicationApi =
        retrofit.create(ApplicationApi::class.java)

    @Singleton
    @Provides
    fun provideCategoryApi(@Named("authenticated_retrofit") retrofit: Retrofit): CategoryApi =
        retrofit.create(CategoryApi::class.java)

    @Singleton
    @Provides
    fun provideUserApi(@Named("authenticated_retrofit") retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Singleton
    @Provides
    fun provideImageApi(@Named("authenticated_retrofit") retrofit: Retrofit): ImageApi =
        retrofit.create(ImageApi::class.java)

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAppLocaleManager(
        @ApplicationContext context: Context
    ): AppLocaleManager = AppLocaleManager(context)

    @Provides
    @Singleton
    fun provideAuthStateManager(context: Context): AuthStateManager {
        return AuthStateManager.getInstance(context)
    }

    @Provides
    fun provideAuthState(authStateManager: AuthStateManager): AuthState {
        return authStateManager.current
    }
}