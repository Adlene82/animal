package com.example.petmanager.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "weight_records",
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["animalId"],
        childColumns = ["animalOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["animalOwnerId"])]
)
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val weightRecordId: Long = 0,
    val animalOwnerId: Long, // Foreign key to Animal
    val date: LocalDate,
    val weightInKg: Double
)
