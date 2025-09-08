package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class Application(
    @SerializedName("id") var id: Long?,
    @SerializedName("name") var name: String,
    @SerializedName("url") var url: String,

    // This provides access to the per-category sort order.
    @SerializedName("applicationCategories") var applicationCategories: List<ApplicationCategory>,

    // Filename identifiers (e.g., "uuid.png") for editing and saving.
    // The backend expects these in POST/PUT requests.
    @SerializedName("iconLarge") var iconLarge: String?,
    @SerializedName("iconMedium") var iconMedium: String?,
    @SerializedName("iconThumbnail") var iconThumbnail: String?,

    // Full URLs (e.g., "http://.../api/images/uuid.png") for displaying images.
    // The backend provides these in GET requests.
    @SerializedName("iconUrlLarge") var iconUrlLarge: String?,
    @SerializedName("iconUrlMedium") var iconUrlMedium: String?,
    @SerializedName("iconUrlThumbnail") var iconUrlThumbnail: String?

)