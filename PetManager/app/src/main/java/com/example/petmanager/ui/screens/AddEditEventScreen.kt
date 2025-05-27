package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun AddEditEventScreen(navController: NavController, eventId: Long?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (eventId != null) {
            Text("Add/Edit Event Screen for event ID: $eventId")
        } else {
            Text("Add New Event Screen")
        }
    }
}
