package org.friesoft.porturl.data.remote

import okhttp3.MultipartBody
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.ApplicationResponse
import org.friesoft.porturl.data.model.ApplicationUpdateRequest
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.data.model.ImageUploadResponse
import org.friesoft.porturl.data.model.User
import retrofit2.http.*

/**
 * A Retrofit interface that defines the HTTP API for the PortURL backend.
 */
interface ApiService {
    @GET("api/applications")
    suspend fun getAllApplications(): List<ApplicationResponse>

    @GET("api/applications/{id}")
    suspend fun getApplicationById(@Path("id") id: Long): Application

    @POST("api/applications")
    suspend fun createApplication(@Body application: Application): Application

    @PUT("api/applications/{id}")
    suspend fun updateApplication(@Path("id") id: Long, @Body application: ApplicationUpdateRequest): Application

    @DELETE("api/applications/{id}")
    suspend fun deleteApplication(@Path("id") id: Long)

    /**
     * A new endpoint for efficiently sending a batch of updated applications
     * after a reorder operation.
     */
    @POST("api/applications/reorder")
    suspend fun reorderApplications(@Body applications: List<Application>)

    @GET("api/categories")
    suspend fun getAllCategories(): List<Category>

    @POST("api/categories")
    suspend fun createCategory(@Body category: Category): Category

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Category

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: Category): Category

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)

    @POST("api/categories/reorder")
    suspend fun reorderCategories(@Body categories: List<Category>)

    /**
     * endpoint for uploading an image file using a multipart request.
     * @param file The image file part.
     * @return A response containing the unique filename assigned by the backend.
     */
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ImageUploadResponse

    // --- User Management & Roles ---

    @GET("api/users/current")
    suspend fun getCurrentUser(): User

    @PATCH("api/users/current")
    suspend fun updateCurrentUser(@Body request: org.friesoft.porturl.data.model.UserUpdateRequest): User

    @GET("api/users")
    suspend fun getAllUsers(): List<User>

    @GET("api/users/roles")
    suspend fun getCurrentUserRoles(): List<String>

    @GET("api/users/{id}/roles")
    suspend fun getUserRoles(@Path("id") id: Long): List<String>

    @POST("api/applications/{appId}/assign/{userId}/{role}")
    suspend fun assignRole(
        @Path("appId") appId: Long,
        @Path("userId") userId: Long,
        @Path("role") role: String
    )

    @POST("api/applications/{appId}/unassign/{userId}/{role}")
    suspend fun unassignRole(
        @Path("appId") appId: Long,
        @Path("userId") userId: Long,
        @Path("role") role: String
    )

    @GET("api/applications/{id}/roles")
    suspend fun getApplicationRoles(@Path("id") appId: Long): List<String>
}
