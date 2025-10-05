package org.friesoft.porturl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.friesoft.porturl.data.model.ColorSource
import org.friesoft.porturl.data.model.ThemeMode
import org.friesoft.porturl.data.model.UserPreferences
import org.friesoft.porturl.ui.screens.AppNavigation
import org.friesoft.porturl.ui.theme.PortUrlTheme
import org.friesoft.porturl.viewmodels.SettingsViewModel

// @AndroidEntryPoint enables member injection in this Activity.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val userPreferences by settingsViewModel.userPreferences.collectAsStateWithLifecycle(
                initialValue = UserPreferences(
                    themeMode = ThemeMode.SYSTEM,
                    colorSource = ColorSource.SYSTEM,
                    predefinedColorName = null,
                    customColors = null
                )
            )
            PortUrlTheme(userPreferences = userPreferences) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This is the main entry point for the UI.
                    AppNavigation()
                }
            }
        }
    }
}
