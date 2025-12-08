package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.first
import org.friesoft.porturl.R
import org.friesoft.porturl.ui.navigation.NavigationState
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.ui.navigation.rememberNavigationState
import org.friesoft.porturl.ui.navigation.toEntries
import org.friesoft.porturl.viewmodels.AppSharedViewModel
import org.friesoft.porturl.viewmodels.AuthViewModel

/**
 * The main navigation component for the application.
 */
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Determine start route dynamically?
    // Ideally, we want the start route to be AppList if authorized, Login if not.
    // Nav 3 rememberNavigationState takes a fixed startRoute.
    // We will use AppList as the canonical "Home".
    // If not authorized, we navigate to Login on top.

    val navigationState = rememberNavigationState(
        startRoute = Routes.AppList,
        topLevelRoutes = setOf(Routes.Login, Routes.AppList)
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val sharedViewModel: AppSharedViewModel = viewModel() // Shared ViewModel for app-wide state
    val showSessionExpiredDialog by authViewModel.showSessionExpiredDialog.collectAsStateWithLifecycle()

    val entryProvider = entryProvider {
        // We no longer need AuthCheck route, logic is handled below
        entry<Routes.Login> {
            LoginScreen(navigator)
        }
        entry<Routes.Settings> {
            SettingsScreen(navigator)
        }
        entry<Routes.AppList> {
            ApplicationListRoute(navigator = navigator, sharedViewModel = sharedViewModel)
        }
        entry<Routes.AppDetail> { key ->
            ApplicationDetailRoute(navigator = navigator, applicationId = key.appId, sharedViewModel = sharedViewModel)
        }
        entry<Routes.CategoryDetail> { key ->
            CategoryDetailScreen(navigator = navigator, categoryId = key.categoryId, sharedViewModel = sharedViewModel)
        }
        entry<Routes.UserList> {
            UserListScreen(navigator = navigator)
        }
        entry<Routes.UserDetail> { key ->
            UserDetailScreen(navigator = navigator, userId = key.userId)
        }
    }

    // Initial Auth Check and Redirection
    LaunchedEffect(Unit) {
        val isAuthorized = authViewModel.authState.first().isAuthorized
        if (!isAuthorized) {
            navigator.navigate(Routes.Login)
        }
    }

    // Continuous Auth Check
    LaunchedEffect(authState.isAuthorized) {
        if (!authState.isAuthorized) {
             val currentRoute = navigationState.topLevelRoute
             if (currentRoute != Routes.Login) {
                 navigator.navigate(Routes.Login)
             }
        }
    }

    if (showSessionExpiredDialog) {
        AlertDialog(
            onDismissRequest = { authViewModel.onSessionExpiredDialogDismissed() },
            title = { Text(stringResource(id = R.string.session_expired_dialog_title)) },
            text = { Text(stringResource(id = R.string.session_expired_dialog_text)) },
            confirmButton = {
                TextButton(onClick = { authViewModel.onSessionExpiredDialogDismissed() }) {
                    Text(stringResource(id = R.string.ok))
                }
            }
        )
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
