package org.friesoft.porturl.data.repository

import org.friesoft.porturl.client.api.ApplicationApi
import org.friesoft.porturl.client.api.UserApi
import org.friesoft.porturl.client.model.User
import org.friesoft.porturl.client.model.UserUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val applicationApi: ApplicationApi
) {
    suspend fun getCurrentUser(): User {
        return userApi.getCurrentUser().body() ?: throw Exception("User not found")
    }

    suspend fun updateCurrentUser(image: String?): User {
        return userApi.updateCurrentUser(UserUpdateRequest(image)).body() ?: throw Exception("Failed to update user")
    }

    suspend fun getAllUsers(): List<User> {
        return userApi.getAllUsers().body() ?: emptyList()
    }

    suspend fun getCurrentUserRoles(): List<String> {
        return userApi.getCurrentUserRoles().body() ?: emptyList()
    }

    suspend fun getUserRoles(userId: Long): List<String> {
        return userApi.getUserRoles(userId).body() ?: emptyList()
    }

    suspend fun assignRole(appId: Long, userId: Long, role: String) {
        applicationApi.assignRoleToUser(appId, userId, role)
    }

    suspend fun unassignRole(appId: Long, userId: Long, role: String) {
        applicationApi.removeRoleFromUser(appId, userId, role)
    }
}
