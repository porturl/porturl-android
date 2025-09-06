package org.friesoft.porturl.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
        // Default URL for a local server accessed from the Android emulator
        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:8080" // Default if nothing is set
    }

    // Flow that emits the backend URL whenever it changes
    val backendUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BACKEND_URL_KEY] ?: DEFAULT_BACKEND_URL
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
