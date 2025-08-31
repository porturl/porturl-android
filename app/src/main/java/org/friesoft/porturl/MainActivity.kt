package org.friesoft.porturl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.friesoft.porturl.ui.screens.AppNavigation
import org.friesoft.porturl.ui.theme.PortUrlTheme

// @AndroidEntryPoint enables member injection in this Activity.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PortUrlTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // This is the main entry point for the UI.
                    AppNavigation()
                }
            }
        }
    }
}
