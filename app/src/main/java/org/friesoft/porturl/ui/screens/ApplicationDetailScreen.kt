package org.friesoft.porturl.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        // When an image is picked, notify the ViewModel
        viewModel.onImageSelected(uri)
    }

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

                    ImagePicker(
                        // If a new image is selected, show it. Otherwise, show the existing one.
                        imageModel = state.selectedImageUri ?: state.application.iconUrlThumbnail,
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )

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


                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveApplication(
                                name = name,
                                url = url,
                                selectedCategoryIds = selectedCategoryIds,
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

/**
 * A reusable composable for displaying and selecting an application icon.
 *
 * @param imageModel The model for Coil to load (can be a URL string or a content URI).
 * @param onClick A lambda to be executed when the user clicks to change the image.
 */
@Composable
fun ImagePicker(imageModel: Any?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Use AsyncImage to display either the remote URL or the locally selected URI
        AsyncImage(
            model = imageModel,
            contentDescription = "Application Icon",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            // If there's no image, show a placeholder icon
            fallback = rememberVectorPainter(image = Icons.Default.AddAPhoto)
        )
    }
}


