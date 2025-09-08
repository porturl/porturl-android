package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("applicationSortMode") val applicationSortMode: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("enabled") val enabled: Boolean
)
