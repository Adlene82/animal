package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petmanager.ui.navigation.Screen
import com.example.petmanager.ui.theme.PetManagerTheme


@Composable
fun FoodStockScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditFoodItem.createRoute())
            }) {
                Icon(Icons.Filled.Add, "Add new food item")
            }
        }
    ) { paddingValues -> // Renamed to avoid conflict with androidx.compose.foundation.layout.padding
        Box(
            modifier = Modifier.fillMaxSize(), // Apply padding if needed from Scaffold
            contentAlignment = Alignment.Center
        ) {
            Text("Food Stock Screen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodStockScreenPreview() {
    PetManagerTheme {
        FoodStockScreen(navController = rememberNavController())
    }
}
