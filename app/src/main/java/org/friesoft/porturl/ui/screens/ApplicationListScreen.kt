package org.friesoft.porturl.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.*

@Composable
fun ApplicationListRoute(
    navController: NavController,
    editModeViewModel: EditModeViewModel,
    viewModel: ApplicationListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isEditing by editModeViewModel.isEditing.collectAsStateWithLifecycle()

    val shouldRefresh by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_list", false)
        ?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    LaunchedEffect(authState.isAuthorized) {
        if (!authState.isAuthorized) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.APP_LIST) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refreshData()
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh_list", false)
        }
    }

    ApplicationListScreen(
        uiState = uiState,
        isEditing = isEditing,
        setIsEditing = { editModeViewModel.setEditMode(it) },
        onDrag = viewModel::onDrag,
        onDragEnd = viewModel::onDragEnd,
        onMoveCategory = viewModel::moveCategory,
        onSortApps = viewModel::sortAppsAlphabetically,
        onRefresh = viewModel::refreshData,
        onApplicationClick = { app -> navController.navigate("${Routes.APP_DETAIL}/${app.id}") },
        onCategoryClick = { category -> navController.navigate("${Routes.CATEGORY_DETAIL}/${category.id}") },
        onAddApplication = { navController.navigate("${Routes.APP_DETAIL}/-1") },
        onAddCategory = { navController.navigate("${Routes.CATEGORY_DETAIL}/-1") },
        onDeleteApplication = viewModel::deleteApplication,
        onDeleteCategory = viewModel::deleteCategory,
        authViewModel = authViewModel
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    uiState: ApplicationListState,
    isEditing: Boolean,
    setIsEditing: (Boolean) -> Unit,
    onDrag: (from: Int, to: Int) -> Unit,
    onDragEnd: () -> Unit,
    onMoveCategory: (id: Long, direction: ApplicationListViewModel.MoveDirection) -> Unit,
    onSortApps: (categoryId: Long) -> Unit,
    onRefresh: () -> Unit,
    onApplicationClick: (Application) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onAddApplication: () -> Unit,
    onAddCategory: () -> Unit,
    onDeleteApplication: (id: Long) -> Unit,
    onDeleteCategory: (id: Long) -> Unit,
    authViewModel: AuthViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<Pair<String, Long>?>(null) }

    val reorderState = rememberReorderableLazyGridState(onMove = { from, to -> onDrag(from.index, to.index) })

    // This effect now correctly calls the onDragEnd lambda when a drag is finished.
    LaunchedEffect(reorderState.draggingItemKey) {
        if (reorderState.draggingItemKey == null) {
            onDragEnd()
        }
    }

    val logoutLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

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
    BackHandler(enabled = isEditing) {
        setIsEditing(false)
    }

    uiState.error?.let { LaunchedEffect(it) { snackbarHostState.showSnackbar(message = it) } }

    if (itemToDelete != null) {
        DeleteConfirmationDialog(
            itemType = itemToDelete!!.first,
            onConfirm = {
                if (itemToDelete!!.first == "Application") onDeleteApplication(itemToDelete!!.second)
                else onDeleteCategory(itemToDelete!!.second)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Application Portal") },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) { Icon(Icons.Filled.Search, "Search") }
                    IconButton(onClick = { setIsEditing(!isEditing) }) {
                        Icon(if (isEditing) Icons.Filled.Done else Icons.Filled.Edit, if (isEditing) "Done" else "Edit Mode")
                    }
                    IconButton(onClick = { authViewModel.logout(logoutLauncher) }) { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedVisibility(visible = isEditing) {
                    FloatingActionButton(
                        onClick = onAddCategory,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
                AnimatedVisibility(visible = isEditing) {
                    FloatingActionButton(onClick = onAddApplication) {
                        Icon(Icons.Default.Apps, contentDescription = "Add Application")
                    }
                }
            }
        }
    ) { padding ->
        val screenContent = @Composable {
            when {
                uiState.isLoading -> FullScreenLoader()
                uiState.dashboardItems.isEmpty() && !uiState.isRefreshing -> EmptyState()
                else -> {
                    LazyVerticalGrid(
                        state = reorderState.gridState,
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.fillMaxSize().reorderable(reorderState),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            uiState.dashboardItems,
                            key = { _, item -> item.key },
                            span = { _, item ->
                                if (item is DashboardItem.CategoryItem) GridItemSpan(maxLineSpan) else GridItemSpan(
                                    1
                                )
                            }) { index, item ->
                            ReorderableItem(reorderableState = reorderState, key = item.key) {
                                when (item) {
                                    is DashboardItem.CategoryItem -> {
                                        val isFirstCategory =
                                            uiState.dashboardItems.indexOfFirst { it is DashboardItem.CategoryItem } == index
                                        val isLastCategory =
                                            uiState.dashboardItems.indexOfLast { it is DashboardItem.CategoryItem } == index
                                        CategoryHeader(
                                            category = item.category,
                                            isEditing = isEditing,
                                            onSortClick = { onSortApps(item.category.id) },
                                            onClick = { if (isEditing) onCategoryClick(item.category) },
                                            onDeleteClick = {
                                                itemToDelete = "Category" to item.category.id
                                            },
                                            onMoveUp = {
                                                onMoveCategory(
                                                    item.category.id,
                                                    ApplicationListViewModel.MoveDirection.UP
                                                )
                                            },
                                            onMoveDown = {
                                                onMoveCategory(
                                                    item.category.id,
                                                    ApplicationListViewModel.MoveDirection.DOWN
                                                )
                                            },
                                            canMoveUp = !isFirstCategory,
                                            canMoveDown = !isLastCategory
                                        )
                                    }

                                    is DashboardItem.ApplicationItem -> ApplicationGridItem(
                                        application = item.application,
                                        isEditing = isEditing,
                                        onClick = {
                                            if (isEditing) onApplicationClick(item.application)
                                            else openUrlInCustomTab(item.application.url)
                                        },
                                        onDeleteClick = {
                                            itemToDelete =
                                                "Application" to item.application.id!!
                                        },
                                        modifier = if (isEditing) Modifier.detectReorderAfterLongPress(
                                            reorderState
                                        ) else Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isEditing) {
            Box(modifier = Modifier.padding(padding)) {
                screenContent()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.padding(padding)
            ) {
                screenContent()
            }
        }
    }
}

@Composable
private fun FullScreenLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No applications or categories found.\nTap 'Edit' then '+' to add some!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ApplicationGridItem(
    application: Application,
    isEditing: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.width(150.dp)) {
        ElevatedCard(onClick = onClick) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(application.iconUrlThumbnail)
                        .crossfade(true)
                        .build(),
                    placeholder = rememberVectorPainter(Icons.Default.Image),
                    error = rememberVectorPainter(Icons.Default.BrokenImage),
                    contentDescription = "${application.name} icon",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = application.name,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
        if (isEditing) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)
            ) {
                Icon(Icons.Filled.Delete, "Delete Application", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: Category,
    isEditing: Boolean,
    onSortClick: () -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick, enabled = isEditing),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) { Icon(Icons.Filled.ArrowUpward, "Move Up") }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) { Icon(Icons.Filled.ArrowDownward, "Move Down") }
                    IconButton(onClick = onSortClick) { Icon(Icons.Filled.SortByAlpha, "Sort alphabetically") }
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete Category", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    itemType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $itemType") },
        text = { Text("Are you sure you want to permanently delete this $itemType?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

