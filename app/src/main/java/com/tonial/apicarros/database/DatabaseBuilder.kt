package com.tonial.apicarros.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {

    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context? = null): AppDatabase {
        return DatabaseBuilder.INSTANCE ?: synchronized(this){
            if(context == null){
                throw Exception("Context não pode ser nulo")
            }
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
            DatabaseBuilder.INSTANCE = instance
            instance
        }
    }

}