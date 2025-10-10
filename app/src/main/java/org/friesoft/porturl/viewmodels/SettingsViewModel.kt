package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.AppLocaleManager
import org.friesoft.porturl.Language
import org.friesoft.porturl.appLanguages
import org.friesoft.porturl.data.model.ColorSource
import org.friesoft.porturl.data.model.CustomColors
import org.friesoft.porturl.data.model.ThemeMode
import org.friesoft.porturl.data.model.UserPreferences
import org.friesoft.porturl.data.repository.ConfigRepository
import org.friesoft.porturl.data.repository.SettingsRepository
import javax.inject.Inject

// Represents the different states of the URL validation process
enum class ValidationState {
    IDLE, LOADING, SUCCESS, ERROR
}

/**
 * ViewModel for the SettingsScreen.
 *
 * This ViewModel interacts with the [SettingsRepository] to fetch and save
 * application settings. It provides Flows of the settings data for the UI to observe
 * and a method to update them.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val configRepository: ConfigRepository,
    private val appLocaleManager: AppLocaleManager
) : ViewModel() {

    /**
     * A Flow that emits the current backend URL string. The UI collects this Flow to
     * display the currently saved backend URL.
     */
    val backendUrl: Flow<String> = settingsRepository.backendUrl
    val userPreferences: Flow<UserPreferences> = settingsRepository.userPreferences

    /**
     * A SharedFlow to emit one-time events to the UI, such as showing a Snackbar
     * message. This is used to inform the user that settings have been saved.
     * Used to send one-off messages to the UI (e.g., for Snackbars)
     */
    val userMessage = MutableSharedFlow<String>()

    private val _validationState = MutableStateFlow(ValidationState.IDLE)
    val validationState = _validationState.asStateFlow()

    private val _settingState = MutableStateFlow(SettingState())
    val settingState: StateFlow<SettingState> = _settingState.asStateFlow()

    init {
        loadInitialLanguage()
    }

    private fun loadInitialLanguage() {
        val currentLanguage = appLocaleManager.getLanguageCode()
        _settingState.value = SettingState(
            selectedLanguage = currentLanguage,
            availableLanguages = appLanguages
        )
    }

    fun changeLanguage(languageCode: String) {
        appLocaleManager.changeLanguage(languageCode)
        _settingState.value = _settingState.value.copy(selectedLanguage = languageCode)
    }

    /**
     * Saves the provided backend and Keycloak URLs to the DataStore via the repository.
     * After saving, it emits a confirmation message to the [toastMessage] flow.
     *
     * @param backendUrl The new backend URL to save.
     */
    fun saveAndValidateBackendUrl(url: String) {
        viewModelScope.launch {
            _validationState.value = ValidationState.LOADING
            // Attempt to validate the new URL
            if (configRepository.validateBackendUrl(url)) {
                // If valid, save it to persistent storage
                settingsRepository.saveBackendUrl(url)
                _validationState.value = ValidationState.SUCCESS
            } else {
                // If invalid, report an error and do not save
                _validationState.value = ValidationState.ERROR
                userMessage.emit("Could not connect to the provided URL.")
            }
        }
    }

    // Resets the validation state, for example, after the user dismisses an error.
    fun resetValidationState() {
        _validationState.value = ValidationState.IDLE
    }

    fun saveThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.saveThemeMode(themeMode)
        }
    }

    fun saveColorSource(colorSource: ColorSource) {
        viewModelScope.launch {
            settingsRepository.saveColorSource(colorSource)
        }
    }

    fun savePredefinedColorName(colorName: String) {
        viewModelScope.launch {
            settingsRepository.savePredefinedColorName(colorName)
        }
    }

    fun saveCustomColors(customColors: CustomColors) {
        viewModelScope.launch {
            settingsRepository.saveCustomColors(customColors)
        }
    }
}

data class SettingState(
    val selectedLanguage: String = "",
    val availableLanguages: List<Language> = emptyList()
)
