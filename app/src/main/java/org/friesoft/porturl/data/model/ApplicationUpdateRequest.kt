package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class ApplicationUpdateRequest(
    @SerializedName("name") var name: String,
    @SerializedName("url") var url: String,
    @SerializedName("iconLarge") var iconLarge: String?,
    @SerializedName("iconMedium") var iconMedium: String?,
    @SerializedName("iconThumbnail") var iconThumbnail: String?,
    @SerializedName("applicationCategories") var applicationCategories: List<ApplicationCategory>,
    @SerializedName("availableRoles") var availableRoles: List<String> = emptyList()
)
