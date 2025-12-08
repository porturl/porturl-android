package org.friesoft.porturl.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.R
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.viewmodels.AppSharedViewModel
import org.friesoft.porturl.viewmodels.ApplicationDetailViewModel

@Composable
fun ApplicationDetailRoute(
    navigator: Navigator,
    applicationId: Long,
    sharedViewModel: AppSharedViewModel,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }

    LaunchedEffect(Unit) {
        viewModel.finishScreen.collect {
             // Refresh signal
            sharedViewModel.triggerRefreshAppList()
            navigator.goBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ApplicationDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onImageSelected = viewModel::onImageSelected,
        onSaveClick = viewModel::saveApplication,
        onBackClick = { navigator.goBack() },
        applicationId = applicationId,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApplicationDetailScreen(
    uiState: ApplicationDetailViewModel.UiState,
    snackbarHostState: SnackbarHostState,
    onImageSelected: (uri: android.net.Uri?) -> Unit,
    onSaveClick: (name: String, url: String, categoryIds: Set<Long>, availableRoles: List<String>) -> Unit,
    onBackClick: () -> Unit,
    applicationId: Long
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (applicationId == -1L) R.string.app_detail_add_title else R.string.app_detail_edit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.application != null) {
                ApplicationForm(
                    state = uiState,
                    onImagePickerClick = { imagePickerLauncher.launch("image/*") },
                    onSave = onSaveClick
                )
            }
        }
    }
}

@Composable
private fun ApplicationForm(
    state: ApplicationDetailViewModel.UiState,
    onImagePickerClick: () -> Unit,
    onSave: (name: String, url: String, categoryIds: Set<Long>, availableRoles: List<String>) -> Unit
) {
    val application = state.application ?: return
    val focusManager = LocalFocusManager.current
    var name by remember(application.name) { mutableStateOf(application.name) }
    var url by remember(application.url) { mutableStateOf(application.url) }
    var rolesInput by remember(state.roles) { mutableStateOf(state.roles) }
    var selectedCategoryIds by remember(application.applicationCategories) {
        mutableStateOf(application.applicationCategories.map { it.category.id }.toSet())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImagePicker(
                imageModel = state.selectedImageUri ?: application.iconUrlThumbnail,
                onClick = onImagePickerClick
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.app_detail_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(id = R.string.app_detail_url_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            CategorySelector(
                allCategories = state.allCategories,
                selectedIds = selectedCategoryIds,
                onSelectionChanged = { selectedCategoryIds = it }
            )

            RolesEditor(
                roles = rolesInput,
                onRolesChanged = { rolesInput = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSave(name, url, selectedCategoryIds, rolesInput)
            },
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            AnimatedContent(targetState = state.isSaving, label = "SaveButtonContent") { isSaving ->
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolesEditor(
    roles: List<String>?,
    onRolesChanged: (List<String>) -> Unit
) {
    var newRole by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(id = R.string.app_detail_roles_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newRole,
                onValueChange = { newRole = it },
                label = { Text(stringResource(id = R.string.app_detail_new_role_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (newRole.isNotBlank() && !(roles?.contains(newRole) ?: false)) {
                    onRolesChanged((roles ?: emptyList()) + newRole.trim())
                    newRole = ""
                }
            }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.app_detail_add_role_description)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (roles ?: emptyList()).forEach { role ->
                InputChip(
                    selected = true,
                    onClick = { /* Do nothing or select */ },
                    label = { Text(role) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.app_detail_remove_role_description),
                            modifier = Modifier.clickable {
                                onRolesChanged((roles ?: emptyList()) - role)
                            }
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    allCategories: List<org.friesoft.porturl.data.model.Category>,
    selectedIds: Set<Long>,
    onSelectionChanged: (Set<Long>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            stringResource(id = R.string.app_detail_categories_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            allCategories.forEach { category ->
                val isSelected = selectedIds.contains(category.id)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection =
                            if (isSelected) selectedIds - category.id else selectedIds + category.id
                        onSelectionChanged(newSelection)
                    },
                    label = { Text(category.name) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                stringResource(id = R.string.app_detail_category_selected)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

/**
 * A reusable composable for displaying and selecting an application icon.
 *
 * @param imageModel The model for Coil to load (can be a URL string or a content URI).
 * @param onClick A lambda to be executed when the user clicks to change the image.
 */
@Composable
private fun ImagePicker(imageModel: Any?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = stringResource(id = R.string.app_detail_icon_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallback = rememberVectorPainter(image = Icons.Default.AddAPhoto),
            placeholder = rememberVectorPainter(image = Icons.Default.AddAPhoto),
            error = rememberVectorPainter(image = Icons.Default.AddAPhoto)
        )
    }
}
