package org.friesoft.porturl.ui.screens

import org.friesoft.porturl.ui.components.PortUrlTopAppBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.User
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.UserListViewModel

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import org.friesoft.porturl.ui.components.UserAvatar
import org.friesoft.porturl.viewmodels.AuthViewModel
import org.friesoft.porturl.viewmodels.SettingsViewModel
import org.friesoft.porturl.viewmodels.UserDetailViewModel
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    navigator: Navigator,
    windowSizeClass: WindowWidthSizeClass,
    viewModel: UserListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    userDetailViewModel: UserDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val backendUrl by settingsViewModel.backendUrl.collectAsStateWithLifecycle(
        initialValue = org.friesoft.porturl.data.repository.SettingsRepository.DEFAULT_BACKEND_URL
    )
    val userDetailUiState by userDetailViewModel.uiState.collectAsStateWithLifecycle()

    val isExpanded = windowSizeClass == WindowWidthSizeClass.Expanded
    var userToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<User?>(null) }
    var showCreateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showCreateDialog) {
        var newUsername by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
        var newEmail by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.create_user_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text(stringResource(R.string.user_username_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text(stringResource(R.string.user_email_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.trim().length >= 3) {
                            viewModel.createUser(newUsername.trim(), newEmail.ifBlank { null })
                            showCreateDialog = false
                        }
                    },
                    enabled = newUsername.trim().length >= 3
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text(stringResource(R.string.delete_user_title)) },
            text = { Text(stringResource(R.string.delete_user_confirmation, userToDelete?.username ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { viewModel.deleteUser(it) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LaunchedEffect(uiState.selectedUser) {
        uiState.selectedUser?.id?.let {
            userDetailViewModel.loadUser(it.toString())
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                PortUrlTopAppBar(
                    title = { Text(stringResource(R.string.manage_users_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add User")
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Row(modifier = Modifier.padding(padding).fillMaxSize()) {
                // List Pane
                Box(modifier = Modifier
                    .weight(if (isExpanded) 0.4f else 1f)
                    .fillMaxHeight()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.users) { user ->
                                val isCurrent = user.id == currentUser?.id
                                val isSelected = user.id == uiState.selectedUser?.id
                                UserItem(
                                    user = user,
                                    isCurrent = isCurrent,
                                    isSelected = isExpanded && isSelected,
                                    backendUrl = backendUrl,
                                    onClick = {
                                        if (isExpanded) {
                                            viewModel.selectUser(user)
                                        } else {
                                            navigator.navigate(Routes.UserDetail(user.id.toString()))
                                        }
                                    },
                                    onDelete = {
                                        userToDelete = user
                                    }
                                )
                            }
                        }
                    }
                }

                // Detail Pane (Only in Expanded)
                if (isExpanded) {
                    VerticalDivider()
                    Box(modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                    ) {
                        if (uiState.selectedUser != null) {
                            UserDetailContent(
                                uiState = userDetailUiState,
                                viewModel = userDetailViewModel,
                                settingsViewModel = settingsViewModel
                            )
                        } else {
                            Text(
                                text = "Select a user to see details",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    isCurrent: Boolean,
    isSelected: Boolean,
    backendUrl: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (isCurrent) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                CardDefaults.elevatedCardColors().containerColor
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                currentUser = user,
                backendUrl = backendUrl,
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (user.email != null) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "ID: ${user.id}${if (isCurrent) " (You)" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isCurrent) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
