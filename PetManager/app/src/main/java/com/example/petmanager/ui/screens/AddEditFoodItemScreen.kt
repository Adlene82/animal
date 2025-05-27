package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun AddEditFoodItemScreen(navController: NavController, foodItemId: Long?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (foodItemId != null) {
            Text("Add/Edit Food Item Screen for food item ID: $foodItemId")
        } else {
            Text("Add New Food Item Screen")
        }
    }
}
