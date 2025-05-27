package com.example.petmanager.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["animalId"],
            childColumns = ["animalId"], // Corrected: child column name should match the field name
            onDelete = ForeignKey.SET_NULL // Or CASCADE, depending on desired behavior
        ),
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["contactId"],
            childColumns = ["contactId"], // Corrected: child column name should match the field name
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["animalId"]), Index(value = ["contactId"])]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long = 0,
    @ColumnInfo(index = true) // Ensure animalId is indexed for foreign key performance
    val animalId: Long?,
    val title: String,
    val dateTime: LocalDateTime,
    val eventType: String, // e.g., "VACCIN", "TRAITEMENT", "RDV_VETO", "ANNIVERSAIRE", "AUTRE"
    val description: String?,
    val location: String?,
    @ColumnInfo(defaultValue = "0") // SQLite stores booleans as integers 0 or 1
    val isRecurring: Boolean = false,
    @ColumnInfo(index = true) // Ensure contactId is indexed for foreign key performance
    val contactId: Long?
)
