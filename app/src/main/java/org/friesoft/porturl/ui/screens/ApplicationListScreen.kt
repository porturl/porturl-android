package org.friesoft.porturl.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.ui.navigation.Routes
import org.friesoft.porturl.viewmodels.ApplicationListViewModel
import org.friesoft.porturl.viewmodels.AuthViewModel

/**
 * Displays a list of applications fetched from the backend.
 *
 * This is the main screen for authenticated users. It shows a loading indicator,
 * an empty state message, or a scrollable list of applications. Users can
 * navigate to add a new application or log out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    navController: NavController,
    viewModel: ApplicationListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val applications by viewModel.applications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // This effect observes the authentication state. If the user becomes
    // unauthenticated (e.g., by logging out), it automatically navigates
    // them back to the login screen.
    LaunchedEffect(authViewModel.authState) {
        authViewModel.authState.collect {
            if (!it.isAuthorized) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.APP_LIST) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Applications") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                authViewModel.logout()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("${Routes.APP_DETAIL}/-1") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Application")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (applications.isEmpty()) {
                Text(
                    "No applications found.\nTap the '+' button to add one.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(applications, key = { it.id!! }) { app ->
                        ApplicationListItem(
                            application = app,
                            onItemClick = {
                                navController.navigate("${Routes.APP_DETAIL}/${app.id}")
                            },
                            onDeleteClick = {
                                viewModel.deleteApplication(app.id!!)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * A Composable that displays a single application item in a card.
 */
@Composable
fun ApplicationListItem(
    application: Application,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(application.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(application.url, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
