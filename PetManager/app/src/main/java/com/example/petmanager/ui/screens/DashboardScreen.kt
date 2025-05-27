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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petmanager.R
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.dashboard.AnimalListItem
import com.example.petmanager.ui.screens.dashboard.DashboardUiState
import com.example.petmanager.ui.screens.dashboard.DashboardViewModel
import com.example.petmanager.ui.theme.PetManagerTheme


// TODO: Move BottomNavItem to a common package if used by other top-level screens (e.g., ui.common)
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

    val bottomNavItems = listOf(
        // TODO: Use string resources for labels R.string.bottom_nav_home, etc.
        BottomNavItem(stringResource(R.string.bottom_nav_home), Icons.Filled.Home, Screen.Dashboard),
        BottomNavItem(stringResource(R.string.bottom_nav_calendar), Icons.Filled.CalendarMonth, Screen.Calendar),
        BottomNavItem(stringResource(R.string.bottom_nav_food_stock), Icons.Filled.Inventory, Screen.FoodStock),
        BottomNavItem(stringResource(R.string.bottom_nav_contacts), Icons.Filled.Contacts, Screen.Contacts)
    )
    // Determine current selected item based on route more reliably
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val selectedBottomNavItem = remember(currentRoute) {
        bottomNavItems.indexOfFirst { it.screen.route == currentRoute }.coerceAtLeast(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title_my_animals)) },
                actions = {
                    // Search field is now part of the Column content for better layout control
                },
                colors = TopAppBarDefaults.topAppBarColors( // M3 styling for TopAppBar
                    containerColor = MaterialTheme.colorScheme.surfaceVariant 
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onAddAnimalClick()
                    navController.navigate(Screen.AddEditAnimal.createRoute())
                },
                // Modifier.minimumInteractiveComponentSize() // Default FAB size is usually sufficient
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_animal_fab_description)
                )
            }
        },
        bottomBar = {
            NavigationBar { // Material 3 Bottom Navigation
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) }, // Icon's CD is its label
                        label = { Text(item.label) },
                        selected = selectedBottomNavItem == index,
                        onClick = {
                            // State update handled by route change and remember(currentRoute)
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
        Column(modifier = Modifier.padding(innerPadding)) { // Use Column to place search bar above content
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text(stringResource(R.string.search_animal_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_icon_description)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearchQuery() }) { // IconButton has min touch target by default
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_search_description))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Consistent padding
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge // M3 search field style
            )

            DashboardContent(
                // Modifier.padding is handled by the Column's innerPadding
                uiState = uiState,
                onAnimalClick = { animalId ->
                    viewModel.onAnimalClick(animalId)
                    navController.navigate(Screen.AnimalDetails.createRoute(animalId))
                }
            )
        }
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
