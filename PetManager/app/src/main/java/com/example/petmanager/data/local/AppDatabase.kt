package com.example.petmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

@Database(
    entities = [
        Animal::class,
        WeightRecord::class,
        Event::class,
        FoodItem::class,
        Contact::class
    ],
    version = 1,
    exportSchema = false // Recommended to disable for non-library modules
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun eventDao(): EventDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_manager_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this task.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
