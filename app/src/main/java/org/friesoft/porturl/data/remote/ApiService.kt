package org.friesoft.porturl.data.remote

import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.Category
import retrofit2.http.*

/**
 * A Retrofit interface that defines the HTTP API for the PortURL backend.
 */
interface ApiService {
    @GET("api/applications")
    suspend fun getAllApplications(): List<Application>

    @GET("api/applications/{id}")
    suspend fun getApplicationById(@Path("id") id: Long): Application

    @POST("api/applications")
    suspend fun createApplication(@Body application: Application): Application

    @PUT("api/applications/{id}")
    suspend fun updateApplication(@Path("id") id: Long, @Body application: Application): Application

    @DELETE("api/applications/{id}")
    suspend fun deleteApplication(@Path("id") id: Long)

    @GET("api/categories")
    suspend fun getAllCategories(): List<Category>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Category;

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: Category): Category

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)

    // TODO: A new endpoint to update the sort order of multiple categories in one call would be efficient,
    // but for now, we will call the standard update endpoint for each reordered category.

}
