package org.friesoft.porturlmobile.network

import org.friesoft.porturlmobile.model.Application
import retrofit2.Call
import retrofit2.http.*

interface ApplicationApi {
    @GET("applications")
    fun getApplications(): Call<List<Application>>

    @GET("applications/{id}")
    fun getApplication(@Path("id") id: Long): Call<Application>

    @POST("applications")
    fun createApplication(@Body app: Application): Call<Application>

    @PUT("applications/{id}")
    fun updateApplication(@Path("id") id: Long, @Body app: Application): Call<Application>

    @DELETE("applications/{id}")
    fun deleteApplication(@Path("id") id: Long): Call<Void>
}