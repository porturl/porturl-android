package org.friesoft.porturl.data.remote

import org.friesoft.porturl.data.model.Application
import retrofit2.http.*

/**
 * A Retrofit interface that defines the HTTP API for the PortURL backend.
 */
interface ApiService {
    @GET("applications")
    suspend fun getAllApplications(): List<Application>

    @GET("applications/{id}")
    suspend fun getApplicationById(@Path("id") id: Long): Application

    @POST("applications")
    suspend fun createApplication(@Body application: Application): Application

    @PUT("applications/{id}")
    suspend fun updateApplication(@Path("id") id: Long, @Body application: Application): Application

    @DELETE("applications/{id}")
    suspend fun deleteApplication(@Path("id") id: Long)
}
