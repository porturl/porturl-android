package org.friesoft.porturl.ui.screens

import org.friesoft.porturl.ui.components.PortUrlTopAppBar
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.friesoft.porturl.R
import org.friesoft.porturl.ui.navigation.Navigator
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
    navigator: Navigator,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val loginErrorResId by authViewModel.loginError.collectAsStateWithLifecycle()
    val isBackendUrlValid by authViewModel.isBackendUrlValid.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
            navigator.navigate(Routes.AppList)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PortUrlTopAppBar(
                title = { Text(stringResource(id = R.string.login_title)) },
                actions = {
                    IconButton(onClick = { navigator.navigate(Routes.Settings) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(id = R.string.settings_description)
                        )
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.login_welcome), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            // A prominent error card is now displayed if the login fails.
            AnimatedVisibility(visible = loginErrorResId != null) {
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
                            text = stringResource(id = loginErrorResId!!),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { authViewModel.clearLoginError() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(id = R.string.login_dismiss_error))
                        }
                    }
                }
            }

            if (!isBackendUrlValid) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.login_backend_not_valid),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { navigator.navigate(Routes.Settings) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(id = R.string.login_go_to_settings))
                        }
                    }
                }
            }

            Button(
                onClick = { authViewModel.login(launcher) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isBackendUrlValid
            ) {
                Text(stringResource(id = R.string.login_sso_button))
            }
        }
    }
}
