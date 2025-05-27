package com.example.petmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petmanager.data.local.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteById(eventId: Long)

    @Query("SELECT * FROM events WHERE eventId = :eventId")
    fun getEventById(eventId: Long): Flow<Event?>

    @Query("SELECT * FROM events ORDER BY dateTime DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE animalId = :animalId ORDER BY dateTime DESC")
    fun getEventsForAnimal(animalId: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE dateTime BETWEEN :startDateTime AND :endDateTime ORDER BY dateTime DESC")
    fun getEventsForDateRange(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Flow<List<Event>>
}
