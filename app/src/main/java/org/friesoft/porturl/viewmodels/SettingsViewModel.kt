package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.repository.SettingsRepository
import javax.inject.Inject

/**
 * ViewModel for the SettingsScreen.
 *
 * This ViewModel interacts with the [SettingsRepository] to fetch and save
 * application settings. It provides Flows of the settings data for the UI to observe
 * and a method to update them.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {

    /**
     * A Flow that emits the current backend URL string. The UI collects this Flow to
     * display the currently saved backend URL.
     */
    val backendUrl: Flow<String> = settingsRepository.backendUrl

    /**
     * A Flow that emits the current Keycloak issuer URL string. The UI collects this Flow
     * to display the currently saved Keycloak URL.
     */
    val keycloakUrl: Flow<String> = settingsRepository.keycloakUrl

    /**
     * A SharedFlow to emit one-time events to the UI, such as showing a Snackbar
     * message. This is used to inform the user that settings have been saved.
     */
    val toastMessage = MutableSharedFlow<String>()

    /**
     * Saves the provided backend and Keycloak URLs to the DataStore via the repository.
     * After saving, it emits a confirmation message to the [toastMessage] flow.
     *
     * @param backendUrl The new backend URL to save.
     * @param keycloakUrl The new Keycloak issuer URL to save.
     */
    fun saveSettings(backendUrl: String, keycloakUrl: String) {
        viewModelScope.launch {
            settingsRepository.saveSettings(backendUrl, keycloakUrl)
            toastMessage.emit("Settings Saved!")
        }
    }
}
