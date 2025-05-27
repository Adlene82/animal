package com.example.petmanager.data.repository

import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.data.local.model.FoodItem
import com.example.petmanager.data.local.model.WeightRecord
import kotlinx.coroutines.flow.Flow

interface PetRepository {

    // Animal operations
    fun getAllAnimals(): Flow<List<Animal>>
    fun getAnimalById(animalId: Long): Flow<Animal?> // Changed to Flow as per DAO
    suspend fun insertAnimal(animal: Animal): Long
    suspend fun updateAnimal(animal: Animal)
    suspend fun deleteAnimal(animal: Animal)
    suspend fun deleteAnimalById(animalId: Long)

    // Weight Record operations
    fun getWeightRecordsForAnimal(animalId: Long): Flow<List<WeightRecord>>
    fun getAllWeightRecords(): Flow<List<WeightRecord>> // Added from DAO
    fun getWeightRecordById(weightRecordId: Long): Flow<WeightRecord?> // Added from DAO
    suspend fun insertWeightRecord(weightRecord: WeightRecord): Long
    suspend fun updateWeightRecord(weightRecord: WeightRecord) // Added from DAO
    suspend fun deleteWeightRecord(weightRecord: WeightRecord)
    suspend fun deleteWeightRecordById(weightRecordId: Long) // Added from DAO

    // Event operations
    fun getAllEvents(): Flow<List<Event>>
    fun getEventsForAnimal(animalId: Long): Flow<List<Event>>
    fun getEventById(eventId: Long): Flow<Event?> // Changed to Flow as per DAO
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun deleteEventById(eventId: Long) // Added from DAO

    // Food Item operations
    fun getAllFoodItems(): Flow<List<FoodItem>>
    fun getFoodItemById(foodItemId: Long): Flow<FoodItem?> // Changed to Flow as per DAO
    suspend fun insertFoodItem(foodItem: FoodItem): Long
    suspend fun updateFoodItem(foodItem: FoodItem)
    suspend fun deleteFoodItem(foodItem: FoodItem)
    suspend fun deleteFoodItemById(foodItemId: Long) // Added from DAO

    // Contact operations
    fun getAllContacts(): Flow<List<Contact>>
    fun getContactById(contactId: Long): Flow<Contact?> // Changed to Flow as per DAO
    fun getFavoriteContacts(): Flow<List<Contact>> // Added from DAO
    suspend fun insertContact(contact: Contact): Long
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(contact: Contact)
    suspend fun deleteContactById(contactId: Long) // Added from DAO
}
