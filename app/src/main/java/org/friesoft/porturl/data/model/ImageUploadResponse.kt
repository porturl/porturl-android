package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

/**
 * A simple data class to parse the JSON response from the image upload endpoint.
 */
data class ImageUploadResponse(
    @SerializedName("filename") val filename: String
)
