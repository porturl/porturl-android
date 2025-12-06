package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class ApplicationCategoryId(
    @SerializedName("applicationId") val applicationId: Long,
    @SerializedName("categoryId") val categoryId: Long
)

data class ApplicationCategory(
    @SerializedName("id") val id: ApplicationCategoryId,
    @SerializedName("category") val category: Category,
    @SerializedName("sortOrder") val sortOrder: Int
)
