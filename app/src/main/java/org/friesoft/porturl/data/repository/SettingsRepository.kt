package org.friesoft.porturl.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        val KEYCLOAK_URL_KEY = stringPreferencesKey("keycloak_url")
        // Default URL for a local server accessed from the Android emulator
        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:8080" // Default if nothing is set
        const val DEFAULT_KEYCLOAK_URL = "https://sso.<your-domain>.net/auth/realms/<your-realm>"
    }

    // Flow that emits the backend URL whenever it changes
    val backendUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BACKEND_URL_KEY] ?: DEFAULT_BACKEND_URL
    }

    // Flow that emits the Keycloak URL whenever it changes
    val keycloakUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEYCLOAK_URL_KEY] ?: DEFAULT_KEYCLOAK_URL
    }

    /**
     * Provides the base URL for the backend API.
     * This is the URL that the user can configure.
     */
    fun getBaseUrl(): Flow<String> {
        return backendUrl
    }

    suspend fun saveSettings(backendUrl: String, keycloakUrl: String) {
        context.dataStore.edit { settings ->
            settings[BACKEND_URL_KEY] = backendUrl
            settings[KEYCLOAK_URL_KEY] = keycloakUrl
        }
    }
}
