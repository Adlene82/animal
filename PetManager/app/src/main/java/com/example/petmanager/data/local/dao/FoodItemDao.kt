package com.example.petmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petmanager.data.local.model.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem): Long

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("DELETE FROM food_items WHERE foodItemId = :foodItemId")
    suspend fun deleteById(foodItemId: Long)

    @Query("SELECT * FROM food_items WHERE foodItemId = :foodItemId")
    fun getFoodItemById(foodItemId: Long): Flow<FoodItem?>

    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>
}
