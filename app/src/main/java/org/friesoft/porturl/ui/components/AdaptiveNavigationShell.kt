package org.friesoft.porturl.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.User
import org.friesoft.porturl.ui.navigation.Routes
import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalLayoutDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveNavigationShell(
    windowSizeClass: WindowWidthSizeClass,
    currentRoute: NavKey,
    onNavigate: (NavKey) -> Unit,
    currentUser: User?,
    isAdmin: Boolean,
    backendUrl: String,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    layoutMode: org.friesoft.porturl.data.model.LayoutMode = org.friesoft.porturl.data.model.LayoutMode.GRID,
    onLayoutModeChanged: (org.friesoft.porturl.data.model.LayoutMode) -> Unit = {},
    onProfileClick: () -> Unit, // Opens menu or profile screen
    onAddApp: () -> Unit,
    onAddCategory: () -> Unit,
    isModalOpen: Boolean = false,
    content: @Composable () -> Unit
) {
    val showNavigation = currentRoute != Routes.Login

    if (!showNavigation) {
        content()
        return
    }

    BackHandler(enabled = searchQuery.isNotEmpty() && currentRoute == Routes.AppList) {
        onSearchQueryChanged("")
    }

    val destinations = mutableListOf(
        NavigationItem(
            route = Routes.AppList,
            labelRes = R.string.app_list_title, // Assuming "Home" or "Apps"
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        )
    )

    if (isAdmin) {
        destinations.add(
            NavigationItem(
                route = Routes.UserList,
                labelRes = R.string.manage_users_title,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person
            )
        )
    }

    destinations.add(
        NavigationItem(
            route = Routes.Settings,
            labelRes = R.string.settings_description,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
             ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                drawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant
             ) {
                Column(Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                        destinations.forEach { item ->
                            val selected = isRouteSelected(currentRoute, item.route)
                            NavigationDrawerItem(
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigate(item.route)
                                },
                                icon = {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    
                    // Footer (Profile)
                    NavigationDrawerItem(
                        selected = currentRoute == Routes.Profile,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onProfileClick()
                        },
                        icon = {
                            UserAvatar(currentUser = currentUser, backendUrl = backendUrl, size = 40.dp)
                        },
                        label = { Text(stringResource(R.string.user_profile)) },
                        modifier = Modifier.padding(12.dp)
                    )
                }
             }
        }
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.height(80.dp).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Menu (10%)
                        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                enabled = !isModalOpen
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_description))
                            }
                        }
                        
                        // Search Bar (80%)
                        Box(modifier = Modifier.weight(0.8f).padding(vertical = 8.dp)) {
                            if (currentRoute == Routes.AppList) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isModalOpen,
                                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = stringResource(R.string.search_description),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { onSearchQueryChanged("") },
                                                    enabled = !isModalOpen
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search_description))
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    val nextMode = if (layoutMode == org.friesoft.porturl.data.model.LayoutMode.GRID)
                                                        org.friesoft.porturl.data.model.LayoutMode.LIST
                                                    else
                                                        org.friesoft.porturl.data.model.LayoutMode.GRID
                                                    onLayoutModeChanged(nextMode)
                                                },
                                                enabled = !isModalOpen
                                            ) {
                                                Icon(
                                                    imageVector = if (layoutMode == org.friesoft.porturl.data.model.LayoutMode.GRID)
                                                        Icons.AutoMirrored.Filled.ViewList
                                                    else
                                                        Icons.Filled.GridView,
                                                    contentDescription = stringResource(R.string.toggle_layout_description)
                                                )
                                            }
                                        }
                                    },
                                    shape = CircleShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledBorderColor = Color.Transparent,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }

                        // Home (10%)
                        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
                            destinations.find { it.route == Routes.AppList }?.let { item ->
                                val selected = isRouteSelected(currentRoute, item.route)
                                IconButton(
                                    onClick = { onNavigate(item.route) },
                                    enabled = !isModalOpen
                                ) {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes),
                                        tint = if (selected) {
                                            if (isModalOpen) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                                            else MaterialTheme.colorScheme.primary
                                        } else {
                                            if (isModalOpen) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute == Routes.AppList && !isModalOpen) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FloatingActionButton(
                            onClick = onAddCategory,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) { Icon(Icons.Default.CreateNewFolder, contentDescription = stringResource(R.string.add_category_description)) }

                        FloatingActionButton(
                            onClick = onAddApp,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_application_description))
                        }
                    }
                }
            }
        ) { padding ->
            val layoutDirection = LocalLayoutDirection.current
            val effectivePadding = PaddingValues(
                start = padding.calculateStartPadding(layoutDirection),
                top = 0.dp,
                end = padding.calculateEndPadding(layoutDirection),
                bottom = padding.calculateBottomPadding()
            )
            Box(
                modifier = Modifier
                    .padding(effectivePadding)
            ) {
                content()
            }
        }
    }
}

data class NavigationItem(
    val route: NavKey,
    val labelRes: Int,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun isRouteSelected(currentRoute: NavKey, itemRoute: NavKey): Boolean {
    if (currentRoute == itemRoute) return true
    
    if (itemRoute == Routes.AppList && (currentRoute is Routes.AppDetail || currentRoute is Routes.CategoryDetail)) {
        return true
    }
    
    if (itemRoute == Routes.UserList && currentRoute is Routes.UserDetail) {
        return true
    }

    return false
}
