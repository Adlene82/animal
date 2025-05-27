package com.example.petmanager.data.repository

import com.example.petmanager.data.local.dao.AnimalDao
import com.example.petmanager.data.local.dao.ContactDao
import com.example.petmanager.data.local.dao.EventDao
import com.example.petmanager.data.local.dao.FoodItemDao
import com.example.petmanager.data.local.dao.WeightRecordDao
import com.example.petmanager.data.local.model.Animal
import com.example.petmanager.data.local.model.Contact
import com.example.petmanager.data.local.model.Event
import com.example.petmanager.data.local.model.FoodItem
import com.example.petmanager.data.local.model.WeightRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val animalDao: AnimalDao,
    private val weightRecordDao: WeightRecordDao,
    private val eventDao: EventDao,
    private val foodItemDao: FoodItemDao,
    private val contactDao: ContactDao
) : PetRepository {

    // Animal operations
    override fun getAllAnimals(): Flow<List<Animal>> = animalDao.getAllAnimals()
    override fun getAnimalById(animalId: Long): Flow<Animal?> = animalDao.getAnimalById(animalId)
    override suspend fun insertAnimal(animal: Animal): Long = animalDao.insert(animal)
    override suspend fun updateAnimal(animal: Animal) = animalDao.update(animal)
    override suspend fun deleteAnimal(animal: Animal) = animalDao.delete(animal)
    override suspend fun deleteAnimalById(animalId: Long) = animalDao.deleteById(animalId)

    // Weight Record operations
    override fun getWeightRecordsForAnimal(animalId: Long): Flow<List<WeightRecord>> =
        weightRecordDao.getAllWeightRecordsForAnimal(animalId)
    override fun getAllWeightRecords(): Flow<List<WeightRecord>> = weightRecordDao.getAllWeightRecords()
    override fun getWeightRecordById(weightRecordId: Long): Flow<WeightRecord?> =
        weightRecordDao.getWeightRecordById(weightRecordId)
    override suspend fun insertWeightRecord(weightRecord: WeightRecord): Long =
        weightRecordDao.insert(weightRecord)
    override suspend fun updateWeightRecord(weightRecord: WeightRecord) =
        weightRecordDao.update(weightRecord)
    override suspend fun deleteWeightRecord(weightRecord: WeightRecord) =
        weightRecordDao.delete(weightRecord)
    override suspend fun deleteWeightRecordById(weightRecordId: Long) =
        weightRecordDao.deleteById(weightRecordId)

    // Event operations
    override fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    override fun getEventsForAnimal(animalId: Long): Flow<List<Event>> =
        eventDao.getEventsForAnimal(animalId)
    override fun getEventById(eventId: Long): Flow<Event?> = eventDao.getEventById(eventId)
    override suspend fun insertEvent(event: Event): Long = eventDao.insert(event)
    override suspend fun updateEvent(event: Event) = eventDao.update(event)
    override suspend fun deleteEvent(event: Event) = eventDao.delete(event)
    override suspend fun deleteEventById(eventId: Long) = eventDao.deleteById(eventId)

    // Food Item operations
    override fun getAllFoodItems(): Flow<List<FoodItem>> = foodItemDao.getAllFoodItems()
    override fun getFoodItemById(foodItemId: Long): Flow<FoodItem?> =
        foodItemDao.getFoodItemById(foodItemId)
    override suspend fun insertFoodItem(foodItem: FoodItem): Long = foodItemDao.insert(foodItem)
    override suspend fun updateFoodItem(foodItem: FoodItem) = foodItemDao.update(foodItem)
    override suspend fun deleteFoodItem(foodItem: FoodItem) = foodItemDao.delete(foodItem)
    override suspend fun deleteFoodItemById(foodItemId: Long) = foodItemDao.deleteById(foodItemId)

    // Contact operations
    override fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()
    override fun getContactById(contactId: Long): Flow<Contact?> =
        contactDao.getContactById(contactId)
    override fun getFavoriteContacts(): Flow<List<Contact>> = contactDao.getFavoriteContacts()
    override suspend fun insertContact(contact: Contact): Long = contactDao.insert(contact)
    override suspend fun updateContact(contact: Contact) = contactDao.update(contact)
    override suspend fun deleteContact(contact: Contact) = contactDao.delete(contact)
    override suspend fun deleteContactById(contactId: Long) = contactDao.deleteById(contactId)
}
