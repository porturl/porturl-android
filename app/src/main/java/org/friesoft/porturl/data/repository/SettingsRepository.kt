package org.friesoft.porturl.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.friesoft.porturl.data.model.ColorSource
import org.friesoft.porturl.data.model.CustomColors
import org.friesoft.porturl.data.model.ThemeMode
import org.friesoft.porturl.data.model.UserPreferences
import org.friesoft.porturl.data.model.VpnPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user-configurable settings using Jetpack DataStore.
 */
@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    // Creates a singleton instance of DataStore
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        val BACKEND_URL_KEY = stringPreferencesKey("backend_url")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val COLOR_SOURCE_KEY = stringPreferencesKey("color_source")
        val PREDEFINED_COLOR_NAME_KEY = stringPreferencesKey("predefined_color_name")
        val CUSTOM_COLORS_KEY = stringPreferencesKey("custom_colors")
        val VPN_CHECK_ENABLED_KEY = booleanPreferencesKey("vpn_check_enabled")
        val VPN_PROFILE_NAME_KEY = stringPreferencesKey("vpn_profile_name")
        val LIVENESS_CHECK_ENABLED_KEY = booleanPreferencesKey("liveness_check_enabled")
        val LIVENESS_CHECK_HOST_KEY = stringPreferencesKey("liveness_check_host")
        val WIFI_WHITELIST_KEY = stringSetPreferencesKey("wifi_whitelist")
        val VPN_APP_PACKAGE_NAME_KEY = stringPreferencesKey("vpn_app_package_name")

        // Default URL for a local server accessed from the Android emulator
        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:8080" // Default if nothing is set
    }

    // Flow that emits the backend URL whenever it changes
    val backendUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BACKEND_URL_KEY] ?: DEFAULT_BACKEND_URL
    }

    private val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.valueOf(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name)
    }

    private val colorSource: Flow<ColorSource> = context.dataStore.data.map { preferences ->
        ColorSource.valueOf(preferences[COLOR_SOURCE_KEY] ?: ColorSource.SYSTEM.name)
    }

    private val predefinedColorName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PREDEFINED_COLOR_NAME_KEY]
    }

    private val customColors: Flow<CustomColors?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_COLORS_KEY]?.let { Json.decodeFromString<CustomColors>(it) }
    }

    private val vpnCheckEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VPN_CHECK_ENABLED_KEY] ?: false
    }

    private val vpnProfileName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[VPN_PROFILE_NAME_KEY]
    }

    private val livenessCheckEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LIVENESS_CHECK_ENABLED_KEY] ?: false
    }

    private val livenessCheckHost: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LIVENESS_CHECK_HOST_KEY]
    }

    private val wifiWhitelist: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[WIFI_WHITELIST_KEY] ?: emptySet()
    }

    private val vpnAppPackageName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[VPN_APP_PACKAGE_NAME_KEY]
    }

    val userPreferences: Flow<UserPreferences> = combine(
        themeMode,
        colorSource,
        predefinedColorName,
        customColors
    ) { themeMode, colorSource, predefinedColorName, customColors ->
        UserPreferences(themeMode, colorSource, predefinedColorName, customColors)
    }

    val vpnPreferences: Flow<VpnPreferences> = combine(
        vpnCheckEnabled,
        vpnProfileName,
        livenessCheckEnabled,
        livenessCheckHost,
        wifiWhitelist,
        vpnAppPackageName
    ) { values ->
        val vpnCheckEnabled = values[0] as Boolean
        val vpnProfileName = values[1] as String?
        val livenessCheckEnabled = values[2] as Boolean
        val livenessCheckHost = values[3] as String?
        val wifiWhitelist = values[4] as Set<String>
        val vpnAppPackageName = values[5] as String?

        VpnPreferences(
            vpnCheckEnabled,
            vpnProfileName,
            livenessCheckEnabled,
            livenessCheckHost,
            wifiWhitelist,
            vpnAppPackageName
        )
    }

    /**
     * Saves the provided backend URL to the DataStore.
     *
     * @param backendUrl The new backend URL to save.
     */
    suspend fun saveBackendUrl(backendUrl: String) {
        context.dataStore.edit { settings ->
            settings[BACKEND_URL_KEY] = backendUrl
        }
    }

    suspend fun saveThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { settings ->
            settings[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun saveColorSource(colorSource: ColorSource) {
        context.dataStore.edit { settings ->
            settings[COLOR_SOURCE_KEY] = colorSource.name
        }
    }

    suspend fun savePredefinedColorName(colorName: String) {
        context.dataStore.edit { settings ->
            settings[PREDEFINED_COLOR_NAME_KEY] = colorName
        }
    }

    suspend fun saveCustomColors(customColors: CustomColors) {
        context.dataStore.edit { settings ->
            settings[CUSTOM_COLORS_KEY] = Json.encodeToString(customColors)
        }
    }

    suspend fun saveVpnCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[VPN_CHECK_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveVpnProfileName(name: String) {
        context.dataStore.edit { settings ->
            settings[VPN_PROFILE_NAME_KEY] = name
        }
    }

    suspend fun saveLivenessCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[LIVENESS_CHECK_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveLivenessCheckHost(host: String) {
        context.dataStore.edit { settings ->
            settings[LIVENESS_CHECK_HOST_KEY] = host
        }
    }

    suspend fun saveWifiWhitelist(whitelist: Set<String>) {
        context.dataStore.edit { settings ->
            settings[WIFI_WHITELIST_KEY] = whitelist
        }
    }

    suspend fun saveVpnAppPackageName(packageName: String) {
        context.dataStore.edit { settings ->
            settings[VPN_APP_PACKAGE_NAME_KEY] = packageName
        }
    }

    /**
     * A synchronous helper function to get the current backend URL.
     * This is required by the Hilt module to provide the initial Retrofit instance,
     * as Hilt's @Provides methods cannot be suspend functions.
     *
     * @return The current backend URL as a String.
     */
    fun getBackendUrlBlocking(): String = runBlocking {
        backendUrl.first()
    }

}
