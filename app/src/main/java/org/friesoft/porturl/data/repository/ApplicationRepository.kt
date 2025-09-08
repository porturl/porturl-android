package org.friesoft.porturl.data.repository

import kotlinx.coroutines.flow.first
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.remote.ApiService
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getAllApplications(): List<Application> = apiService.getAllApplications()
    suspend fun getApplicationById(id: Long): Application = apiService.getApplicationById(id)
    suspend fun createApplication(application: Application): Application = apiService.createApplication(application)
    suspend fun updateApplication(id: Long, application: Application): Application = apiService.updateApplication(id, application)
    suspend fun deleteApplication(id: Long) = apiService.deleteApplication(id)

    /**
     * Sends a list of updated applications to the backend's batch-update endpoint.
     */
    suspend fun reorderApplications(applications: List<Application>) {
        if (applications.isNotEmpty()) {
            apiService.reorderApplications(applications)
        }
    }
}

