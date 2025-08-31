package org.friesoft.porturl.data.repository

import kotlinx.coroutines.flow.first
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.remote.ApiService
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepository @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: okhttp3.OkHttpClient
) {
    // We create the ApiService lazily, only when it's first needed.
    // This ensures we have the backendUrl before building Retrofit.
    private val apiService: ApiService by lazy {
        // This is a blocking call, but it's safe inside lazy initialization
        // which will be triggered by a coroutine in the ViewModel.
        val backendUrl = kotlinx.coroutines.runBlocking { settingsRepository.backendUrl.first() }
        retrofitBuilder
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    suspend fun getAllApplications(): List<Application> = apiService.getAllApplications()
    suspend fun getApplicationById(id: Long): Application = apiService.getApplicationById(id)
    suspend fun createApplication(application: Application): Application = apiService.createApplication(application)
    suspend fun updateApplication(id: Long, application: Application): Application = apiService.updateApplication(id, application)
    suspend fun deleteApplication(id: Long) = apiService.deleteApplication(id)
}

