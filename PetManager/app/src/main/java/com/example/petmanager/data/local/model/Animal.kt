package com.example.petmanager.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val animalId: Long = 0,
    val name: String,
    val species: String,
    val breed: String?,
    val birthDate: LocalDate,
    val sex: String, // "Male", "Female", "Unknown"
    val photoUri: String?,
    val chipId: String?,
    val diet: String?,
    val allergies: String?,
    val generalNotes: String?
)
