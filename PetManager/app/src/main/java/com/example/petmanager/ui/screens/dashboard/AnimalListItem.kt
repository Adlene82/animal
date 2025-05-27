package com.example.petmanager.ui.screens.dashboard

data class AnimalListItem(
    val id: Long,
    val name: String,
    val species: String,
    val breed: String?,
    val age: String,
    val photoUri: String?,
    val quickStatus: String? // e.g., "Prochain vaccin: Demain", "Anniversaire !"
)
