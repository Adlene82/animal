package com.example.petmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.screens.AddEditAnimalScreen
import com.example.petmanager.ui.screens.AddEditContactScreen
import com.example.petmanager.ui.screens.AddEditEventScreen
import com.example.petmanager.ui.screens.AddEditFoodItemScreen
import com.example.petmanager.ui.screens.AnimalDetailsScreen
import com.example.petmanager.ui.screens.CalendarScreen
import com.example.petmanager.ui.screens.ContactDetailsScreen
import com.example.petmanager.ui.screens.ContactsScreen
import com.example.petmanager.ui.screens.DashboardScreen
import com.example.petmanager.ui.screens.FoodStockScreen
import com.example.petmanager.ui.theme.PetManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetManagerTheme {
                PetManagerAppNavigation()
            }
        }
    }
}

@Composable
fun PetManagerAppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(
            route = Screen.AnimalDetails.route,
            arguments = Screen.AnimalDetails.arguments
        ) { navBackStackEntry ->
            val animalId = navBackStackEntry.arguments?.getLong(Screen.AnimalDetails.argumentName)
            requireNotNull(animalId) { "animalId parameter missing" }
            AnimalDetailsScreen(navController = navController, animalId = animalId)
        }
        composable(
            route = Screen.AddEditAnimal.route,
            arguments = Screen.AddEditAnimal.arguments
        ) { navBackStackEntry ->
            val animalId = Screen.getOptionalIdArg(navBackStackEntry.arguments, Screen.AddEditAnimal.argumentName)
            AddEditAnimalScreen(navController = navController, animalId = animalId)
        }
        composable(Screen.FoodStock.route) {
            FoodStockScreen(navController = navController)
        }
        composable(
            route = Screen.AddEditFoodItem.route,
            arguments = Screen.AddEditFoodItem.arguments
        ) { navBackStackEntry ->
            val foodItemId = Screen.getOptionalIdArg(navBackStackEntry.arguments, Screen.AddEditFoodItem.argumentName)
            AddEditFoodItemScreen(navController = navController, foodItemId = foodItemId)
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }
        composable(
            route = Screen.AddEditEvent.route,
            arguments = Screen.AddEditEvent.arguments
        ) { navBackStackEntry ->
            val eventId = Screen.getOptionalIdArg(navBackStackEntry.arguments, Screen.AddEditEvent.argumentName)
            AddEditEventScreen(navController = navController, eventId = eventId)
        }
        composable(Screen.Contacts.route) {
            ContactsScreen(navController = navController)
        }
        composable(
            route = Screen.ContactDetails.route,
            arguments = Screen.ContactDetails.arguments
        ) { navBackStackEntry ->
            val contactId = navBackStackEntry.arguments?.getLong(Screen.ContactDetails.argumentName)
            requireNotNull(contactId) { "contactId parameter missing" }
            ContactDetailsScreen(navController = navController, contactId = contactId)
        }
        composable(
            route = Screen.AddEditContact.route,
            arguments = Screen.AddEditContact.arguments
        ) { navBackStackEntry ->
            val contactId = Screen.getOptionalIdArg(navBackStackEntry.arguments, Screen.AddEditContact.argumentName)
            AddEditContactScreen(navController = navController, contactId = contactId)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PetManagerTheme {
        PetManagerAppNavigation()
    }
}
