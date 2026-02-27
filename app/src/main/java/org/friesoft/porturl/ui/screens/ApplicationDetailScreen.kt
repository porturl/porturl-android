package org.friesoft.porturl.ui.screens

import org.friesoft.porturl.ui.components.PortUrlTopAppBar
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.Category
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.viewmodels.AppSharedViewModel
import org.friesoft.porturl.viewmodels.ApplicationDetailViewModel

@Composable
fun ApplicationDetailRoute(
    navigator: Navigator,
    applicationId: Long,
    sharedViewModel: AppSharedViewModel,
    viewModel: ApplicationDetailViewModel = hiltViewModel(key = applicationId.toString())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }

    LaunchedEffect(Unit) {
        viewModel.finishScreen.collect {
            sharedViewModel.triggerRefreshAppList()
            sharedViewModel.closeAppDetail()
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
        onBackClick = { sharedViewModel.closeAppDetail() },
        onScanRealmClick = viewModel::scanRealmClients,
        onCheckLinkStatus = viewModel::checkLinkStatus,
        onClearScannedClients = viewModel::clearScannedClients,
        onRefreshRealms = viewModel::refreshRealms,
        applicationId = applicationId,
    )
}

@Composable
private fun ClientDiscoveryDialog(
    clients: List<org.friesoft.porturl.client.model.KeycloakClientDto>,
    onClientSelected: (org.friesoft.porturl.client.model.KeycloakClientDto) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.app_detail_discovery_dialog_title)) },
        text = {
            if (clients.isEmpty()) {
                Text(stringResource(id = R.string.app_detail_discovery_no_clients))
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    clients.forEach { client ->
                        val currentClientId = client.clientId ?: return@forEach
                        ListItem(
                            headlineContent = { Text(currentClientId) },
                            supportingContent = client.name?.let { { Text(it) } },
                            modifier = Modifier.clickable { onClientSelected(client) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    uiState: ApplicationDetailViewModel.UiState,
    snackbarHostState: SnackbarHostState,
    onImageSelected: (uri: android.net.Uri?) -> Unit,
    onSaveClick: (name: String, url: String, categoryIds: Set<Long>, availableRoles: List<String>, clientId: String?, realm: String?) -> Unit,
    onBackClick: () -> Unit,
    onScanRealmClick: (realm: String) -> Unit,
    onCheckLinkStatus: (realm: String, clientId: String) -> Unit,
    onClearScannedClients: () -> Unit,
    onRefreshRealms: () -> Unit,
    applicationId: Long
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onImageSelected(uri) }
    )

    if (uiState.scannedClients.isNotEmpty()) {
        // Handled in ApplicationForm for better state management of name/clientId
    }

    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.application != null) {
                ApplicationForm(
                    state = uiState,
                    onImagePickerClick = { imagePickerLauncher.launch("image/*") },
                    onSave = onSaveClick,
                    onScanRealmClick = onScanRealmClick,
                    onCheckLinkStatus = onCheckLinkStatus,
                    onClearScannedClients = onClearScannedClients,
                    onRefreshRealms = onRefreshRealms
                )
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationForm(
    state: ApplicationDetailViewModel.UiState,
    onImagePickerClick: () -> Unit,
    onSave: (name: String, url: String, categoryIds: Set<Long>, availableRoles: List<String>, clientId: String?, realm: String?) -> Unit,
    onScanRealmClick: (realm: String) -> Unit,
    onCheckLinkStatus: (realm: String, clientId: String) -> Unit,
    onClearScannedClients: () -> Unit,
    onRefreshRealms: () -> Unit
) {
    val application = state.application ?: return
    val focusManager = LocalFocusManager.current
    var name by remember(application.name) { mutableStateOf(application.name ?: "") }
    var url by remember(application.url) { mutableStateOf(application.url ?: "") }
    var clientId by remember(application.clientId) { mutableStateOf(application.clientId ?: "") }
    var realm by remember(application.realm) { mutableStateOf(application.realm ?: "") }
    var rolesInput by remember(state.roles) { mutableStateOf(state.roles) }
    var selectedCategoryIds by remember(application.categories) {
        mutableStateOf(application.categories?.mapNotNull { it.id }?.toSet() ?: emptySet())
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
                imageModel = state.selectedImageUri ?: application.iconUrl,
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

            // Keycloak Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.app_detail_keycloak_section_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    var realmExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = realmExpanded,
                        onExpandedChange = { realmExpanded = !realmExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = realm,
                            onValueChange = { 
                                realm = it
                                onCheckLinkStatus(realm, clientId)
                                if (realm.isBlank()) {
                                    onClearScannedClients()
                                    clientId = ""
                                }
                            },
                            label = { Text(stringResource(id = R.string.app_detail_realm_label)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine = true,
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = onRefreshRealms) {
                                        Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.app_detail_scan_button))
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = realmExpanded)
                                }
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        if (state.availableRealms.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = realmExpanded,
                                onDismissRequest = { realmExpanded = false }
                            ) {
                                state.availableRealms.forEach { availableRealm ->
                                    DropdownMenuItem(
                                        text = { Text(availableRealm) },
                                        onClick = {
                                            realm = availableRealm
                                            realmExpanded = false
                                            onCheckLinkStatus(realm, clientId)
                                            onScanRealmClick(realm)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    var clientExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = clientExpanded,
                        onExpandedChange = { clientExpanded = !clientExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = clientId,
                            onValueChange = { 
                                clientId = it
                                onCheckLinkStatus(realm, clientId)
                            },
                            label = { Text(stringResource(id = R.string.app_detail_client_id_label)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine = true,
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (state.isScanning) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        IconButton(onClick = { onScanRealmClick(realm) }, enabled = realm.isNotBlank()) {
                                            Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.app_detail_scan_button))
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded)
                                }
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        if (state.scannedClients.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = clientExpanded,
                                onDismissRequest = { clientExpanded = false }
                            ) {
                                state.scannedClients.forEach { scannedClient ->
                                    val currentClientId = scannedClient.clientId ?: return@forEach
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(currentClientId)
                                                scannedClient.name?.let { 
                                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        },
                                        onClick = {
                                            clientId = currentClientId
                                            if (name.isBlank()) {
                                                name = scannedClient.name ?: scannedClient.clientId
                                            }
                                            clientExpanded = false
                                            onCheckLinkStatus(realm, clientId)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.app_detail_link_status_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (state.isLinked) stringResource(id = R.string.app_detail_link_status_linked) else stringResource(id = R.string.app_detail_link_status_not_linked),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        if (state.isLinked) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(start = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    RolesEditor(
                        roles = rolesInput,
                        onRolesChanged = { rolesInput = it },
                        isEnabled = state.isLinked
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSave(name, url, selectedCategoryIds, rolesInput, clientId.takeIf { it.isNotBlank() }, realm.takeIf { it.isNotBlank() })
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
    onRolesChanged: (List<String>) -> Unit,
    isEnabled: Boolean
) {
    var newRole by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(id = R.string.app_detail_roles_title),
            style = MaterialTheme.typography.titleMedium,
            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        if (!isEnabled) {
            Text(
                text = stringResource(id = R.string.app_detail_roles_disabled_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
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
                singleLine = true,
                enabled = isEnabled
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                enabled = isEnabled,
                onClick = {
                    if (newRole.isNotBlank() && !(roles?.contains(newRole) ?: false)) {
                        onRolesChanged((roles ?: emptyList()) + newRole.trim())
                        newRole = ""
                    }
                }
            ) {
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
                    onClick = { },
                    label = { Text(role) },
                    enabled = isEnabled,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.app_detail_remove_role_description),
                            modifier = Modifier.clickable(enabled = isEnabled) {
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
    allCategories: List<Category>,
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
                val categoryId = category.id ?: return@forEach
                val isSelected = selectedIds.contains(categoryId)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection =
                            if (isSelected) selectedIds - categoryId else selectedIds + categoryId
                        onSelectionChanged(newSelection)
                    },
                    label = { Text(category.name ?: "") },
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