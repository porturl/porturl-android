package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

data class ApplicationResponse(
    @SerializedName("application") val application: Application,
    @SerializedName("availableRoles") val availableRoles: List<String>
)
