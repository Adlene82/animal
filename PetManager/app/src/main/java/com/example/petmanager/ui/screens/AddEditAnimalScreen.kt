package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun AddEditAnimalScreen(navController: NavController, animalId: Long?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (animalId != null) {
            Text("Add/Edit Animal Screen for animal ID: $animalId")
        } else {
            Text("Add New Animal Screen")
        }
    }
}
