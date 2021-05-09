package com.example.google.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.google.data.AssitantDatabase.Companion.INSTANCE

@Database(entities = [Assistant::class], version = 1, exportSchema = false)
abstract class AssitantDatabase  : RoomDatabase() {

    abstract val assiatntDao: AssiatntDao

    companion object {
        @Volatile
        private var INSTANCE: AssitantDatabase? = null


        fun getInstance(context: Context): AssitantDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            AssitantDatabase::class.java,
                            "assistant_messages_database"
                    ).allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}