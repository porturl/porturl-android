package org.friesoft.porturl.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.burnoutcrew.reorderable.*
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.ApplicationListViewModel
import org.friesoft.porturl.viewmodels.AuthViewModel
import org.friesoft.porturl.viewmodels.DashboardItem
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    navController: NavController,
    viewModel: ApplicationListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isInEditMode by remember { mutableStateOf(false) }

    val reorderState = rememberReorderableLazyGridState(
        onMove = { from, to -> viewModel.onMove(from.index, to.index) },
        canDragOver = { _, _ -> isInEditMode }
    )

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
            viewModel.refreshDashboard()
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
            customTabsIntent.launchUrl(context, validUrl.toUri())
        } catch (e: ActivityNotFoundException) {
            // Fallback to a standard VIEW intent if a browser that supports custom tabs is not available
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
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
                navController.navigate(Routes.LOGIN) { popUpTo(Routes.APP_LIST) { inclusive = true } }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Portal") },
                actions = {
                    if (isInEditMode) {
                        Button(onClick = { isInEditMode = false }) { Text("Done") }
                    } else {
                        IconButton(onClick = { isInEditMode = true }) { Icon(Icons.Default.Edit, "Enter Edit Mode") }
                        IconButton(onClick = { authViewModel.logout(logoutLauncher) }) { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("${Routes.APP_DETAIL}/-1") }) {
                Icon(Icons.Default.Add, "Add Application")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshDashboard() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            LazyVerticalGrid(
                state = reorderState.gridState,
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(reorderState),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.dashboardItems,
                    // The key is a composite of the category and app IDs,
                    // which guarantees it will be unique across the entire grid.
                    key = { item ->
                        when (item) {
                            is DashboardItem.Header -> "header_${item.category.id}"
                            is DashboardItem.App -> "app_${item.parentCategoryId}_${item.application.id}"
                        }
                    },
                    span = { item ->
                        when (item) {
                            is DashboardItem.Header -> GridItemSpan(maxLineSpan)
                            is DashboardItem.App -> GridItemSpan(1)
                        }
                    }) { item ->
                    ReorderableItem(reorderState, key = item) { isDragging ->
                        // The 'isDragging' state is now passed to the composables to drive animations
                        when (item) {
                            is DashboardItem.Header -> CategoryHeader(
                                category = item.category,
                                isInEditMode = isInEditMode,
                                isDragging = isDragging,
                                dragHandleModifier = Modifier.detectReorderAfterLongPress(reorderState),
                                onEditClick = {
                                    navController.navigate("${Routes.CATEGORY_DETAIL}/${item.category.id}")
                                },
                                onDeleteClick = { viewModel.deleteCategory(item.category.id) }
                            )
                            is DashboardItem.App -> ApplicationGridItem(
                                application = item.application,
                                isInEditMode = isInEditMode,
                                isDragging = isDragging,
                                dragHandleModifier = Modifier.detectReorder(reorderState),
                                onClick = {
                                    if (isInEditMode) navController.navigate("${Routes.APP_DETAIL}/${item.application.id}")
                                    else openUrlInCustomTab(item.application.url)
                                },
                                onDeleteClick = { viewModel.deleteApplication(item.application.id!!) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: Category,
    isInEditMode: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Animate the scale of the header when it's being dragged
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale) // Apply the animated scale
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        AnimatedVisibility(visible = isInEditMode) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder Category",
                // The drag gesture is now tied directly to the handle
                modifier = dragHandleModifier.padding(end = 8.dp)
            )
        }
        Icon(Icons.Default.Category, category.name, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(category.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = isInEditMode) {
            Row {
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit Category") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete Category", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationGridItem(
    application: Application,
    isInEditMode: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Animate the elevation and scale for a "lift" effect when dragging
    val elevation by animateDpAsState(if (isDragging) 12.dp else 4.dp, label = "elevation")
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale), // Apply the animated scale
        elevation = CardDefaults.cardElevation(defaultElevation = elevation) // Apply animated elevation
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = application.iconUrlThumbnail,
                    application.name,
                    Modifier.size(48.dp),
                    error = rememberVectorPainter(Icons.Default.Info)
                )
                Spacer(Modifier.height(8.dp))
                Text(application.name, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2)
            }

            if (isInEditMode) {
                Icon(
                    Icons.Default.DragHandle,
                    "Reorder Application",
                    // The drag gesture is tied directly to the handle
                    modifier = dragHandleModifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
                IconButton(onClick = onDeleteClick, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Delete, "Delete Application", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

