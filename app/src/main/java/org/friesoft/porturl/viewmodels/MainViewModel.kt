package org.friesoft.porturl.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.repository.SettingsRepository
import org.friesoft.porturl.util.VpnStatusChecker
import javax.inject.Inject

enum class VpnStatus {
    CONNECTED,
    DISCONNECTED,
    NOT_CONFIGURED,
    WHITELISTED
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val vpnStatusChecker: VpnStatusChecker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val vpnStatus: StateFlow<VpnStatus> = combine(
        settingsRepository.vpnPreferences
    ) { (vpnPreferences) ->
        if (!vpnPreferences.vpnCheckEnabled) {
            return@combine VpnStatus.NOT_CONFIGURED
        }

        if (!vpnStatusChecker.isVpnCheckRequired(vpnPreferences.wifiWhitelist)) {
            return@combine VpnStatus.WHITELISTED
        }

        val isVpnActive = vpnStatusChecker.isVpnActive(vpnPreferences.vpnProfileName)
        val isConnectionLive = if (vpnPreferences.livenessCheckEnabled) {
            vpnStatusChecker.isConnectionLive(vpnPreferences.livenessCheckHost)
        } else {
            true
        }

        if (isVpnActive && isConnectionLive) {
            VpnStatus.CONNECTED
        } else {
            VpnStatus.DISCONNECTED
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VpnStatus.NOT_CONFIGURED
    )

    fun launchVpnApp() {
        viewModelScope.launch {
            val vpnAppPackageName = settingsRepository.vpnPreferences.first().vpnAppPackageName
            if (vpnAppPackageName != null) {
                val intent = context.packageManager.getLaunchIntentForPackage(vpnAppPackageName)
                if (intent != null) {
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        }
    }
}