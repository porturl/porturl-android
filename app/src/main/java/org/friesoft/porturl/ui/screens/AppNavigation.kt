package org.friesoft.porturl.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import org.friesoft.porturl.data.auth.IsolatedAuthManager
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * The main navigation component for the application.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigation(isolatedAuthManager: IsolatedAuthManager) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val showSessionExpiredDialog by authViewModel.showSessionExpiredDialog.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin by authViewModel.isAdmin.collectAsStateWithLifecycle()
    val backendUrl by hiltViewModel<org.friesoft.porturl.viewmodels.SettingsViewModel>().backendUrl.collectAsStateWithLifecycle(
        initialValue = org.friesoft.porturl.data.repository.SettingsRepository.DEFAULT_BACKEND_URL
    )

    val activity = androidx.activity.compose.LocalActivity.current!!
    val windowSizeClass = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(activity).widthSizeClass

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

    val userKey = (currentUser?.email ?: "anonymous") + "_" + authState.isAuthorized

    key(userKey) {
        val navigationState = rememberNavigationState(
            startRoute = Routes.AppList,
            topLevelRoutes = setOf(Routes.Login, Routes.AppList, Routes.Settings, Routes.UserList, Routes.Profile)
        )
        val navigator = remember(navigationState) { Navigator(navigationState) }

        val sharedViewModel: AppSharedViewModel = viewModel() // Shared ViewModel for app-wide state
        val settingsViewModel: org.friesoft.porturl.viewmodels.SettingsViewModel = hiltViewModel()
        val userPreferences by settingsViewModel.userPreferences.collectAsStateWithLifecycle()
        val searchQuery by sharedViewModel.searchQuery.collectAsStateWithLifecycle()
        val activeAppDetailId by sharedViewModel.activeAppDetailId.collectAsStateWithLifecycle()
        val activeCategoryDetailId by sharedViewModel.activeCategoryDetailId.collectAsStateWithLifecycle()

        // Initial Auth Check and Redirection
        LaunchedEffect(Unit) {
            if (!authState.isAuthorized) {
                navigator.navigate(Routes.Login)
            }
        }

        // Continuous Auth Check
        LaunchedEffect(authState.isAuthorized) {
            if (!authState.isAuthorized) {
                 val currentRoute = navigationState.topLevelRoute
                 if (currentRoute != Routes.Login) {
                     navigationState.clearAllBackStacks()
                     navigator.navigate(Routes.Login)
                 }
            }
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
            layoutMode = userPreferences?.layoutMode ?: org.friesoft.porturl.data.model.LayoutMode.GRID,
            onLayoutModeChanged = { settingsViewModel.saveLayoutMode(it) },
            onProfileClick = { navigator.navigate(Routes.Profile) },
            onRefresh = { sharedViewModel.triggerRefreshAppList() },
            onAddApp = { sharedViewModel.openAppDetail(-1) },
            onAddCategory = { sharedViewModel.openCategoryDetail(-1) },
            isModalOpen = activeAppDetailId != null || activeCategoryDetailId != null
        ) { onAppListInteraction ->
            val entryProvider = entryProvider {
                entry<Routes.Login> {
                    LoginScreen(navigator, authViewModel = authViewModel)
                }
                entry<Routes.Settings> {
                    SettingsScreen(navigator)
                }
                entry<Routes.AppList> {
                    ApplicationListRoute(
                        navigator = navigator,
                        sharedViewModel = sharedViewModel,
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel,
                        isolatedAuthManager = isolatedAuthManager,
                        onAppListInteraction = onAppListInteraction
                    )
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
                entry<Routes.Profile> {
                    ProfileScreen(navigator = navigator, authViewModel = authViewModel)
                }
            }

            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                transitionSpec = {
                    val spec = tween<IntOffset>(durationMillis = 400, easing = LinearOutSlowInEasing)
                    val fadeSpec = tween<Float>(durationMillis = 400)
                    slideInHorizontally(animationSpec = spec) { it }
                        .togetherWith(slideOutHorizontally(animationSpec = spec) { -it / 3 } + fadeOut(animationSpec = fadeSpec))
                        .apply { targetContentZIndex = 1f }
                },
                popTransitionSpec = {
                    val spec = tween<IntOffset>(durationMillis = 400, easing = LinearOutSlowInEasing)
                    slideInHorizontally(animationSpec = spec) { -it / 3 }.togetherWith(
                        slideOutHorizontally(animationSpec = spec) { it }
                    ).apply { targetContentZIndex = -1f }
                },
                predictivePopTransitionSpec = {
                    val spec = tween<IntOffset>(durationMillis = 400, easing = LinearOutSlowInEasing)
                    slideInHorizontally(animationSpec = spec) { -it } togetherWith
                            slideOutHorizontally(animationSpec = spec) { it }
                }
            )

            activeAppDetailId?.let { id ->
                key(id) {
                    org.friesoft.porturl.ui.components.ModalWindow(
                        title = stringResource(if (id == -1L) R.string.app_detail_add_title else R.string.app_detail_edit_title),
                        onClose = { sharedViewModel.closeAppDetail() },
                        windowSizeClass = windowSizeClass
                    ) {
                        ApplicationDetailRoute(
                            navigator = navigator,
                            applicationId = id,
                            sharedViewModel = sharedViewModel
                        )
                    }
                }
            }

            activeCategoryDetailId?.let { id ->
                key(id) {
                    org.friesoft.porturl.ui.components.ModalWindow(
                        title = stringResource(if (id == -1L) R.string.category_detail_add_title else R.string.category_detail_edit_title),
                        onClose = { sharedViewModel.closeCategoryDetail() },
                        windowSizeClass = windowSizeClass
                    ) {
                        CategoryDetailScreen(
                            navigator = navigator,
                            categoryId = id,
                            sharedViewModel = sharedViewModel
                        )
                    }
                }
            }
        }
    }
}
