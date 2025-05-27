package com.example.petmanager.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AnimalDetails : Screen("animal_details/{animalId}") {
        fun createRoute(animalId: Long) = "animal_details/$animalId"
        val argumentName = "animalId"
        val arguments = listOf(navArgument(argumentName) { type = NavType.LongType })
    }
    object AddEditAnimal : Screen("add_edit_animal?animalId={animalId}") {
        fun createRoute(animalId: Long? = null): String {
            return if (animalId != null) "add_edit_animal?animalId=$animalId" else "add_edit_animal"
        }
        val argumentName = "animalId"
        val arguments = listOf(navArgument(argumentName) {
            type = NavType.LongType
            nullable = true
            defaultValue = -1L // Default for optional Long argument
        })
    }
    object FoodStock : Screen("food_stock")
    object AddEditFoodItem : Screen("add_edit_food_item?foodItemId={foodItemId}") {
        fun createRoute(foodItemId: Long? = null): String {
            return if (foodItemId != null) "add_edit_food_item?foodItemId=$foodItemId" else "add_edit_food_item"
        }
        val argumentName = "foodItemId"
        val arguments = listOf(navArgument(argumentName) {
            type = NavType.LongType
            nullable = true
            defaultValue = -1L
        })
    }
    object Calendar : Screen("calendar")
    object AddEditEvent : Screen("add_edit_event?eventId={eventId}") {
        fun createRoute(eventId: Long? = null): String {
            return if (eventId != null) "add_edit_event?eventId=$eventId" else "add_edit_event"
        }
        val argumentName = "eventId"
        val arguments = listOf(navArgument(argumentName) {
            type = NavType.LongType
            nullable = true
            defaultValue = -1L
        })
    }
    object Contacts : Screen("contacts")
    object ContactDetails : Screen("contact_details/{contactId}") { // Corrected route
        fun createRoute(contactId: Long) = "contact_details/$contactId"
        val argumentName = "contactId"
        val arguments = listOf(navArgument(argumentName) { type = NavType.LongType })
    }
    object AddEditContact : Screen("add_edit_contact?contactId={contactId}") {
        fun createRoute(contactId: Long? = null): String {
            return if (contactId != null) "add_edit_contact?contactId=$contactId" else "add_edit_contact"
        }
        val argumentName = "contactId"
        val arguments = listOf(navArgument(argumentName) {
            type = NavType.LongType
            nullable = true
            defaultValue = -1L
        })
    }

    // Helper to extract optional argument
    companion object {
        fun getOptionalIdArg(arguments: android.os.Bundle?, argName: String): Long? {
            val id = arguments?.getLong(argName, -1L)
            return if (id == -1L) null else id
        }
    }
}
