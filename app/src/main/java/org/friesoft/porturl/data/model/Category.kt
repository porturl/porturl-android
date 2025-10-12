package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

/**
 * Defines the sorting behavior for applications within a category.
 * This must be kept in sync with the backend enum.
 */
enum class SortMode {
    CUSTOM,
    ALPHABETICAL
}

/**
 * Represents a category for organizing applications.
 * This data class is designed to match the backend's Category entity.
 */
data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("applicationSortMode") val applicationSortMode: SortMode = SortMode.CUSTOM,
    @SerializedName("icon") val icon: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("enabled") val enabled: Boolean = true
)
