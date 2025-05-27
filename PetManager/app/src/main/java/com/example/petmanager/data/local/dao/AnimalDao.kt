package com.example.petmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petmanager.data.local.model.Animal
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(animal: Animal): Long

    @Update
    suspend fun update(animal: Animal)

    @Delete
    suspend fun delete(animal: Animal)

    @Query("DELETE FROM animals WHERE animalId = :animalId")
    suspend fun deleteById(animalId: Long)

    @Query("SELECT * FROM animals WHERE animalId = :animalId")
    fun getAnimalById(animalId: Long): Flow<Animal?>

    @Query("SELECT * FROM animals ORDER BY name ASC")
    fun getAllAnimals(): Flow<List<Animal>>
}
