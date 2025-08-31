package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.friesoft.porturl.viewmodels.SettingsViewModel
import androidx.compose.material3.MaterialTheme

/**
 * A screen that allows the user to configure the application's settings,
 * such as the backend and Keycloak URLs.
 *
 * It uses the [SettingsViewModel] to persist the settings to DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    // Collect the persisted URLs from the ViewModel as state.
    val backendUrl by viewModel.backendUrl.collectAsState(initial = "")
    val keycloakUrl by viewModel.keycloakUrl.collectAsState(initial = "")
    val snackbarHostState = remember { SnackbarHostState() }

    // This effect listens for messages from the ViewModel to show a snackbar (e.g., "Settings Saved!").
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuration") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Use local mutable state for text fields to prevent recomposing the whole
            // screen on every key press. `remember(backendUrl)` ensures it updates if the
            // underlying datastore value changes.
            var currentBackendUrl by remember(backendUrl) { mutableStateOf(backendUrl) }
            var currentKeycloakUrl by remember(keycloakUrl) { mutableStateOf(keycloakUrl) }

            OutlinedTextField(
                value = currentBackendUrl,
                onValueChange = { currentBackendUrl = it },
                label = { Text("Backend Base URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., http://10.0.2.2:8080") }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Use http://10.0.2.2 for a local server running on your host machine when using an Android Emulator.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = currentKeycloakUrl,
                onValueChange = { currentKeycloakUrl = it },
                label = { Text("Keycloak Issuer URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., https://sso.<your-domain>.net/auth/realms/<your-realm>") }
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.saveSettings(currentBackendUrl, currentKeycloakUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
