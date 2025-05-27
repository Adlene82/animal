package com.example.petmanager.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val contactId: Long = 0,
    val name: String,
    val category: String, // e.g., "Vétérinaire", "Toiletteur", "Pension"
    val specialty: String?,
    val phoneNumber: String?,
    val emailAddress: String?,
    val address: String?,
    val website: String?,
    @ColumnInfo(defaultValue = "0") // SQLite stores booleans as integers 0 or 1
    val isFavorite: Boolean = false,
    val notes: String?
)
