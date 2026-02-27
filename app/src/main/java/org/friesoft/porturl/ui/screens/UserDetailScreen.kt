package org.friesoft.porturl.ui.screens

import org.friesoft.porturl.ui.components.PortUrlTopAppBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.ApplicationWithRolesDto
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.viewmodels.UserDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserDetailScreen(
    navigator: Navigator,
    userId: String,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                PortUrlTopAppBar(
                    title = { Text(stringResource(R.string.user_permissions_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.allApplications) { appWithRoles ->
                            appWithRoles.application?.let { app ->
                                AppPermissionItem(
                                    app = app,
                                    availableRoles = appWithRoles.availableRoles ?: emptyList(),
                                    hasAccess = viewModel.hasAccess(app),
                                    onAccessToggle = { isChecked -> viewModel.toggleAccess(app, isChecked) },
                                    hasRole = { role -> viewModel.hasRole(app, role) },
                                    onRoleToggle = { role, isChecked -> viewModel.toggleRole(app, role, isChecked) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppPermissionItem(
    app: Application,
    availableRoles: List<String>,
    hasAccess: Boolean,
    onAccessToggle: (Boolean) -> Unit,
    hasRole: (String) -> Boolean,
    onRoleToggle: (String, Boolean) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = app.name ?: "", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.user_permissions_grant_access),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = hasAccess,
                        onCheckedChange = onAccessToggle
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (availableRoles.isEmpty()) {
                 Text(stringResource(R.string.no_roles_defined), style = MaterialTheme.typography.bodySmall)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableRoles.forEach { role ->
                        val isChecked = hasRole(role)
                        FilterChip(
                            selected = isChecked,
                            onClick = { onRoleToggle(role, !isChecked) },
                            label = { Text(role) },
                            leadingIcon = if (isChecked) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}
