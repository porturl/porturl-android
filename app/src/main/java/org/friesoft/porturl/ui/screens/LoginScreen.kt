package org.friesoft.porturl.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.AuthViewModel

/**
 * The login screen of the application.
 *
 * This screen is the entry point for unauthenticated users. It provides options
 * to log in via Keycloak or to configure the application's backend settings.
 * It observes the authentication state from the [AuthViewModel] and automatically
 * navigates to the application list upon successful login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = hiltViewModel()) {
    // Get the current activity context, required for launching the auth intent.
    val context = LocalContext.current as ComponentActivity
    val authState by authViewModel.authState.collectAsState()

    // Create an ActivityResultLauncher to handle the result from the AppAuth activity.
    // When the Keycloak login flow completes, it returns to the app here.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            // If the result contains data, pass it to the ViewModel to handle the auth response.
            authViewModel.handleAuthorizationResponse(data)
        }
    }

    // This effect runs whenever the authState changes.
    // If the user becomes authorized, it navigates them to the main app screen.
    LaunchedEffect(authState) {
        if (authState.isAuthorized) {
            navController.navigate(Routes.APP_LIST) {
                // Clear the back stack up to the login screen to prevent the user
                // from navigating back to it with the back button.
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PortURL Login") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to PortURL", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { authViewModel.login(launcher) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login with Keycloak")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { navController.navigate(Routes.SETTINGS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configure Backend")
            }
        }
    }
}
