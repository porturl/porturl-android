package org.friesoft.porturl.data.repository

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.remote.ApiService
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class ApplicationRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context // Inject context for file operations
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

    /**
     * Uploads an image from a given URI to the backend.
     * @param imageUri The content URI of the image to upload.
     * @return The unique filename returned by the backend upon successful upload, or null on failure.
     */
    suspend fun uploadImage(imageUri: Uri): String? {
        val contentResolver = context.contentResolver
        // Open an input stream to the image file
        val inputStream = contentResolver.openInputStream(imageUri) ?: return null

        // Create a request body from the file's bytes
        val requestBody = inputStream.readBytes().toRequestBody(
            contentResolver.getType(imageUri)?.toMediaTypeOrNull()
        )

        // Create the multipart form data part
        val filePart = MultipartBody.Part.createFormData(
            "file",
            "upload.png", // The filename doesn't matter as the backend generates a UUID
            requestBody
        )

        // Call the API and return the filename from the response
        return apiService.uploadImage(filePart).filename
    }
}

