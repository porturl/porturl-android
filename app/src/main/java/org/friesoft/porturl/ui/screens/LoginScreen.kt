package org.friesoft.porturl.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.AuthViewModel

/**
 * The login screen of the application.
 *
 * This screen is the entry point for unauthenticated users. It provides options
 * to log in via SSO or to configure the application's backend settings.
 * It observes the authentication state from the [AuthViewModel] and automatically
 * navigates to the application list upon successful login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val loginError by authViewModel.loginError.collectAsStateWithLifecycle()

    // Create an ActivityResultLauncher to handle the result from the AppAuth activity.
    // When the Keycloak login flow completes, it returns to the app here.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                // If the result contains data, pass it to the ViewModel to handle the auth response.
                authViewModel.handleAuthorizationResponse(it)
            }
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

            // A prominent error card is now displayed if the login fails.
            AnimatedVisibility(visible = loginError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = loginError ?: "An unknown error occurred.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { authViewModel.clearLoginError() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }


            Button(
                onClick = { authViewModel.login(launcher) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login with SSO")
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

