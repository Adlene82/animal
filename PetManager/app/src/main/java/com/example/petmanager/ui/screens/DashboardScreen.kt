package com.example.petmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Pets // Placeholder for AnimalCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R // Assuming you have a placeholder drawable
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.dashboard.AnimalListItem
import com.example.petmanager.ui.screens.dashboard.DashboardUiState
import com.example.petmanager.ui.screens.dashboard.DashboardViewModel
import com.example.petmanager.ui.theme.PetManagerTheme


// BottomNavItem remains as previously defined in placeholder
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle() // Already in uiState

    val bottomNavItems = listOf(
        BottomNavItem("Accueil", Icons.Filled.Home, Screen.Dashboard),
        BottomNavItem("Calendrier", Icons.Filled.CalendarMonth, Screen.Calendar),
        BottomNavItem("Réserve", Icons.Filled.Inventory, Screen.FoodStock),
        BottomNavItem("Contacts", Icons.Filled.Contacts, Screen.Contacts)
    )
    // For keeping track of the selected item in BottomNavBar
    // This might need to be hoisted or handled differently if DashboardScreen itself is a destination
    // within a larger NavHost that *also* shows the BottomNavBar.
    // For now, assuming Dashboard is the primary entry for this Scaffold.
    var selectedBottomNavItem by remember { mutableStateOf(0) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Animaux") },
                actions = {
                    // Simple TextField for search, can be enhanced
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Rechercher...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .heightIn(min = 56.dp), // Ensure good height for tap target
                        singleLine = true
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onAddAnimalClick() // ViewModel can log or prepare state if needed
                navController.navigate(Screen.AddEditAnimal.createRoute())
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un animal")
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedBottomNavItem == index, // Or determine by current route
                        onClick = {
                            selectedBottomNavItem = index
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        DashboardContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onAnimalClick = { animalId ->
                viewModel.onAnimalClick(animalId) // ViewModel can log or prepare state
                navController.navigate(Screen.AnimalDetails.createRoute(animalId))
            }
        )
    }
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    uiState: DashboardUiState,
    onAnimalClick: (Long) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.animals.isEmpty()) {
            Text(
                text = "Aucun animal ajouté. Cliquez sur + pour commencer.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.animals, key = { it.id }) { animalItem ->
                    AnimalCard(animalItem = animalItem, onClick = { onAnimalClick(animalItem.id) })
                }
            }
        }
        uiState.errorMessage?.let { message ->
            // Consider using a SnackbarHost to show errors if the design requires it
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

// Preview for DashboardScreen
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PetManagerTheme {
        // Mock NavController and ViewModel for preview
        val navController = rememberNavController()
        // You might need a more sophisticated way to preview states if ViewModel logic is complex
        DashboardScreen(navController = navController)
    }
}

@Preview(showBackground = true, name = "Dashboard Content Empty")
@Composable
fun DashboardContentEmptyPreview() {
    PetManagerTheme {
        DashboardContent(
            uiState = DashboardUiState(isLoading = false, animals = emptyList()),
            onAnimalClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Content With Animals")
@Composable
fun DashboardContentWithAnimalsPreview() {
    PetManagerTheme {
        val animals = listOf(
            AnimalListItem(1, "Buddy", "Chien", "Golden Retriever", "2 ans", null, "Anniversaire !"),
            AnimalListItem(2, "Lucy", "Chat", "Siamois", "5 ans", null, null)
        )
        DashboardContent(
            uiState = DashboardUiState(isLoading = false, animals = animals),
            onAnimalClick = {}
        )
    }
}
