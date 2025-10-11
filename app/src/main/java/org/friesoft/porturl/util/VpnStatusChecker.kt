package org.friesoft.porturl.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VpnStatusChecker @Inject constructor(@ApplicationContext private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun isVpnCheckRequired(whitelistedSsids: Set<String>): Boolean {
        val currentSsid = getCurrentSsid()
        return currentSsid == null || !whitelistedSsids.contains(currentSsid)
    }

    fun isVpnActive(vpnProfileName: String?): Boolean {
        if (vpnProfileName.isNullOrBlank()) {
            return false
        }

        val activeNetwork: Network? = connectivityManager.activeNetwork
        val networkCapabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(activeNetwork)

        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }

    fun getCurrentSsid(): String? {
        return if (wifiManager.isWifiEnabled) {
            val wifiInfo = wifiManager.connectionInfo
            wifiInfo.ssid.removeSurrounding("\"")
        } else {
            null
        }
    }

    fun isConnectionLive(host: String?): Boolean {
        if (host.isNullOrBlank()) {
            return true // If no host is configured, we can't check, so we assume it's live.
        }
        return try {
            val address = java.net.InetAddress.getByName(host)
            address.isReachable(1000)
        } catch (e: Exception) {
            false
        }
    }
}