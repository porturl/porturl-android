package org.friesoft.porturl.data.model

data class VpnPreferences(
    val vpnCheckEnabled: Boolean,
    val vpnProfileName: String?,
    val livenessCheckEnabled: Boolean,
    val livenessCheckHost: String?,
    val wifiWhitelist: Set<String>
)