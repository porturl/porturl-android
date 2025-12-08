package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                navigator.goBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            if (message.isNotBlank()) snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (categoryId == -1L) R.string.category_detail_add_title else R.string.category_detail_edit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CategoryDetailViewModel.UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is CategoryDetailViewModel.UiState.Success -> {
                var category by remember(state.category) { mutableStateOf(state.category) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = category.name,
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
                            checked = category.enabled,
                            onCheckedChange = { category = category.copy(enabled = it) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.category_detail_enabled_label))
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
    }
}
