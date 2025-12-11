package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class UserUpdateRequest(
    @SerializedName("image") val image: String?
)
