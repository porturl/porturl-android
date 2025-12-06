package org.friesoft.porturl.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for the /api/applications endpoint.
 * This matches the backend's ApplicationWithRolesDto structure.
 */
data class ApplicationWithRolesDto(
    @SerializedName("application") val application: Application,
    @SerializedName("availableRoles") val availableRoles: List<String>
)
