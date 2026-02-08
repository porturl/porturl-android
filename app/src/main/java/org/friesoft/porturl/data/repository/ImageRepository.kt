package org.friesoft.porturl.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.friesoft.porturl.client.api.ImageApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val imageApi: ImageApi,
    @param:ApplicationContext private val context: Context
) {
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
        return imageApi.uploadImage(filePart).body()?.filename
    }
}
