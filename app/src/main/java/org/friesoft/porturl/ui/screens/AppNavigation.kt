package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.AuthViewModel

/**
 * The main navigation component for the application.
 *
 * This Composable is responsible for setting up the NavHost and defining the navigation graph.
 * It determines the starting screen based on the user's authentication state.
 */
@Composable
fun AppNavigation() {
    // Creates and remembers the navigation controller.
    val navController = rememberNavController()

    // Retrieves an instance of the AuthViewModel using Hilt.
    val authViewModel: AuthViewModel = hiltViewModel()

    // Collects the start destination from the ViewModel as a State.
    // This allows the UI to reactively update when the destination is determined.
    val startDestination by authViewModel.startDestination.collectAsState()

    // The NavHost will only be composed if the startDestination is known.
    // While it's empty, a loading indicator is shown. This prevents a "flicker"
    // where the login screen might briefly appear for an already authenticated user.
    if (startDestination.isNotEmpty()) {
        NavHost(navController = navController, startDestination = startDestination) {

            // Defines the route for the Login screen.
            composable(Routes.LOGIN) {
                LoginScreen(navController)
            }

            // Defines the route for the Settings screen.
            composable(Routes.SETTINGS) {
                SettingsScreen(navController)
            }

            // Defines the route for the main Application List screen.
            composable(Routes.APP_LIST) {
                ApplicationListScreen(navController)
            }

            // Defines the route for the Application Detail screen.
            // This route includes a mandatory 'appId' argument of type Long.
            composable(
                route = "${Routes.APP_DETAIL}/{${Routes.APP_ID_KEY}}",
                arguments = listOf(navArgument(Routes.APP_ID_KEY) { type = NavType.LongType })
            ) { backStackEntry ->
                // Extracts the 'appId' from the navigation arguments.
                // A value of -1 is used as a sentinel to indicate a "new application" screen.
                val appId = backStackEntry.arguments?.getLong(Routes.APP_ID_KEY) ?: -1L
                ApplicationDetailScreen(navController = navController, applicationId = appId)
            }
        }
    } else {
        // Show a centered loading indicator while the initial auth state is being checked.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}