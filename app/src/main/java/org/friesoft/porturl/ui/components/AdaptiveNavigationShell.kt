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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.friesoft.porturl.R
import org.friesoft.porturl.data.model.User
import org.friesoft.porturl.ui.navigation.Routes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalConfiguration

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
    onProfileClick: () -> Unit, // Opens menu or profile screen
    onAddApp: () -> Unit,
    onAddCategory: () -> Unit,
    content: @Composable () -> Unit
) {
    val showNavigation = currentRoute != Routes.Login

    if (!showNavigation) {
        content()
        return
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

    // Check if we should use Rail or BottomBar
    val useRail = windowSizeClass != WindowWidthSizeClass.Compact

    if (useRail) {
        val configuration = LocalConfiguration.current
        // Default expanded if screen is large (Tablet) but not just expanded width (Foldable)
        // Foldables often have width ~800-900dp. Tablets usually > 1000dp.
        var isExpanded by rememberSaveable { 
            mutableStateOf(windowSizeClass == WindowWidthSizeClass.Expanded && configuration.screenWidthDp > 1000) 
        }
        
        if (isExpanded) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(240.dp),
                        windowInsets = WindowInsets(0),
                        drawerShape = RectangleShape,
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Column(Modifier.fillMaxSize()) {
                                Column {
                                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .height(64.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { isExpanded = !isExpanded }) {
                                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_description))
                                        }
                                    }
                                }

                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                                destinations.forEach { item ->
                                    val selected = isRouteSelected(currentRoute, item.route)
                                    NavigationDrawerItem(
                                        selected = selected,
                                        onClick = { onNavigate(item.route) },
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
                                
                                if (currentRoute == Routes.AppList) {
                                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    NavigationDrawerItem(
                                        selected = false,
                                        onClick = onAddApp,
                                        icon = { Icon(Icons.Filled.Apps, contentDescription = null) },
                                        label = { Text(stringResource(R.string.add_application_description)) },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    NavigationDrawerItem(
                                        selected = false,
                                        onClick = onAddCategory,
                                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                                        label = { Text(stringResource(R.string.add_category_description)) },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            
                            // Footer (Profile)
                            NavigationDrawerItem(
                                selected = false,
                                onClick = onProfileClick,
                                icon = {
                                    UserAvatar(currentUser = currentUser, backendUrl = backendUrl)
                                },
                                label = { Text(stringResource(R.string.user_profile)) },
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            ) {
                content()
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxHeight().width(64.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column {
                                 Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
                                 Box(
                                    modifier = Modifier
                                        .height(64.dp)
                                        .width(64.dp), // Fixed width to match standard Rail width
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = { isExpanded = !isExpanded }) {
                                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_description))
                                    }
                                }
                            }

                        // Top items
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            destinations.forEach { item ->
                                val selected = isRouteSelected(currentRoute, item.route)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { onNavigate(item.route) },
                                    icon = {
                                        Icon(
                                            if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = stringResource(item.labelRes)
                                        )
                                    },
                                    alwaysShowLabel = false
                                )
                            }
                            
                            if (currentRoute == Routes.AppList) {
                                Spacer(Modifier.height(16.dp))
                                NavigationRailItem(
                                    selected = false,
                                    onClick = onAddApp,
                                    icon = { Icon(Icons.Filled.Apps, contentDescription = stringResource(R.string.add_application_description)) },
                                    alwaysShowLabel = false
                                )
                                NavigationRailItem(
                                    selected = false,
                                    onClick = onAddCategory,
                                    icon = { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_category_description)) },
                                    alwaysShowLabel = false
                                )
                            }
                        }
                        
                        // Footer (Profile)
                        NavigationRailItem(
                            selected = false,
                            onClick = onProfileClick,
                            icon = {
                                UserAvatar(currentUser = currentUser, backendUrl = backendUrl)
                            },
                            alwaysShowLabel = false
                        )
                        Spacer(Modifier.padding(vertical = 8.dp))
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
    } else {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var isSearchActive by rememberSaveable { mutableStateOf(false) }

        // Automatically close search if query is cleared? Optional.
        // For now, keep it manual close via back button or similar if needed.

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
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onProfileClick()
                            },
                            icon = {
                                UserAvatar(currentUser = currentUser, backendUrl = backendUrl)
                            },
                            label = { Text(stringResource(R.string.user_profile)) },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                 }
            }
        ) {
            Scaffold(
                bottomBar = {
                    AnimatedContent(targetState = isSearchActive, label = "BottomBarSearchFade") { searchMode ->
                        if (searchMode) {
                            Surface(
                                tonalElevation = 3.dp,
                                modifier = Modifier.fillMaxWidth().height(64.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = onSearchQueryChanged,
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                                        singleLine = true,
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search_description))
                                                }
                                            }
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(onClick = { isSearchActive = false }) {
                                         Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                                    }
                                }
                            }
                        } else {
                            NavigationBar(modifier = Modifier.height(64.dp)) {
                                // Menu Item (Left)
                                NavigationBarItem(
                                    selected = false,
                                    onClick = { scope.launch { drawerState.open() } },
                                    icon = { Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_description)) },
                                    alwaysShowLabel = false
                                )
                                
                                // Search Item (Left-ish)
                                NavigationBarItem(
                                    selected = false,
                                    onClick = { isSearchActive = true },
                                    icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_description)) },
                                    alwaysShowLabel = false
                                )

                                // Destinations (Only Home/AppList in Bottom Bar for compact mode)
                                destinations.forEach { item ->
                                    if (item.route != Routes.Settings && item.route != Routes.UserList) {
                                        val selected = isRouteSelected(currentRoute, item.route)
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { onNavigate(item.route) },
                                            icon = {
                                                Icon(
                                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = stringResource(item.labelRes)
                                                )
                                            },
                                            alwaysShowLabel = false
                                        )
                                    }
                                }
                                // Profile Item
                                NavigationBarItem(
                                    selected = false,
                                    onClick = onProfileClick,
                                    icon = {
                                        UserAvatar(currentUser = currentUser, backendUrl = backendUrl)
                                    },
                                    alwaysShowLabel = false
                                )
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
                Box(modifier = Modifier.padding(effectivePadding)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(currentUser: User?, backendUrl: String) {
    val imageUrl = if (!currentUser?.image.isNullOrBlank() && backendUrl.isNotBlank()) {
        "${backendUrl.trimEnd('/')}/api/images/${currentUser.image}"
    } else {
        currentUser?.imageUrl
    }

    if (imageUrl != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.user_profile),
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
            placeholder = null, 
            error = null 
        )
    } else {
        Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.user_profile))
    }
}

data class NavigationItem(
    val route: NavKey,
    val labelRes: Int,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun isRouteSelected(currentRoute: NavKey, itemRoute: NavKey): Boolean {
    // Simple equality check. Might need more complex logic if nested routes exist.
    // For AppDetail, it counts as AppList being selected?
    // Maybe not.
    if (currentRoute == itemRoute) return true
    
    // If current is AppDetail or CategoryDetail, select AppList
    if (itemRoute == Routes.AppList && (currentRoute is Routes.AppDetail || currentRoute is Routes.CategoryDetail)) {
        return true
    }
    
    // If current is UserDetail, select UserList
    if (itemRoute == Routes.UserList && currentRoute is Routes.UserDetail) {
        return true
    }

    return false
}
