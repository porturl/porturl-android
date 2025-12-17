@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalLayoutApi::class
)

package org.friesoft.porturl.ui.screens

import androidx.activity.compose.LocalActivity
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.friesoft.porturl.R
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.friesoft.porturl.ui.components.PortUrlTopAppBar

// A sealed class to represent the item currently being dragged.
private sealed class DraggingItem {
    abstract val key: String
    abstract var dragPosition: Offset
    abstract val itemOffset: Offset
    abstract val itemSize: IntSize
    abstract val composable: @Composable () -> Unit

    data class App(
        val application: Application,
        val fromCategory: Category,
        val dashboardItemKey: String,
        override var dragPosition: Offset,
        override val itemOffset: Offset,
        override val itemSize: IntSize,
        override val composable: @Composable () -> Unit,
    ) : DraggingItem() {
        override val key: String = dashboardItemKey
    }

    data class CategoryItem(
        val category: Category,
        override var dragPosition: Offset,
        override val itemOffset: Offset,
        override val itemSize: IntSize,
        override val composable: @Composable () -> Unit
    ) : DraggingItem() {
        override val key: String = "category_${category.id}"
    }
}

// Represents a potential drop location: a category and an index within it.
private data class DropTarget(val categoryId: Long, val index: Int)

private sealed interface CategoryDisplayItem {
    data class App(val application: Application) : CategoryDisplayItem
    data class Placeholder(val size: IntSize) : CategoryDisplayItem
}

@Composable
fun ApplicationListRoute(
    navigator: Navigator,
    sharedViewModel: AppSharedViewModel,
    viewModel: ApplicationListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin by authViewModel.isAdmin.collectAsStateWithLifecycle()
    val shouldRefresh by sharedViewModel.shouldRefreshAppList.collectAsStateWithLifecycle()
    val userPreferences by settingsViewModel.userPreferences.collectAsStateWithLifecycle(
        initialValue = org.friesoft.porturl.data.model.UserPreferences(
            org.friesoft.porturl.data.model.ThemeMode.SYSTEM,
            org.friesoft.porturl.data.model.ColorSource.SYSTEM,
            null,
            null,
            translucentBackground = false
        )
    )

    LaunchedEffect(authState.isAuthorized) {
        if (!authState.isAuthorized) {
            navigator.navigate(Routes.Login)
        }
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refreshData()
            sharedViewModel.onAppListRefreshed()
        }
    }

    ApplicationListScreen(
        uiState = uiState,
        onCategoryDragEnd = viewModel::moveCategory,
        onMoveApplication = viewModel::moveApplication,
        onSortApps = viewModel::sortAppsAlphabetically,
        onRefresh = viewModel::refreshData,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onApplicationClick = { app -> navigator.navigate(Routes.AppDetail(app.id!!)) },
        onCategoryClick = { category -> navigator.navigate(Routes.CategoryDetail(category.id)) },
        onAddApplication = { navigator.navigate(Routes.AppDetail(-1)) },
        onAddCategory = { navigator.navigate(Routes.CategoryDetail(-1)) },
        onDeleteApplication = viewModel::deleteApplication,
        onDeleteCategory = viewModel::deleteCategory,
        authViewModel = authViewModel,
        currentUser = currentUser,
        onSettingsClick = { navigator.navigate(Routes.Settings) },
        onManageUsers = { navigator.navigate(Routes.UserList) },
        isAdmin = isAdmin,
        translucentBackground = userPreferences.translucentBackground
    )
}


@Composable
fun ApplicationListScreen(
    uiState: ApplicationListState,
    onCategoryDragEnd: (fromCatId: Long, targetCategoryIndex: Int) -> Unit,
    onMoveApplication: (appId: Long, fromCatId: Long, toCatId: Long, targetIndexInCat: Int) -> Unit,
    onSortApps: (categoryId: Long) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onApplicationClick: (Application) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onAddApplication: () -> Unit,
    onAddCategory: () -> Unit,
    onDeleteApplication: (id: Long) -> Unit,
    onDeleteCategory: (id: Long) -> Unit,
    authViewModel: AuthViewModel,
    currentUser: org.friesoft.porturl.data.model.User?,
    onSettingsClick: () -> Unit,
    onManageUsers: () -> Unit,
    isAdmin: Boolean,
    translucentBackground: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var searchBarVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val activity = LocalActivity.current!!
    val windowWidthSize = calculateWindowSizeClass(activity).widthSizeClass

    // --- Drag and Drop State ---
    var draggingItem by remember { mutableStateOf<DraggingItem?>(null) }
    var dropTargetInfo by remember { mutableStateOf<DropTarget?>(null) }
    val categoryBounds = remember { mutableStateMapOf<Long, Rect>() }
    val applicationBounds = remember { mutableStateMapOf<String, Rect>() }
    // Snapshot of bounds relative to their category, taken at drag start.
    // keys are same as applicationBounds: "app_{catId}_{appId}"
    var frozenAppBounds by remember { mutableStateOf<Map<String, Rect>?>(null) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyStaggeredGridState()
    var listBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(draggingItem) {
        if (draggingItem != null) {
            while (true) {
                val dragPos = draggingItem?.dragPosition
                if (dragPos != null && listBounds != null) {
                    val topEdge = listBounds!!.top + 150f
                    val bottomEdge = listBounds!!.bottom - 150f
                    val y = dragPos.y

                    var scrollDiff = 0f
                    if (y < topEdge) {
                        scrollDiff = -10f
                    } else if (y > bottomEdge) {
                        scrollDiff = 10f
                    }

                    if (scrollDiff != 0f) {
                        if (windowWidthSize == WindowWidthSizeClass.Compact) {
                            if (listState.canScrollForward && scrollDiff > 0 || listState.canScrollBackward && scrollDiff < 0) {
                                listState.animateScrollBy(scrollDiff)
                            }
                        } else {
                            if (gridState.canScrollForward && scrollDiff > 0 || gridState.canScrollBackward && scrollDiff < 0) {
                                gridState.animateScrollBy(scrollDiff)
                            }
                        }
                    }
                }
                delay(16)
            }
        }
    }
    // --- End D&D State ---

    val logoutLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    val showSearchBar = searchBarVisible || uiState.searchQuery.isNotBlank()

    BackHandler(enabled = showSearchBar) {
        onSearchQueryChanged("")
        searchBarVisible = false
    }

    uiState.error?.let { LaunchedEffect(it) { snackbarHostState.showSnackbar(message = it) } }

    if (itemToDelete != null) {
        val itemType = if (itemToDelete!!.first == "Application") stringResource(id = R.string.item_type_application)
        else stringResource(id = R.string.item_type_category)
        DeleteConfirmationDialog(
            itemType = itemType,
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
            AnimatedContent(
                targetState = showSearchBar,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                },
                label = "TopBarAnimation"
            ) { isSearchOpen ->
                if (isSearchOpen) {
                    PortUrlTopAppBar(
                        title = {},
                        actions = {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .focusRequester(focusRequester),
                                placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
                                trailingIcon = {
                                    IconButton(onClick = { onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.clear_search_description), tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            )
                            LaunchedEffect(Unit) {
                                delay(100)
                                focusRequester.requestFocus()
                            }
                        }
                    )
                } else {
                    PortUrlTopAppBar(
                        title = {
                            Text(stringResource(id = R.string.app_list_title))
                        },
                        actions = {
                            if (windowWidthSize == WindowWidthSizeClass.Compact) {
                                IconButton(onClick = { searchBarVisible = true }) { Icon(Icons.Filled.Search, stringResource(id = R.string.search_description)) }
                                if (isAdmin) {
                                    IconButton(onClick = onManageUsers) { Icon(Icons.Filled.Person, stringResource(R.string.manage_users_title)) }
                                }
                                UserMenu(
                                    currentUser = currentUser,
                                    onLogout = { authViewModel.logout(logoutLauncher) },
                                    onSettings = onSettingsClick,
                                    onImageSelected = { uri -> authViewModel.updateUserImage(uri) }
                                )
                            } else {
                                TextButton(
                                    onClick = { searchBarVisible = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Icon(Icons.Filled.Search, stringResource(id = R.string.search_description), modifier = Modifier.padding(end = 8.dp))
                                    Text(stringResource(id = R.string.search_description))
                                }
                                if (isAdmin) {
                                    TextButton(
                                        onClick = onManageUsers,
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                                    ) {
                                        Icon(Icons.Filled.Person, stringResource(R.string.manage_users_title), modifier = Modifier.padding(end = 8.dp))
                                        Text(stringResource(R.string.manage_users_title))
                                    }
                                }
                                UserMenu(
                                    currentUser = currentUser,
                                    onLogout = { authViewModel.logout(logoutLauncher) },
                                    onSettings = onSettingsClick,
                                    onImageSelected = { uri -> authViewModel.updateUserImage(uri) }
                                )
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (windowWidthSize == WindowWidthSizeClass.Compact) {
                    FloatingActionButton(
                        onClick = onAddCategory,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_category_description)) }
                } else {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(id = R.string.add_category_description)) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = onAddCategory,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                if (windowWidthSize == WindowWidthSizeClass.Compact) {
                    FloatingActionButton(
                        onClick = onAddApplication,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = stringResource(id = R.string.add_application_description))
                    }
                } else {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(id = R.string.add_application_description)) },
                        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                        onClick = onAddApplication,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .onGloballyPositioned { listBounds = it.boundsInRoot() }
        ) {
            val screenContent = @Composable {
                val groupedItems = uiState.groupedDashboardItems
                val sortedCategories = uiState.allItems.mapNotNull {
                    (it as? DashboardItem.CategoryItem)?.category
                }

                when {
                    uiState.isLoading -> FullScreenLoader()
                    groupedItems.isEmpty() && !uiState.isRefreshing -> EmptyState(showSearchBar)
                    else -> {
                        val onDragStart: (Application, Category, String, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit =
                            { app, cat, key, absPos, relPos, size, composable ->
                                // Freeze bounds!
                                frozenAppBounds = applicationBounds.mapNotNull { (appKey, rect) ->
                                    // Parse category ID from key "app_{catId}_{appId}"
                                    val parts = appKey.split("_")
                                    if (parts.size >= 2) {
                                        val catId = parts[1].toLongOrNull()
                                        if (catId != null) {
                                            val catRect = categoryBounds[catId]
                                            if (catRect != null) {
                                                // Store RELATIVE rect
                                                appKey to rect.translate(-catRect.topLeft)
                                            } else null
                                        } else null
                                    } else null
                                }.toMap()
                                draggingItem = DraggingItem.App(app, cat, key, absPos, relPos, size, composable)
                            }

                        val onCategoryDragStart: (Category, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit =
                            { category, absPos, relPos, size, composable ->
                                draggingItem = DraggingItem.CategoryItem(category, absPos, relPos, size, composable)
                            }

                        val onDrag: (Offset) -> Unit = { dragAmount ->
                            draggingItem?.let { state ->
                                // println("DEBUG_DRAG: delta=$dragAmount, pos=${state.dragPosition}")
                                state.dragPosition += dragAmount
                                // Force recomposition
                                draggingItem = when (state) {
                                    is DraggingItem.App -> state.copy()
                                    is DraggingItem.CategoryItem -> state.copy()
                                }
                                var newDropTarget: DropTarget? = null

                                if (state is DraggingItem.App) {
                                    val targetCatId = categoryBounds.entries.find { (_, rect) ->
                                        rect.contains(state.dragPosition)
                                    }?.key

                                    if (targetCatId != null) {
                                        val appsInTargetCategory = uiState.allItems
                                            .filterIsInstance<DashboardItem.ApplicationItem>()
                                            .filter { it.parentCategoryId == targetCatId }

                                        // Use frozen bounds if available, otherwise fallback to live bounds (shouldn't happen during drag)
                                        val boundsSource = frozenAppBounds ?: applicationBounds

                                        val targetIndex = if (appsInTargetCategory.isEmpty()) {
                                            0
                                        } else {
                                            // Get current category rect for converting global drag pos to relative
                                            val catRect = categoryBounds[targetCatId]
                                            val relativeDragPos = if (frozenAppBounds != null && catRect != null) {
                                                state.dragPosition - catRect.topLeft
                                            } else {
                                                state.dragPosition
                                            }

                                            val closestApp = appsInTargetCategory.minByOrNull { item ->
                                                if (item.key == state.key) return@minByOrNull Float.MAX_VALUE
                                                
                                                val bounds = boundsSource[item.key]
                                                    ?: return@minByOrNull Float.MAX_VALUE
                                                
                                                (bounds.center - relativeDragPos).getDistance()
                                            }

                                            if (closestApp != null) {
                                                val closestBounds = boundsSource.getValue(closestApp.key)
                                                val closestIndex = appsInTargetCategory.indexOf(closestApp)
                                                
                                                // Compare relative drag pos to relative center
                                                if (relativeDragPos.x < closestBounds.center.x) {
                                                    closestIndex
                                                } else {
                                                    closestIndex + 1
                                                }
                                            } else {
                                                appsInTargetCategory.size
                                            }
                                        }
                                        newDropTarget = DropTarget(targetCatId, targetIndex)
                                    }
                                } else if (state is DraggingItem.CategoryItem) {
                                     val targetCatEntry = categoryBounds.entries.find { (_, rect) ->
                                         rect.contains(state.dragPosition)
                                     }
                                     if (targetCatEntry != null) {
                                          val targetCatId = targetCatEntry.key
                                          // Find the index of this category in the UI list of categories
                                          val targetIndex = uiState.allItems.asSequence()
                                                .filterIsInstance<DashboardItem.CategoryItem>()
                                                .map { it.category.id }
                                                .indexOf(targetCatId)

                                          if (targetIndex != -1) {
                                              newDropTarget = DropTarget(targetCatId, targetIndex)
                                          }
                                     }
                                }
                                dropTargetInfo = newDropTarget
                            }
                        }

                        val onDragEnd: () -> Unit = {
                            draggingItem?.let { state ->
                                dropTargetInfo?.let { target ->
                                    if (state is DraggingItem.App) {
                                        state.application.id?.let { appId ->
                                            onMoveApplication(appId, state.fromCategory.id, target.categoryId, target.index)
                                        }
                                    } else if (state is DraggingItem.CategoryItem) {
                                        onCategoryDragEnd(state.category.id, target.index)
                                    }
                                }
                            }
                            draggingItem = null
                            dropTargetInfo = null
                            frozenAppBounds = null
                        }

                        if (windowWidthSize == WindowWidthSizeClass.Compact) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                itemsIndexed(sortedCategories, key = { _, cat -> "cat_${cat.id}" }) { index, category ->
                                    val applications = groupedItems[category].orEmpty()
                                    CategoryColumn(
                                        category = category,
                                        applications = applications,
                                        dropTargetInfo = dropTargetInfo,
                                        draggingItem = draggingItem,
                                        onSortApps = onSortApps,
                                        onCategoryClick = onCategoryClick,
                                        onDeleteCategory = { itemToDelete = "Category" to category.id },
                                        onApplicationClick = onApplicationClick,
                                        onDeleteApplication = { app -> itemToDelete = "Application" to app.id!! },
                                        onAppDragStart = onDragStart,
                                        onCategoryDragStart = onCategoryDragStart,
                                        onDrag = onDrag,
                                        onDragEnd = onDragEnd,
                                        onAppBoundsChanged = { key, rect -> applicationBounds[key] = rect },
                                        translucentBackground = translucentBackground,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned {
                                                categoryBounds[category.id] = it.boundsInRoot()
                                            }
                                            .animateItem()
                                    )
                                }
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                state = gridState,
                                columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalItemSpacing = 16.dp
                            ) {
                                itemsIndexed(sortedCategories, key = { _, cat -> "cat_${cat.id}" }) { index, category ->
                                    val applications = groupedItems[category].orEmpty()
                                    CategoryColumn(
                                        category = category,
                                        applications = applications,
                                        dropTargetInfo = dropTargetInfo,
                                        draggingItem = draggingItem,
                                        onSortApps = onSortApps,
                                        onCategoryClick = onCategoryClick,
                                        onDeleteCategory = { itemToDelete = "Category" to category.id },
                                        onApplicationClick = onApplicationClick,
                                        onDeleteApplication = { app -> itemToDelete = "Application" to app.id!! },
                                        onAppDragStart = onDragStart,
                                        onCategoryDragStart = onCategoryDragStart,
                                        onDrag = onDrag,
                                        onDragEnd = onDragEnd,
                                        onAppBoundsChanged = { key, rect -> applicationBounds[key] = rect },
                                        translucentBackground = translucentBackground,
                                        modifier = Modifier
                                            .onGloballyPositioned {
                                                categoryBounds[category.id] = it.boundsInRoot()
                                            }
                                            .animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh
            ) { screenContent() }

            // --- Drag Overlay ---
            draggingItem?.let { state ->
                val scale by animateFloatAsState(targetValue = 1.1f, label = "DragScale")
                val elevation by animateDpAsState(targetValue = 8.dp, label = "DragElevation")
                val parentOffset = listBounds?.topLeft ?: Offset.Zero
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier
                        .offset {
                            IntOffset(
                                (state.dragPosition.x - state.itemOffset.x - parentOffset.x).roundToInt(),
                                (state.dragPosition.y - state.itemOffset.y - parentOffset.y).roundToInt()
                            )
                        }
                        .zIndex(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            shadowElevation = elevation.toPx()
                        }
                    ) {
                        state.composable()
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryColumn(
    category: Category,
    applications: List<Application>,
    dropTargetInfo: DropTarget?,
    draggingItem: DraggingItem?,
    onSortApps: (categoryId: Long) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onDeleteCategory: () -> Unit,
    onApplicationClick: (Application) -> Unit,
    onDeleteApplication: (Application) -> Unit,
    onAppDragStart: (Application, Category, String, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit,
    onCategoryDragStart: (Category, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onAppBoundsChanged: (key: String, bounds: Rect) -> Unit,
    modifier: Modifier = Modifier,
    translucentBackground: Boolean
) {
    val isDropTarget = dropTargetInfo?.categoryId == category.id
    val borderDp by animateDpAsState(if (isDropTarget) 2.dp else 0.dp, label = "DropTargetBorder")
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.primaryContainer
    val density = LocalDensity.current

    Column(
        modifier = modifier.border(borderDp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryHeader(
            category = category,
            onSortClick = { onSortApps(category.id) },
            onEditClick = { onCategoryClick(category) },
            onDeleteClick = onDeleteCategory,
            onDragStart = onCategoryDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            isGhost = draggingItem is DraggingItem.CategoryItem && draggingItem.category.id == category.id,
            translucentBackground = translucentBackground
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val displayItems = remember(applications, dropTargetInfo, draggingItem) {
                // We start with the original list of applications.
                // We do NOT remove the dragging item. It stays in the list to maintain the gesture.
                // We will hide it visually in the render loop.
                val items = applications.map { CategoryDisplayItem.App(it) }.toMutableList<CategoryDisplayItem>()

                // Insert Placeholder at Target if valid
                if (dropTargetInfo != null && dropTargetInfo.categoryId == category.id) {
                    val insertionIndex = dropTargetInfo.index.coerceIn(0, items.size)
                    val placeholderSize = draggingItem?.itemSize ?: IntSize(200, 200)
                    items.add(insertionIndex, CategoryDisplayItem.Placeholder(placeholderSize))
                }
                items
            }

            displayItems.forEach { item ->
                when (item) {
                    is CategoryDisplayItem.Placeholder -> {
                        key("placeholder") {
                            val widthDp = with(density) { item.size.width.toDp() }
                            val heightDp = with(density) { item.size.height.toDp() }
                            Box(modifier = Modifier.size(widthDp, heightDp))
                        }
                    }
                    is CategoryDisplayItem.App -> {
                        val application = item.application
                        // Stable key is crucial for preserving the Node and Gesture
                        key(application.id) {
                            val appKey = "app_${category.id}_${application.id}"
                            var itemBounds by remember(application.id) { mutableStateOf(Offset.Zero) }
                            var itemSize by remember(application.id) { mutableStateOf(IntSize.Zero) }
                            val isDraggedItem = draggingItem?.key == appKey
                            
                            // If this is the dragging item, we effectively "hide" it from the flow
                            // by setting its size to 0. 
                            // However, the Node must remain to handle the gesture events.
                            val layoutModifier = if (isDraggedItem) Modifier.size(0.dp) else Modifier

                            Box(
                                modifier = Modifier
                                    .then(layoutModifier)
                                    .onGloballyPositioned {
                                        // Update bounds only if it's not collapsed (normal state)
                                        if (!isDraggedItem) {
                                            itemBounds = it.boundsInRoot().topLeft
                                            itemSize = it.size
                                            onAppBoundsChanged(appKey, it.boundsInRoot())
                                        }
                                    }
                                    .pointerInput(application, category) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { offset ->
                                                val composable: @Composable () -> Unit = {
                                                    ApplicationGridItem(
                                                        application = application,
                                                        onClick = {},
                                                        onEditClick = {},
                                                        onDeleteClick = {},
                                                        color = color,
                                                        translucentBackground = translucentBackground
                                                    )
                                                }
                                                // itemBounds was captured before it became 0 size (during last normal render)
                                                val fingerAbsolutePosition = itemBounds + offset
                                                val centerAsOffset = offset
                                                onAppDragStart(
                                                    application,
                                                    category,
                                                    appKey,
                                                    fingerAbsolutePosition,
                                                    centerAsOffset,
                                                    itemSize,
                                                    composable
                                                )
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                onDrag(dragAmount)
                                            },
                                            onDragEnd = onDragEnd,
                                            onDragCancel = onDragEnd
                                        )
                                    }
                            ) {
                                ApplicationGridItem(
                                    application = application,
                                    onClick = { openUrlInCustomTab(application.url, context) },
                                    onEditClick = { onApplicationClick(application) },
                                    onDeleteClick = { onDeleteApplication(application) },
                                    modifier = Modifier, 
                                    isGhost = isDraggedItem,
                                    color = color,
                                    translucentBackground = translucentBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to open URL, extracted for reuse
private fun openUrlInCustomTab(url: String, context: android.content.Context) {
    try {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "http://$url" else url
        CustomTabsIntent.Builder().build().launchUrl(context, validUrl.toUri())
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e2: Exception) {
            // Handle cases where no browser is available
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
private fun EmptyState(isSearchActive: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (isSearchActive) stringResource(id = R.string.empty_state_no_results) else stringResource(id = R.string.empty_state_no_items),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ApplicationGridItem(
    application: Application,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false,
    color: Color,
    translucentBackground: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        val alpha by animateFloatAsState(targetValue = if (isGhost) 0f else 1f, label = "GhostAlpha")
        val cardColor = if (translucentBackground) color.copy(alpha = 0.5f) else color
        ElevatedCard(
            onClick = onClick,
            enabled = !isGhost,
            modifier = Modifier.graphicsLayer { this.alpha = alpha },
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                        .padding(8.dp),
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
                        contentDescription = stringResource(id = R.string.application_icon_description, application.name),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = application.name,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.edit_mode_description),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.edit_button_text)) },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.delete_button_text)) },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: Category,
    onSortClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDragStart: (Category, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    color: Color,
    isGhost: Boolean = false,
    translucentBackground: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var headerSize by remember(category.id) { mutableStateOf(IntSize.Zero) }
    var headerPosition by remember(category.id) { mutableStateOf(Offset.Zero) }
    val alpha by animateFloatAsState(targetValue = if (isGhost) 0f else 1f, label = "GhostAlpha")
    val surfaceColor = if (translucentBackground) color.copy(alpha = 0.5f) else color

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .onGloballyPositioned {
                headerSize = it.size
                headerPosition = it.boundsInRoot().topLeft
            }
            .pointerInput(category) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val composable: @Composable () -> Unit = {
                            CategoryHeader(
                                category = category,
                                onSortClick = {},
                                onEditClick = {},
                                onDeleteClick = {},
                                onDragStart = { _, _, _, _, _ -> },
                                onDrag = {},
                                onDragEnd = {},
                                color = color,
                                isGhost = false,
                                translucentBackground = translucentBackground
                            )
                        }
                        val fingerAbsolutePosition = headerPosition + offset
                        val centerAsOffset = offset
                        onDragStart(category, fingerAbsolutePosition, centerAsOffset, headerSize, composable)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            },
        color = surfaceColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f, fill = false),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.edit_mode_description),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.sort_alpha_description)) },
                        onClick = {
                            menuExpanded = false
                            onSortClick()
                        },
                        leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.edit_button_text)) },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.delete_button_text)) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
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
        title = { Text(stringResource(id = R.string.delete_dialog_title, itemType)) },
        text = { Text(stringResource(id = R.string.delete_dialog_text, itemType)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(id = R.string.delete_button_text)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) } }
    )
}

// Extension for vector magnitude
private fun Offset.getDistance() = sqrt(x.pow(2) + y.pow(2))

@Composable
fun UserMenu(
    currentUser: org.friesoft.porturl.data.model.User?,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    onImageSelected: (android.net.Uri) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box {
        IconButton(onClick = { expanded = true }) {
            if (currentUser?.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentUser.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(id = R.string.user_image_description),
                    modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape).size(32.dp),
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person)
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = stringResource(id = R.string.user_image_description))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // User Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .border(1.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser?.imageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentUser.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(id = R.string.user_image_description),
                            modifier = Modifier.fillMaxSize(),
                            placeholder = rememberVectorPainter(Icons.Default.Person),
                            error = rememberVectorPainter(Icons.Default.Person)
                        )
                    } else {
                         Icon(
                             Icons.Default.Person,
                             contentDescription = null,
                             modifier = Modifier.size(48.dp),
                             tint = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider()

            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.settings_description)) },
                onClick = {
                    expanded = false
                    onSettings()
                },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )

            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.logout_description)) },
                onClick = {
                    expanded = false
                    onLogout()
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
            )
        }
    }
}
