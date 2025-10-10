package org.friesoft.porturl.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    val userPreferences: Flow<UserPreferences> = combine(
        themeMode,
        colorSource,
        predefinedColorName,
        customColors
    ) { themeMode, colorSource, predefinedColorName, customColors ->
        UserPreferences(themeMode, colorSource, predefinedColorName, customColors)
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
