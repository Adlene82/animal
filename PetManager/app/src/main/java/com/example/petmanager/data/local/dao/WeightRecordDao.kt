package com.example.petmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petmanager.data.local.model.WeightRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weightRecord: WeightRecord): Long

    @Update
    suspend fun update(weightRecord: WeightRecord)

    @Delete
    suspend fun delete(weightRecord: WeightRecord)

    @Query("DELETE FROM weight_records WHERE weightRecordId = :weightRecordId")
    suspend fun deleteById(weightRecordId: Long)

    @Query("SELECT * FROM weight_records WHERE weightRecordId = :weightRecordId")
    fun getWeightRecordById(weightRecordId: Long): Flow<WeightRecord?>

    @Query("SELECT * FROM weight_records WHERE animalOwnerId = :animalId ORDER BY date DESC")
    fun getAllWeightRecordsForAnimal(animalId: Long): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records ORDER BY date DESC")
    fun getAllWeightRecords(): Flow<List<WeightRecord>>
}
