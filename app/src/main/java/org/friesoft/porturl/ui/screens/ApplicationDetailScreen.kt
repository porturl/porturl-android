package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.friesoft.porturl.viewmodels.ApplicationDetailViewModel

/**
 * A screen for creating a new application or editing an existing one.
 *
 * The screen's mode (create or edit) is determined by the `applicationId`.
 * A value of -1L indicates create mode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApplicationDetailScreen(
    navController: NavController,
    applicationId: Long,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val applicationState by viewModel.applicationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger the initial data load for the application when the screen is composed
    // or when the applicationId changes.
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }

    // Effect to handle navigation events from the ViewModel.
    // This will navigate back when the finishScreen flow emits true.
    LaunchedEffect(Unit) {
        viewModel.finishScreen.collect {
            if (it) {
                // Signal the previous screen to refresh its list
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refresh_list", true)
                navController.popBackStack()
            }
        }
    }

    // Effect to show error messages from the ViewModel in a Snackbar.
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    var sortOrder by remember(state.application.sortOrder) { mutableStateOf(state.application.sortOrder.toString()) }
                    var selectedCategoryIds by remember(state.application.categories) {
                        mutableStateOf(state.application.categories.map { it.id }.toSet())
                    }
                    // The UI state is bound to the filename properties for editing
                    var iconLarge by remember(state.application.iconLarge) { mutableStateOf(state.application.iconLarge ?: "") }
                    var iconMedium by remember(state.application.iconMedium) { mutableStateOf(state.application.iconMedium ?: "") }
                    var iconThumbnail by remember(state.application.iconThumbnail) { mutableStateOf(state.application.iconThumbnail ?: "") }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Application Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Categories", style = MaterialTheme.typography.titleMedium)
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
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                } else {
                                    null
                                }
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
                                sortOrder = sortOrder.toIntOrNull() ?: 0,
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
