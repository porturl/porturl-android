package org.friesoft.porturl.ui.screens

import org.friesoft.porturl.ui.components.PortUrlTopAppBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.R
import org.friesoft.porturl.ui.navigation.Navigator
import org.friesoft.porturl.viewmodels.AppSharedViewModel
import org.friesoft.porturl.viewmodels.CategoryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    navigator: Navigator,
    categoryId: Long,
    sharedViewModel: AppSharedViewModel,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
    }

    LaunchedEffect(Unit) {
        viewModel.finishScreen.collect {
            if (it) {
                sharedViewModel.triggerRefreshAppList()
                sharedViewModel.closeCategoryDetail()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            if (message.isNotBlank()) snackbarHostState.showSnackbar(message)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is CategoryDetailViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CategoryDetailViewModel.UiState.Success -> {
                    var category by remember(state.category) { mutableStateOf(state.category) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = category.name ?: "",
                            onValueChange = { category = category.copy(name = it) },
                            label = { Text(stringResource(id = R.string.category_detail_name_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = category.icon ?: "",
                            onValueChange = { category = category.copy(icon = it) },
                            label = { Text(stringResource(id = R.string.category_detail_icon_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = category.description ?: "",
                            onValueChange = { category = category.copy(description = it) },
                            label = { Text(stringResource(id = R.string.category_detail_description_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = category.enabled ?: true,
                                onCheckedChange = { category = category.copy(enabled = it) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = R.string.category_detail_enabled_label))
                        }

                        Text(
                            text = stringResource(id = R.string.category_detail_sort_mode_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when (category.applicationSortMode) {
                                        org.friesoft.porturl.client.model.Category.ApplicationSortMode.ALPHABETICAL -> stringResource(id = R.string.sort_mode_alphabetical)
                                        else -> stringResource(id = R.string.sort_mode_custom)
                                    }
                                )
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.sort_mode_custom)) },
                                    onClick = {
                                        category = category.copy(applicationSortMode = org.friesoft.porturl.client.model.Category.ApplicationSortMode.CUSTOM)
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.sort_mode_alphabetical)) },
                                    onClick = {
                                        category = category.copy(applicationSortMode = org.friesoft.porturl.client.model.Category.ApplicationSortMode.ALPHABETICAL)
                                        expanded = false
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.saveCategory(category) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.save))
                        }
                    }
                }
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}