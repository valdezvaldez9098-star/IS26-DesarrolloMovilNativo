package com.example.practica14.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MarcadorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun marcadorDao(): MarcadorDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "practica14.db"
                ).build().also { INSTANCIA = it }
            }
        }
    }
}
