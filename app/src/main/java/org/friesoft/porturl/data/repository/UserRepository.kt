package org.friesoft.porturl.data.repository

import org.friesoft.porturl.data.model.User
import org.friesoft.porturl.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllUsers(): List<User> {
        return apiService.getAllUsers()
    }

    suspend fun getCurrentUserRoles(): List<String> {
        return apiService.getCurrentUserRoles()
    }

    suspend fun getUserRoles(userId: Long): List<String> {
        return apiService.getUserRoles(userId)
    }

    suspend fun assignRole(appId: Long, userId: Long, role: String) {
        apiService.assignRole(appId, userId, role)
    }

    suspend fun unassignRole(appId: Long, userId: Long, role: String) {
        apiService.unassignRole(appId, userId, role)
    }
}
