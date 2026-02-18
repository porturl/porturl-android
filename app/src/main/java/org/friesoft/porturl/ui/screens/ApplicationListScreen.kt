@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalLayoutApi::class
)

package org.friesoft.porturl.ui.screens

import androidx.activity.compose.LocalActivity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.Category
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.*
import kotlin.math.roundToInt

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
        val isVisualDragStarted: Boolean = false
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

    val searchQuery by sharedViewModel.searchQuery.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    LaunchedEffect(searchQuery) {
        viewModel.onSearchQueryChanged(searchQuery)
    }

    LaunchedEffect(authState.isAuthorized, currentUser) {
        if (authState.isAuthorized) {
            if (currentUser != null) {
                viewModel.refreshData()
            }
        } else {
            viewModel.clearData()
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
        onApplicationClick = { app -> app.id?.let { navigator.navigate(Routes.AppDetail(it)) } },
        onCategoryClick = { category -> category.id?.let { navigator.navigate(Routes.CategoryDetail(it)) } },
        onAddApplication = { navigator.navigate(Routes.AppDetail(-1)) },
        onAddCategory = { navigator.navigate(Routes.CategoryDetail(-1)) },
        onDeleteApplication = viewModel::deleteApplication,
        onDeleteCategory = viewModel::deleteCategory,
        translucentBackground = userPreferences.translucentBackground,
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
    translucentBackground: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var itemToDelete by remember { mutableStateOf<Pair<String, Long?>?>(null) }
    var searchBarVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val activity = LocalActivity.current!!
    val windowWidthSize = calculateWindowSizeClass(activity).widthSizeClass

    // --- Drag and Drop State ---
    var draggingItem by remember { mutableStateOf<DraggingItem?>(null) }
    var dropTargetInfo by remember { mutableStateOf<DropTarget?>(null) }
    var menuOpenAppId by remember { mutableStateOf<String?>(null) }

    val categoryBounds = remember { mutableStateMapOf<Long, Rect>() }
    val applicationBounds = remember { mutableStateMapOf<String, Rect>() }
    var frozenAppBounds by remember { mutableStateOf<Map<String, Rect>?>(null) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyStaggeredGridState()
    var listBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(draggingItem) {
        if (draggingItem != null) {
            while (true) {
                val state = draggingItem
                val isVisualDrag = when(state) {
                    is DraggingItem.App -> state.isVisualDragStarted
                    is DraggingItem.CategoryItem -> true
                    else -> false
                }

                if (state != null && isVisualDrag) {
                    val dragPos = state.dragPosition
                    if (listBounds != null) {
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
                }
                delay(16)
            }
        }
    }

    val showSearchBar = searchBarVisible || uiState.searchQuery.isNotBlank()

    BackHandler(enabled = showSearchBar) {
        onSearchQueryChanged("")
        searchBarVisible = false
    }

    uiState.error?.let { LaunchedEffect(it) { snackbarHostState.showSnackbar(message = it) } }

    if (itemToDelete != null) {
        val itemType = if (itemToDelete!!.first == "Application") stringResource(id = R.string.item_type_application)
        else stringResource(id = R.string.item_type_category)
        val itemId = itemToDelete!!.second
        if (itemId != null) {
            DeleteConfirmationDialog(
                itemType = itemType,
                onConfirm = {
                    if (itemToDelete!!.first == "Application") onDeleteApplication(itemId)
                    else onDeleteCategory(itemId)
                    itemToDelete = null
                },
                onDismiss = { itemToDelete = null }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (windowWidthSize != WindowWidthSizeClass.Compact) {
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f),
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChanged,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onSearchQueryChanged("") }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = stringResource(id = R.string.clear_search_description)
                                            )
                                        }
                                    }
                                },
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                searchBarVisible = false
                                onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.cancel))
                            }
                        }
                        LaunchedEffect(Unit) {
                            delay(100)
                            focusRequester.requestFocus()
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (windowWidthSize != WindowWidthSizeClass.Compact) {
                 FloatingActionButton(
                    onClick = { searchBarVisible = !searchBarVisible },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (searchBarVisible) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.search_description)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = onAddCategory,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_category_description)) }

                    FloatingActionButton(
                        onClick = onAddApplication,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = stringResource(id = R.string.add_application_description))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .onGloballyPositioned { listBounds = it.boundsInRoot() }
        ) {
            Column(Modifier.fillMaxSize()) {
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
                                    menuOpenAppId = null
                                    frozenAppBounds = applicationBounds.mapNotNull { (appKey, rect) ->
                                        val parts = appKey.split("_")
                                        if (parts.size >= 2) {
                                            val catId = parts[1].toLongOrNull()
                                            if (catId != null) {
                                                val catRect = categoryBounds[catId]
                                                if (catRect != null) {
                                                    appKey to rect.translate(-catRect.topLeft)
                                                } else null
                                            } else null
                                        } else null
                                    }.toMap()
                                    draggingItem = DraggingItem.App(
                                        app, cat, key, absPos, relPos, size, composable,
                                        isVisualDragStarted = false
                                    )
                                }

                            val onCategoryDragStart: (Category, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit =
                                { category, absPos, relPos, size, composable ->
                                    draggingItem = DraggingItem.CategoryItem(category, absPos, relPos, size, composable)
                                }

                            val onDrag: (Offset) -> Unit = { dragAmount ->
                                draggingItem?.let { state ->
                                    val nextState = when (state) {
                                        is DraggingItem.App -> {
                                            state.copy(
                                                dragPosition = state.dragPosition + dragAmount,
                                                isVisualDragStarted = true
                                            )
                                        }
                                        is DraggingItem.CategoryItem -> {
                                            state.copy(dragPosition = state.dragPosition + dragAmount)
                                        }
                                    }
                                    draggingItem = nextState

                                    val isVisualDrag = if (nextState is DraggingItem.App) nextState.isVisualDragStarted else true

                                    if (isVisualDrag) {
                                        var newDropTarget: DropTarget? = null

                                        if (nextState is DraggingItem.App) {
                                            val targetCatId = categoryBounds.entries.find { (_, rect) ->
                                                rect.contains(nextState.dragPosition)
                                            }?.key

                                            if (targetCatId != null) {
                                                val appsInTargetCategory = uiState.allItems
                                                    .filterIsInstance<DashboardItem.ApplicationItem>()
                                                    .filter { it.parentCategoryId == targetCatId }

                                                val boundsSource = frozenAppBounds ?: applicationBounds

                                                val targetIndex = if (appsInTargetCategory.isEmpty()) {
                                                    0
                                                } else {
                                                    val catRect = categoryBounds[targetCatId]
                                                    val relativeDragPos = if (frozenAppBounds != null && catRect != null) {
                                                        nextState.dragPosition - catRect.topLeft
                                                    } else {
                                                        nextState.dragPosition
                                                    }

                                                    val closestApp = appsInTargetCategory.minByOrNull { item ->
                                                        if (item.key == nextState.key) return@minByOrNull Float.MAX_VALUE
                                                        val bounds = boundsSource[item.key] ?: return@minByOrNull Float.MAX_VALUE
                                                        (bounds.center - relativeDragPos).getDistance()
                                                    }

                                                    if (closestApp != null) {
                                                        val closestBounds = boundsSource.getValue(closestApp.key)
                                                        val closestIndex = appsInTargetCategory.indexOf(closestApp)
                                                        if (relativeDragPos.x < closestBounds.center.x) closestIndex else closestIndex + 1
                                                    } else {
                                                        appsInTargetCategory.size
                                                    }
                                                }
                                                newDropTarget = DropTarget(targetCatId, targetIndex)
                                            }
                                        } else if (nextState is DraggingItem.CategoryItem) {
                                             val targetCatEntry = categoryBounds.entries.find { (_, rect) ->
                                                 rect.contains(nextState.dragPosition)
                                             }
                                             if (targetCatEntry != null) {
                                                  val targetCatId = targetCatEntry.key
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
                            }

                            val onDragEnd: () -> Unit = {
                                draggingItem?.let { state ->
                                    if (state is DraggingItem.App && !state.isVisualDragStarted) {
                                        menuOpenAppId = state.key
                                    } else {
                                        dropTargetInfo?.let { target ->
                                            if (state is DraggingItem.App) {
                                                state.application.id?.let { appId ->
                                                    state.fromCategory.id?.let { fromCatId ->
                                                        onMoveApplication(appId, fromCatId, target.categoryId, target.index)
                                                    }
                                                }
                                            } else if (state is DraggingItem.CategoryItem) {
                                                state.category.id?.let { onCategoryDragEnd(it, target.index) }
                                            }
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
                                    itemsIndexed(sortedCategories, key = { _, cat -> "cat_${cat.id}" }) { _, category ->
                                        val applications = groupedItems[category].orEmpty()
                                        CategoryColumn(
                                            category = category,
                                            applications = applications,
                                            dropTargetInfo = dropTargetInfo,
                                            draggingItem = draggingItem,
                                            menuOpenAppId = menuOpenAppId,
                                            onMenuDismiss = { menuOpenAppId = null },
                                            onSortApps = onSortApps,
                                            onCategoryClick = onCategoryClick,
                                            onDeleteCategory = { itemToDelete = "Category" to category.id },
                                            onApplicationClick = onApplicationClick,
                                            onDeleteApplication = { app -> itemToDelete = "Application" to app.id },
                                            onAppDragStart = onDragStart,
                                            onCategoryDragStart = onCategoryDragStart,
                                            onDrag = onDrag,
                                            onDragEnd = onDragEnd,
                                            onAppBoundsChanged = { key, rect -> applicationBounds[key] = rect },
                                            translucentBackground = translucentBackground,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onGloballyPositioned { coords ->
                                                    category.id?.let { id -> categoryBounds[id] = coords.boundsInRoot() }
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
                                    itemsIndexed(sortedCategories, key = { _, cat -> "cat_${cat.id}" }) { _, category ->
                                        val applications = groupedItems[category].orEmpty()
                                        CategoryColumn(
                                            category = category,
                                            applications = applications,
                                            dropTargetInfo = dropTargetInfo,
                                            draggingItem = draggingItem,
                                            menuOpenAppId = menuOpenAppId,
                                            onMenuDismiss = { menuOpenAppId = null },
                                            onSortApps = onSortApps,
                                            onCategoryClick = onCategoryClick,
                                            onDeleteCategory = { itemToDelete = "Category" to category.id },
                                            onApplicationClick = onApplicationClick,
                                            onDeleteApplication = { app -> itemToDelete = "Application" to app.id },
                                            onAppDragStart = onDragStart,
                                            onCategoryDragStart = onCategoryDragStart,
                                            onDrag = onDrag,
                                            onDragEnd = onDragEnd,
                                            onAppBoundsChanged = { key, rect -> applicationBounds[key] = rect },
                                            translucentBackground = translucentBackground,
                                            modifier = Modifier
                                                .onGloballyPositioned { coords ->
                                                    category.id?.let { id -> categoryBounds[id] = coords.boundsInRoot() }
                                                }
                                                .animateItem()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh
                    ) { screenContent() }
                }
            }

            draggingItem?.let { state ->
                val isVisualDrag = if (state is DraggingItem.App) state.isVisualDragStarted else true
                if (isVisualDrag) {
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
}

@Composable
private fun CategoryColumn(
    category: Category,
    applications: List<Application>,
    dropTargetInfo: DropTarget?,
    draggingItem: DraggingItem?,
    menuOpenAppId: String?,
    onMenuDismiss: () -> Unit,
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

    Column(
        modifier = modifier.border(borderDp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        CategoryHeader(
            category = category,
            onSortClick = { category.id?.let { onSortApps(it) } },
            onEditClick = { onCategoryClick(category) },
            onDeleteClick = onDeleteCategory,
            onDragStart = onCategoryDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            isGhost = draggingItem is DraggingItem.CategoryItem && draggingItem.category.id == category.id,
            translucentBackground = translucentBackground
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val density = LocalDensity.current
            val spacing = 16.dp
            val minItemSize = 80.dp
            val widthPx = with(density) { maxWidth.toPx() }
            val spacingPx = with(density) { spacing.toPx() }
            val minItemSizePx = with(density) { minItemSize.toPx() }
            val numColumns = ((widthPx + spacingPx) / (minItemSizePx + spacingPx)).toInt().coerceAtLeast(1)
            val itemWidthPx = ((widthPx - (spacingPx * (numColumns - 1))) / numColumns) - 0.5f
            val itemWidth = with(density) { itemWidthPx.toDp() }
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                val displayItems = remember(applications, dropTargetInfo, draggingItem) {
                    val items = applications.map { CategoryDisplayItem.App(it) }.toMutableList<CategoryDisplayItem>()
                    if (dropTargetInfo != null && dropTargetInfo.categoryId == category.id) {
                        val insertionIndex = dropTargetInfo.index.coerceIn(0, items.size)
                        items.add(insertionIndex, CategoryDisplayItem.Placeholder(IntSize(100, 100)))
                    }
                    items
                }

                displayItems.forEach { item ->
                    when (item) {
                        is CategoryDisplayItem.Placeholder -> {
                            key("placeholder") {
                                Box(modifier = Modifier.size(itemWidth))
                            }
                        }
                        is CategoryDisplayItem.App -> {
                            val application = item.application
                            key(application.id) {
                                val appKey = "app_${category.id}_${application.id}"
                                var itemBounds by remember(application.id) { mutableStateOf(Offset.Zero) }
                                var itemSize by remember(application.id) { mutableStateOf(IntSize.Zero) }
                                val isDraggedItem = draggingItem?.key == appKey
                                val isVisualDrag = if (isDraggedItem && draggingItem is DraggingItem.App) draggingItem.isVisualDragStarted else false
                                val alphaModifier = if (isVisualDrag) Modifier.alpha(0f) else Modifier
                                val isMenuOpen = menuOpenAppId == appKey || (isDraggedItem && !isVisualDrag)

                                Box(
                                    modifier = Modifier
                                        .width(itemWidth)
                                        .aspectRatio(1f)
                                        .then(alphaModifier)
                                        .onGloballyPositioned {
                                            if (!isVisualDrag) {
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
                                                            translucentBackground = translucentBackground,
                                                            isMenuOpen = false,
                                                            onDismissMenu = {},
                                                            modifier = Modifier.size(with(density) { itemSize.width.toDp() }, with(density) { itemSize.height.toDp() })
                                                        )
                                                    }
                                                    onAppDragStart(application, category, appKey, itemBounds + offset, offset, itemSize, composable)
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
                                        onClick = { application.url?.let { openUrlInCustomTab(it, context) } },
                                        onEditClick = { onApplicationClick(application) },
                                        onDeleteClick = { onDeleteApplication(application) },
                                        modifier = Modifier.fillMaxSize(), 
                                        isGhost = false,
                                        color = color,
                                        translucentBackground = translucentBackground,
                                        isMenuOpen = isMenuOpen,
                                        onDismissMenu = onMenuDismiss,
                                        enabled = !isDraggedItem
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openUrlInCustomTab(url: String, context: android.content.Context) {
    try {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "http://$url" else url
        CustomTabsIntent.Builder().build().launchUrl(context, validUrl.toUri())
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e2: Exception) { }
    }
}

@Composable
private fun FullScreenLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
    translucentBackground: Boolean,
    isMenuOpen: Boolean = false,
    onDismissMenu: () -> Unit = {},
    enabled: Boolean = true
) {
    val screenWidth = LocalWindowInfo.current.containerSize.width
    var itemBounds by remember { mutableStateOf(Rect.Zero) }

    Box(modifier = modifier.onGloballyPositioned { itemBounds = it.boundsInRoot() }) {
        val alpha by animateFloatAsState(targetValue = if (isGhost) 0f else 1f, label = "GhostAlpha")
        val cardColor = if (translucentBackground) color.copy(alpha = 0.5f) else color
        ElevatedCard(
            onClick = onClick,
            enabled = enabled && !isGhost,
            modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha },
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                        contentDescription = stringResource(id = R.string.application_icon_description, application.name ?: ""),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = application.name ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                val anchorAlignment = if (itemBounds.center.x > screenWidth / 2) Alignment.TopStart else Alignment.TopEnd
                Box(modifier = Modifier.align(anchorAlignment)) {
                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = onDismissMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.edit_button_text)) },
                            onClick = {
                                onDismissMenu()
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.delete_button_text)) },
                            onClick = {
                                onDismissMenu()
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
                        onDragStart(category, headerPosition + offset, offset, headerSize, composable)
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
                text = category.name ?: "",
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