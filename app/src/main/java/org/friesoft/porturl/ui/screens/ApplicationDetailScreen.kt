package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.viewmodels.ApplicationDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApplicationDetailScreen(
    navController: NavController,
    applicationId: Long,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val applicationState by viewModel.applicationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }

    LaunchedEffect(Unit) {
        viewModel.finishScreen.collect {
            if (it) {
                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_list", true)
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            if (message.isNotBlank()) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (applicationId == -1L) "Add Application" else "Edit Application") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = applicationState) {
            is ApplicationDetailViewModel.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ApplicationDetailViewModel.UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var name by remember(state.application.name) { mutableStateOf(state.application.name) }
                    var url by remember(state.application.url) { mutableStateOf(state.application.url) }
                    // The local state for sortOrder has been removed.
                    var selectedCategoryIds by remember(state.application.applicationCategories) {
                        mutableStateOf(state.application.applicationCategories.map { it.category.id }.toSet())
                    }
                    var iconLarge by remember(state.application.iconUrlLarge) { mutableStateOf(state.application.iconLarge ?: "") }
                    var iconMedium by remember(state.application.iconUrlMedium) { mutableStateOf(state.application.iconUrlMedium ?: "") }
                    var iconThumbnail by remember(state.application.iconUrlThumbnail) { mutableStateOf(state.application.iconUrlThumbnail ?: "") }

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Application Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Text("Categories (at least one is required)", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.allCategories.forEach { category ->
                            val isSelected = selectedCategoryIds.contains(category.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryIds = if (isSelected) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                },
                                label = { Text(category.name) },
                                leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, "Selected") } } else { null }
                            )
                        }
                    }

                    Text("Icon Filenames (Optional)", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = iconLarge, onValueChange = { iconLarge = it }, label = { Text("Large Icon Filename") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = iconMedium, onValueChange = { iconMedium = it }, label = { Text("Medium Icon Filename") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = iconThumbnail, onValueChange = { iconThumbnail = it }, label = { Text("Thumbnail Icon Filename") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveApplication(
                                name = name,
                                url = url,
                                selectedCategoryIds = selectedCategoryIds,
                                iconLarge = iconLarge,
                                iconMedium = iconMedium,
                                iconThumbnail = iconThumbnail
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

