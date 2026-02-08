package org.friesoft.porturl.data.repository

import org.friesoft.porturl.client.api.ApplicationApi
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.ApplicationUpdateRequest
import kotlin.collections.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepository @Inject constructor(
    private val applicationApi: ApplicationApi
) {

    suspend fun getAllApplications(): List<Application> = 
        applicationApi.getVisibleApplications().body()?.mapNotNull { it.application } ?: emptyList()

    suspend fun getAllApplicationsWithRoles(): List<org.friesoft.porturl.client.model.ApplicationWithRolesDto> =
        applicationApi.getVisibleApplications().body() ?: emptyList()

    suspend fun getApplicationById(id: Long): Application = 
        applicationApi.findOneApplication(id).body() ?: throw Exception("Application not found")

    suspend fun createApplication(application: org.friesoft.porturl.client.model.ApplicationCreateRequest): Application = 
        applicationApi.createApplication(application).body() ?: throw Exception("Failed to create application")

    suspend fun updateApplication(id: Long, application: ApplicationUpdateRequest): Application = 
        applicationApi.updateApplication(id, application).body() ?: throw Exception("Failed to update application")

    suspend fun deleteApplication(id: Long) {
        applicationApi.deleteApplication(id)
    }

    suspend fun reorderApplications(categories: List<org.friesoft.porturl.client.model.Category>) {
        if (categories.isNotEmpty()) {
            applicationApi.reorderApplications(categories)
        }
    }

    suspend fun getApplicationRoles(appId: Long): List<String> = 
        applicationApi.getApplicationRoles(appId).body() ?: emptyList()
}

