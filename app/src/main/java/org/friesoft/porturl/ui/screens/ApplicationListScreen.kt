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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
}

// Represents a potential drop location: a category and an index within it.
private data class DropTarget(val categoryId: Long, val index: Int)

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
    val isEditing by sharedViewModel.isEditing.collectAsStateWithLifecycle()
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
        isEditing = isEditing,
        setIsEditing = { sharedViewModel.setEditMode(it) },
        onMoveCategory = viewModel::moveCategoryByDirection,
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
    isEditing: Boolean,
    setIsEditing: (Boolean) -> Unit,
    onMoveCategory: (id: Long, direction: ApplicationListViewModel.MoveDirection) -> Unit,
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
    // --- End D&D State ---

    val logoutLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    val showSearchBar = searchBarVisible || uiState.searchQuery.isNotBlank()

    BackHandler(enabled = isEditing) { setIsEditing(false) }
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
                    TopAppBar(
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
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.clear_search_description))
                                    }
                                }
                            )
                            LaunchedEffect(Unit) {
                                delay(100)
                                focusRequester.requestFocus()
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(stringResource(id = R.string.app_list_title))
                        },
                        actions = {
                            if (windowWidthSize == WindowWidthSizeClass.Compact) {
                                IconButton(onClick = { searchBarVisible = true }) { Icon(Icons.Filled.Search, stringResource(id = R.string.search_description)) }
                                IconButton(onClick = { setIsEditing(!isEditing) }) {
                                    Icon(if (isEditing) Icons.Filled.Done else Icons.Filled.Edit, if (isEditing) stringResource(id = R.string.done_description) else stringResource(id = R.string.edit_mode_description))
                                }
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
                                TextButton(onClick = { searchBarVisible = true }) {
                                    Icon(Icons.Filled.Search, stringResource(id = R.string.search_description), modifier = Modifier.padding(end = 8.dp))
                                    Text(stringResource(id = R.string.search_description))
                                }
                                TextButton(onClick = { setIsEditing(!isEditing) }) {
                                    Icon(if (isEditing) Icons.Filled.Done else Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text(if (isEditing) stringResource(id = R.string.done_description) else stringResource(id = R.string.edit_button_text))
                                }
                                if (isAdmin) {
                                    TextButton(onClick = onManageUsers) {
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
                AnimatedVisibility(visible = isEditing) {
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
                }
                AnimatedVisibility(visible = isEditing) {
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
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
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
                                draggingItem = DraggingItem.App(app, cat, key, absPos, relPos, size, composable)
                            }

                        val onDrag: (Offset) -> Unit = { dragAmount ->
                            draggingItem?.let { state ->
                                state.dragPosition += dragAmount
                                var newDropTarget: DropTarget? = null

                                val targetCatId = categoryBounds.entries.find { (_, rect) ->
                                    rect.contains(state.dragPosition)
                                }?.key

                                if (targetCatId != null) {
                                    val appsInTargetCategory = uiState.allItems
                                        .filterIsInstance<DashboardItem.ApplicationItem>()
                                        .filter { it.parentCategoryId == targetCatId }

                                    val targetIndex = if (appsInTargetCategory.isEmpty()) { 0 }
                                    else {
                                        val closestApp = appsInTargetCategory.minByOrNull { item ->
                                            if (item.key == state.key) return@minByOrNull Float.MAX_VALUE
                                            val bounds = applicationBounds[item.key] ?: return@minByOrNull Float.MAX_VALUE
                                            (bounds.center - state.dragPosition).getDistance()
                                        }

                                        if (closestApp != null) {
                                            val closestBounds = applicationBounds.getValue(closestApp.key)
                                            val closestIndex = appsInTargetCategory.indexOf(closestApp)
                                            if (state.dragPosition.x < closestBounds.center.x) {
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
                                dropTargetInfo = newDropTarget
                            }
                        }

                        val onDragEnd: () -> Unit = {
                            draggingItem?.let { state ->
                                if (state is DraggingItem.App) {
                                    dropTargetInfo?.let { target ->
                                        state.application.id?.let { appId ->
                                            onMoveApplication(appId, state.fromCategory.id, target.categoryId, target.index)
                                        }
                                    }
                                }
                            }
                            draggingItem = null
                            dropTargetInfo = null
                        }

                        if (windowWidthSize == WindowWidthSizeClass.Compact) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                itemsIndexed(sortedCategories, key = { _, cat -> "cat_${cat.id}" }) { index, category ->
                                    val applications = groupedItems[category].orEmpty()
                                    CategoryColumn(
                                        category = category,
                                        applications = applications,
                                        isEditing = isEditing,
                                        dropTargetInfo = dropTargetInfo,
                                        draggingItem = draggingItem,
                                        onSortApps = onSortApps,
                                        onCategoryClick = onCategoryClick,
                                        onDeleteCategory = { itemToDelete = "Category" to category.id },
                                        onMoveCategory = onMoveCategory,
                                        canMoveUp = index > 0,
                                        canMoveDown = index < sortedCategories.lastIndex,
                                        showMoveControls = true,
                                        onApplicationClick = onApplicationClick,
                                        onDeleteApplication = { app -> itemToDelete = "Application" to app.id!! },
                                        onAppDragStart = onDragStart,
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
                                        isEditing = isEditing,
                                        dropTargetInfo = dropTargetInfo,
                                        draggingItem = draggingItem,
                                        onSortApps = onSortApps,
                                        onCategoryClick = onCategoryClick,
                                        onDeleteCategory = { itemToDelete = "Category" to category.id },
                                        onMoveCategory = onMoveCategory,
                                        canMoveUp = index > 0,
                                        canMoveDown = index < sortedCategories.lastIndex,
                                        showMoveControls = false,
                                        onApplicationClick = onApplicationClick,
                                        onDeleteApplication = { app -> itemToDelete = "Application" to app.id!! },
                                        onAppDragStart = onDragStart,
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

            if (isEditing) {
                Box { screenContent() }
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh
                ) { screenContent() }
            }

            // --- Drag Overlay ---
            draggingItem?.let { state ->
                val scale by animateFloatAsState(targetValue = 1.1f, label = "DragScale")
                val elevation by animateDpAsState(targetValue = 8.dp, label = "DragElevation")
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier
                        .offset {
                            IntOffset(
                                (state.dragPosition.x - state.itemOffset.x).roundToInt(),
                                (state.dragPosition.y - state.itemOffset.y).roundToInt()
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
    isEditing: Boolean,
    dropTargetInfo: DropTarget?,
    draggingItem: DraggingItem?,
    onSortApps: (categoryId: Long) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onDeleteCategory: () -> Unit,
    onMoveCategory: (id: Long, direction: ApplicationListViewModel.MoveDirection) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    showMoveControls: Boolean,
    onApplicationClick: (Application) -> Unit,
    onDeleteApplication: (Application) -> Unit,
    onAppDragStart: (Application, Category, String, Offset, Offset, IntSize, @Composable () -> Unit) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryHeader(
            category = category,
            isEditing = isEditing,
            onSortClick = { onSortApps(category.id) },
            onClick = { if (isEditing) onCategoryClick(category) },
            onDeleteClick = onDeleteCategory,
            onMoveUp = { onMoveCategory(category.id, ApplicationListViewModel.MoveDirection.UP) },
            onMoveDown = { onMoveCategory(category.id, ApplicationListViewModel.MoveDirection.DOWN) },
            onMoveLeft = { onMoveCategory(category.id, ApplicationListViewModel.MoveDirection.LEFT) },
            onMoveRight = { onMoveCategory(category.id, ApplicationListViewModel.MoveDirection.RIGHT) },
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
            canMoveLeft = canMoveUp,
            canMoveRight = canMoveDown,
            showVerticalMoveControls = showMoveControls,
            showHorizontalMoveControls = !showMoveControls,
            color = MaterialTheme.colorScheme.tertiaryContainer
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val density = LocalDensity.current
            val draggedItemWidthDp = with(density) { draggingItem?.itemSize?.width?.toDp() ?: 0.dp }
            val draggedItemSpacing = 8.dp

            applications.forEachIndexed { index, application ->
                val appKey = "app_${category.id}_${application.id}"
                var itemBounds by remember { mutableStateOf(Offset.Zero) }
                var itemSize by remember { mutableStateOf(IntSize.Zero) }
                val isNotTheDraggedItem = draggingItem?.key != appKey
                val isBeingDraggedFromThisCategory = (draggingItem as? DraggingItem.App)?.fromCategory?.id == category.id

                val originalIndex = if (isBeingDraggedFromThisCategory) {
                    applications.indexOfFirst { it.id == draggingItem!!.application.id }
                } else {
                    -1 // Sentinel value indicating the item is from another category
                }

                val offsetX by animateDpAsState(
                    targetValue = when {
                        isDropTarget && dropTargetInfo != null && isNotTheDraggedItem -> {
                            val targetIndex = dropTargetInfo.index
                            val space = draggedItemWidthDp + draggedItemSpacing
                            val currentIndex = index

                            if (isBeingDraggedFromThisCategory) {
                                // --- Reordering within the same category ---
                                val shouldShiftLeft = currentIndex > originalIndex && currentIndex < targetIndex
                                val shouldShiftRight = currentIndex < originalIndex && currentIndex >= targetIndex

                                when {
                                    shouldShiftLeft -> -space
                                    shouldShiftRight -> space
                                    else -> 0.dp
                                }
                            } else {
                                // --- Dragging from another category ---
                                if (currentIndex >= targetIndex) space else 0.dp
                            }
                        }
                        // Default: No drag operation active or this is the dragged item, so no offset.
                        else -> 0.dp
                    },
                    label = "ItemShiftAnimation"
                )


                ApplicationGridItem(
                    application = application,
                    isEditing = isEditing,
                    onClick = {
                        if (isEditing) onApplicationClick(application) else openUrlInCustomTab(application.url, context)
                    },
                    onDeleteClick = { onDeleteApplication(application) },
                    modifier = Modifier
                        .offset(x = offsetX)
                        .onGloballyPositioned {
                            itemBounds = it.boundsInRoot().topLeft
                            itemSize = it.size
                            onAppBoundsChanged(appKey, it.boundsInRoot())
                        }
                        .then(
                            if (isEditing) {
                                Modifier.pointerInput(application, category) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val composable: @Composable () -> Unit = {
                                                ApplicationGridItem(
                                                    application,
                                                    isEditing = false,
                                                    {},
                                                    {},
                                                    color = color,
                                                    translucentBackground = translucentBackground)
                                            }
                                            val fingerAbsolutePosition = itemBounds + offset
                                            // To make the drag feel more natural, we center the dragged item
                                            // on the finger, rather than using the initial touch point.
                                            val centerAsOffset =
                                                Offset(itemSize.width / 2f, itemSize.height / 2f)
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
                            } else Modifier.clickable {
                                openUrlInCustomTab(
                                    application.url,
                                    context
                                )
                            }
                        ),
                    isGhost = draggingItem?.key == appKey,
                    color = color,
                    translucentBackground = translucentBackground
                )
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
    isEditing: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false,
    color: Color,
    translucentBackground: Boolean
) {
    Box(modifier = modifier) {
        val alpha by animateFloatAsState(targetValue = if (isGhost) 0f else 1f, label = "GhostAlpha")
        val cardColor = if (translucentBackground) color.copy(alpha = 0.5f) else color
        ElevatedCard(
            onClick = onClick,
            enabled = !isGhost,
            modifier = Modifier.graphicsLayer { this.alpha = alpha },
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
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
        }
        if (isEditing) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
                    .graphicsLayer { this.alpha = alpha }
            ) {
                Icon(Icons.Filled.Delete, stringResource(id = R.string.delete_application_description), tint = MaterialTheme.colorScheme.error)
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
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    showVerticalMoveControls: Boolean,
    showHorizontalMoveControls: Boolean,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = isEditing),
        color = color,
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
            if (isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showVerticalMoveControls) {
                        IconButton(onClick = onMoveUp, enabled = canMoveUp) { Icon(Icons.Filled.ArrowUpward, stringResource(id = R.string.move_up_description), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                        IconButton(onClick = onMoveDown, enabled = canMoveDown) { Icon(Icons.Filled.ArrowDownward, stringResource(id = R.string.move_down_description), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                    }
                    if (showHorizontalMoveControls) {
                        IconButton(onClick = onMoveLeft, enabled = canMoveLeft) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.move_left_description), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                        IconButton(onClick = onMoveRight, enabled = canMoveRight) { Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(id = R.string.move_right_description), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                    }
                    IconButton(onClick = onSortClick) { Icon(Icons.Filled.SortByAlpha, stringResource(id = R.string.sort_alpha_description), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, stringResource(id = R.string.delete_category_description), tint = MaterialTheme.colorScheme.error) }
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
