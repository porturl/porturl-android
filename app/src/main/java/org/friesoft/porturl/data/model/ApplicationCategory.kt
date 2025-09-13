package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class ApplicationCategory(
    @SerializedName("category") val category: Category?,
    @SerializedName("sortOrder") val sortOrder: Int
)
