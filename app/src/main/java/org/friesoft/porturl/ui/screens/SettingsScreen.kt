package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.viewmodels.SettingsViewModel
import org.friesoft.porturl.viewmodels.ValidationState

/**
 * A screen that allows the user to configure the application's settings,
 * such as the backend URL.
 *
 * It uses the [SettingsViewModel] to persist the settings to DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    val backendUrl by viewModel.backendUrl.collectAsStateWithLifecycle(initialValue = "")
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentBackendUrl by remember(backendUrl) { mutableStateOf(backendUrl) }

    // Listen for messages from the ViewModel and show them in a Snackbar
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
            // After showing the message, reset the state so the user can try again
            viewModel.resetValidationState()
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
            OutlinedTextField(
                value = currentBackendUrl,
                onValueChange = { currentBackendUrl = it },
                label = { Text("Backend Base URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., http://10.0.2.2:8080") },
                isError = validationState == ValidationState.ERROR,
                // Disable the field while validation is in progress
                enabled = validationState != ValidationState.LOADING
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Use http://10.0.2.2 for a local server on your host machine when using an Android Emulator.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.saveAndValidateBackendUrl(currentBackendUrl) },
                modifier = Modifier.fillMaxWidth(),
                // Disable the button while loading
                enabled = validationState != ValidationState.LOADING
            ) {
                if (validationState == ValidationState.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save and Validate")
                }
            }
        }
    }
}

