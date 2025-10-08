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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import org.friesoft.porturl.R
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.AuthViewModel
import org.friesoft.porturl.viewmodels.EditModeViewModel

/**
 * The main navigation component for the application.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val editModeViewModel: EditModeViewModel = viewModel() // Shared ViewModel
    val showSessionExpiredDialog by authViewModel.showSessionExpiredDialog.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // This is the global observer for authentication state. It is now at the top level
    // and will always be active while the app is running.
    LaunchedEffect(authState.isAuthorized) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        // Only redirect if the user is not authorized AND they are not already on the login screen.
        if (!authState.isAuthorized && currentRoute != Routes.LOGIN && currentRoute != "auth_check") {
            navController.navigate(Routes.LOGIN) {
                // THE FIX: Pop up to the start destination of the graph, which is a stable route.
                // This is the correct and reliable way to clear the entire back stack.
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
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

    NavHost(navController = navController, startDestination = "auth_check") {
        composable("auth_check") {
            // This screen now only handles the initial navigation decision.
            InitialAuthCheckScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(Routes.APP_LIST) { ApplicationListRoute(navController = navController,
            editModeViewModel = editModeViewModel) }

        composable(
            route = "${Routes.APP_DETAIL}/{${Routes.APP_ID_KEY}}",
            arguments = listOf(navArgument(Routes.APP_ID_KEY) { type = NavType.LongType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong(Routes.APP_ID_KEY) ?: -1L
            ApplicationDetailRoute(navController = navController, applicationId = appId)
        }

        composable(
            route = "${Routes.CATEGORY_DETAIL}/{${Routes.CATEGORY_ID_KEY}}",
            arguments = listOf(navArgument(Routes.CATEGORY_ID_KEY) { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong(Routes.CATEGORY_ID_KEY) ?: -1L
            CategoryDetailScreen(navController = navController, categoryId = categoryId)
        }
    }
}

/**
 * A simple screen that shows a loading indicator while it determines the correct
 * starting destination based on the user's initial authentication state.
 */
@Composable
private fun InitialAuthCheckScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // This effect runs only once to decide the very first screen.
    LaunchedEffect(Unit) {
        val isAuthorized = authViewModel.authState.first().isAuthorized
        val destination = if (isAuthorized) Routes.APP_LIST else Routes.LOGIN
        navController.navigate(destination) {
            popUpTo("auth_check") { inclusive = true }
        }
    }

    // The ongoing observer has been moved from here to the main AppNavigation composable.

    // Show a loading indicator while the check is happening.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
