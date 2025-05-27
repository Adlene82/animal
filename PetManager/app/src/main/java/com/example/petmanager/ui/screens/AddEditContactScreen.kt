package com.example.petmanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun AddEditContactScreen(navController: NavController, contactId: Long?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (contactId != null) {
            Text("Add/Edit Contact Screen for contact ID: $contactId")
        } else {
            Text("Add New Contact Screen")
        }
    }
}
