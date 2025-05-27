package com.example.petmanager.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val foodItemId: Long = 0,
    val name: String,
    val brand: String?,
    val type: String, // e.g., "Croquettes", "Pâtée", "Graines"
    val targetSpecies: String?, // For which animal species this food is for
    val initialQuantity: Double,
    var currentQuantity: Double,
    val quantityUnit: String, // e.g., "kg", "g", "lbs"
    val lowStockThreshold: Double?,
    val photoUri: String?
)
