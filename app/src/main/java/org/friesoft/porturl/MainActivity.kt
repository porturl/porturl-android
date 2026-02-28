package org.friesoft.porturl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.friesoft.porturl.ui.screens.AppNavigation
import org.friesoft.porturl.ui.theme.PortUrlTheme
import org.friesoft.porturl.viewmodels.SettingsViewModel
import org.friesoft.porturl.data.auth.IsolatedAuthManager
import javax.inject.Inject

import androidx.activity.enableEdgeToEdge

// @AndroidEntryPoint enables member injection in this Activity.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var isolatedAuthManager: IsolatedAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep the splash screen visible until user preferences are loaded from DataStore.
        // This prevents the "flash" of default colors before user settings are applied.
        splashScreen.setKeepOnScreenCondition {
            viewModel.userPreferences.value == null
        }
        
        enableEdgeToEdge()
        setContent {
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            
            userPreferences?.let { prefs ->
                PortUrlTheme(userPreferences = prefs) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // This is the main entry point for the UI.
                        AppNavigation(isolatedAuthManager)
                    }
                }
            }
        }
    }
}
