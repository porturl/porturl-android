package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class Application(
    @SerializedName("id") var id: Long?,
    @SerializedName("name") var name: String,
    @SerializedName("url") var url: String
)