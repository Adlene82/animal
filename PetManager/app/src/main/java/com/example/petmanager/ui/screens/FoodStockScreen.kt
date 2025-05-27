package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.R // Assuming R class is generated
import com.example.petmanager.ui.navigation.Screen
// Assuming BottomNavItem is moved to a common location like ui.screens
// For this overwrite, ensure BottomNavItem.kt exists in ui.screens or a common package.
// If not, this screen might temporarily define it or this overwrite will fail if it's imported.
// For now, let's assume it's in `com.example.petmanager.ui.screens.BottomNavItem`
import com.example.petmanager.ui.screens.foodstock.FoodItemCard
import com.example.petmanager.ui.screens.foodstock.FoodItemDisplay
import com.example.petmanager.ui.screens.foodstock.FoodStockUiState
import com.example.petmanager.ui.screens.foodstock.FoodStockViewModel
import com.example.petmanager.ui.theme.PetManagerTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodStockScreen(
    navController: NavController,
    viewModel: FoodStockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        // TODO: Use string resources for labels R.string.bottom_nav_home, etc.
        BottomNavItem(stringResource(id = R.string.bottom_nav_home_placeholder), Icons.Filled.Home, Screen.Dashboard),
        BottomNavItem(stringResource(id = R.string.bottom_nav_calendar_placeholder), Icons.Filled.CalendarMonth, Screen.Calendar),
        BottomNavItem(stringResource(id = R.string.bottom_nav_food_stock_placeholder), Icons.Filled.Inventory, Screen.FoodStock),
        BottomNavItem(stringResource(id = R.string.bottom_nav_contacts_placeholder), Icons.Filled.Contacts, Screen.Contacts)
    )
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val selectedBottomNavItem = remember(currentRoute) {
        bottomNavItems.indexOfFirst { it.screen.route == currentRoute }.coerceAtLeast(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // TODO: Use string resource R.string.title_food_stock
                title = { Text(stringResource(id = R.string.title_food_stock_placeholder), style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // M3 style
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditFoodItem.createRoute())
                },
                modifier = Modifier.minimumInteractiveComponentSize() // Ensure touch target
            ) {
                Icon(
                    Icons.Filled.Add,
                    // TODO: Use string resource R.string.action_add_food_item
                    contentDescription = stringResource(id = R.string.action_add_food_item_placeholder)
                )
            }
        },
        bottomBar = {
            NavigationBar { // M3 NavigationBar
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) }, // Icon's CD is its label
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }, // M3 label style
                        selected = selectedBottomNavItem == index,
                        onClick = {
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
        FoodStockContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onFoodItemClick = { foodItemId ->
                viewModel.onFoodItemClicked(foodItemId)
                navController.navigate(Screen.AddEditFoodItem.createRoute(foodItemId))
            }
        )
    }
}

@Composable
fun FoodStockContent(
    modifier: Modifier = Modifier,
    uiState: FoodStockUiState,
    onFoodItemClick: (Long) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.foodItems.isEmpty()) {
            Text(
                // TODO: Use string resource R.string.empty_food_stock
                text = stringResource(id = R.string.empty_food_stock_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(all = 16.dp), // Consistent padding
                verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing
            ) {
                items(uiState.foodItems, key = { it.id }) { foodItem ->
                    FoodItemCard(foodItem = foodItem, onClick = { onFoodItemClick(foodItem.id) })
                }
            }
        }
        uiState.errorMessage?.let { message ->
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

@Preview(showBackground = true, name = "Food Stock Screen - Loaded")
@Composable
fun FoodStockScreenPreview_Loaded() {
    PetManagerTheme {
        val sampleItems = listOf(
            FoodItemDisplay(1, "Croquettes Super Premium", "Royal Canin", "Chien", 8.5, 10.0, "kg", null, false, 0.85f),
            FoodItemDisplay(2, "Pâtée Gourmande", "Gourmet", "Chat", 0.5, 1.2, "kg", null, true, 0.4f)
        )
        FoodStockContent(uiState = FoodStockUiState(isLoading = false, foodItems = sampleItems), onFoodItemClick = {})
    }
}

@Preview(showBackground = true, name = "Food Stock Screen - Empty")
@Composable
fun FoodStockScreenPreview_Empty() {
    PetManagerTheme {
        FoodStockContent(uiState = FoodStockUiState(isLoading = false, foodItems = emptyList()), onFoodItemClick = {})
    }
}

@Preview(showBackground = true, name = "Food Stock Screen - Loading")
@Composable
fun FoodStockScreenPreview_Loading() {
    PetManagerTheme {
        FoodStockContent(uiState = FoodStockUiState(isLoading = true), onFoodItemClick = {})
    }
}

// Placeholder string resource IDs used:
// R.string.title_food_stock_placeholder
// R.string.action_add_food_item_placeholder
// R.string.empty_food_stock_placeholder
// R.string.bottom_nav_home_placeholder (assuming shared)
// R.string.bottom_nav_calendar_placeholder (assuming shared)
// R.string.bottom_nav_food_stock_placeholder (assuming shared)
// R.string.bottom_nav_contacts_placeholder (assuming shared)
