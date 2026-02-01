package org.friesoft.porturl.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.first
import org.friesoft.porturl.R
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.ui.navigation.rememberNavigationState
import org.friesoft.porturl.ui.navigation.toEntries
import org.friesoft.porturl.viewmodels.AppSharedViewModel
import org.friesoft.porturl.viewmodels.AuthViewModel
import android.util.Log
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi

/**
 * The main navigation component for the application.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
        topLevelRoutes = setOf(Routes.Login, Routes.AppList, Routes.Settings, Routes.UserList)
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val sharedViewModel: AppSharedViewModel = viewModel() // Shared ViewModel for app-wide state
    val showSessionExpiredDialog by authViewModel.showSessionExpiredDialog.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin by authViewModel.isAdmin.collectAsStateWithLifecycle()
    val backendUrl by hiltViewModel<org.friesoft.porturl.viewmodels.SettingsViewModel>().backendUrl.collectAsStateWithLifecycle(
        initialValue = org.friesoft.porturl.data.repository.SettingsRepository.DEFAULT_BACKEND_URL
    )
    
    val searchQuery by sharedViewModel.searchQuery.collectAsStateWithLifecycle()

    val activity = androidx.activity.compose.LocalActivity.current!!
    val windowSizeClass = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(activity).widthSizeClass

    var showProfileDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    val logoutLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {}
    
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { authViewModel.updateUserImage(it) }
    }

    val entryProvider = entryProvider {
        // We no longer need AuthCheck route, logic is handled below
        entry<Routes.Login> {
            LoginScreen(navigator, authViewModel = authViewModel)
        }
        entry<Routes.Settings> {
            Log.d("AppNavigation", "Composing SettingsScreen")
            SettingsScreen(navigator)
        }
        entry<Routes.AppList> {
            ApplicationListRoute(navigator = navigator, sharedViewModel = sharedViewModel, authViewModel = authViewModel)
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

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text(stringResource(R.string.user_profile)) }, // Reuse description as title
            text = {
                androidx.compose.foundation.layout.Column {
                     TextButton(onClick = {
                         showProfileDialog = false
                         imagePickerLauncher.launch("image/*")
                     }) {
                         Text(stringResource(R.string.edit_button_text)) // Reuse edit text
                     }
                     TextButton(onClick = {
                         showProfileDialog = false
                         authViewModel.logout(logoutLauncher)
                     }) {
                         Text(stringResource(R.string.logout_description))
                     }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    org.friesoft.porturl.ui.components.AdaptiveNavigationShell(
        windowSizeClass = windowSizeClass,
        currentRoute = navigationState.topLevelRoute,
        onNavigate = { route -> navigator.navigate(route) },
        currentUser = currentUser,
        isAdmin = isAdmin,
        backendUrl = backendUrl,
        searchQuery = searchQuery,
        onSearchQueryChanged = { sharedViewModel.updateSearchQuery(it) },
        onProfileClick = { showProfileDialog = true },
        onAddApp = { navigator.navigate(Routes.AppDetail(-1)) },
        onAddCategory = { navigator.navigate(Routes.CategoryDetail(-1)) }
    ) {
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() }
        )
    }
}
