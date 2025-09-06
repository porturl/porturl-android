package org.friesoft.porturl.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.ApplicationListViewModel
import org.friesoft.porturl.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    navController: NavController,
    viewModel: ApplicationListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val applications by viewModel.applications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var isInEditMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pullRefreshState = rememberPullToRefreshState()

    // Launcher for the logout flow. The result is not used, but the launcher
    // is required to start the browser intent.
    val logoutLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // After the browser tab is closed, the user is effectively logged out.
        // The auth state is already cleared, so the LaunchedEffect below will handle navigation.
    }

    // Listen for the "refresh_list" signal from the ApplicationDetailScreen.
    val shouldRefresh by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_list", false)
        ?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }


    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refreshApplications()
            // Reset the signal so it doesn't trigger again on configuration changes
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh_list", false)
        }
    }

    fun openUrlInCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        try {
            // Ensure URL has a scheme
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "http://$url"
            } else {
                url
            }
            customTabsIntent.launchUrl(context, Uri.parse(validUrl))
        } catch (e: ActivityNotFoundException) {
            // Fallback to a standard VIEW intent if a browser that supports custom tabs is not available
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    // When in edit mode, the back button should exit edit mode, not the screen.
    BackHandler(enabled = isInEditMode) {
        isInEditMode = false
    }

    // Navigate to login screen if unauthorized
    LaunchedEffect(authViewModel.authState) {
        authViewModel.authState.collect {
            if (!it.isAuthorized) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.APP_LIST) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isInEditMode) "Select Item to Edit" else "Applications") },
                actions = {
                    if (isInEditMode) {
                        IconButton(onClick = { isInEditMode = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Done")
                        }
                    } else {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    authViewModel.logout(logoutLauncher)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("${Routes.APP_DETAIL}/-1") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Application")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshApplications() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (applications.isEmpty() && !isLoading) {
                // The empty state needs to be in a LazyColumn to be pullable.
                LazyColumn(modifier = Modifier.fillMaxSize()){
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No applications found.\nTap '+' to add one or pull down to refresh.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(applications, key = { it.id!! }) { app ->
                        ApplicationListItem(
                            application = app,
                            isInEditMode = isInEditMode,
                            onItemClick = {
                                if (isInEditMode) {
                                    navController.navigate("${Routes.APP_DETAIL}/${app.id}")
                                } else {
                                    openUrlInCustomTab(app.url)
                                }
                            },
                            onItemLongClick = {
                                isInEditMode = true
                            },
                            onDeleteClick = {
                                viewModel.deleteApplication(app.id!!)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationListItem(
    application: Application,
    isInEditMode: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(application.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(application.url, style = MaterialTheme.typography.bodyMedium)
            }
            // Only show the delete button when in edit mode
            if (isInEditMode) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

