package org.friesoft.porturl.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.friesoft.porturl.AppLocaleManager
import org.friesoft.porturl.BuildConfig
import org.friesoft.porturl.data.auth.SslUtils
import retrofit2.Converter
import java.lang.reflect.Type
import org.friesoft.porturl.client.api.ApplicationApi
import org.friesoft.porturl.client.api.CategoryApi
import org.friesoft.porturl.client.api.ImageApi
import org.friesoft.porturl.client.api.UserApi
import org.friesoft.porturl.client.api.AdminApi
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
    fun provideUnauthenticatedOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder = OkHttpClient.Builder().addInterceptor(logging)
        if (BuildConfig.DEBUG) {
            SslUtils.applySelfSignedTrust(context, builder)
        }
        return builder.build()
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
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // --- CLIENT FOR AUTHENTICATED API CALLS ---
    @Singleton
    @Provides
    @Named("authenticated_client")
    fun provideAuthenticatedOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
        if (BuildConfig.DEBUG) {
            SslUtils.applySelfSignedTrust(context, builder)
        }
        return builder.build()
    }

    @Singleton
    @Provides
    @Named("admin_client")
    fun provideAdminOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
        if (BuildConfig.DEBUG) {
            SslUtils.applySelfSignedTrust(context, builder)
        }
        return builder.build()
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
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Singleton
    @Provides
    @Named("yaml_mapper")
    fun provideYamlObjectMapper(): ObjectMapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Singleton
    @Provides
    @Named("admin_retrofit")
    fun provideAdminRetrofit(
        @Named("admin_client") okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository,
        @Named("yaml_mapper") yamlMapper: ObjectMapper,
        json: Json
    ): Retrofit {
        val backendUrl = settingsRepository.getBackendUrlBlocking()
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(YamlConverterFactory(yamlMapper))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    private class YamlConverterFactory(private val mapper: ObjectMapper) : Converter.Factory() {
        private val mediaType = "application/x-yaml".toMediaType()

        override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<out Annotation>,
            methodAnnotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<*, RequestBody>? {
            if (type != org.friesoft.porturl.client.model.ExportData::class.java) {
                return null
            }
            val javaType = mapper.typeFactory.constructType(type)
            val writer = mapper.writerFor(javaType)
            return Converter<Any, RequestBody> { value ->
                val bytes = writer.writeValueAsBytes(value)
                bytes.toRequestBody(mediaType)
            }
        }

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<ResponseBody, *>? {
            if (type != org.friesoft.porturl.client.model.ExportData::class.java) {
                return null
            }
            val javaType: JavaType = mapper.typeFactory.constructType(type)
            val reader = mapper.readerFor(javaType)
            return Converter<ResponseBody, Any> { value ->
                value.use { reader.readValue<Any>(it.byteStream()) }
            }
        }
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

    @Singleton
    @Provides
    fun provideAdminApi(@Named("admin_retrofit") retrofit: Retrofit): AdminApi =
        retrofit.create(AdminApi::class.java)

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