package com.tonial.apicarros.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tonial.apicarros.converter.DateConverters
import com.tonial.apicarros.database.dao.UserLocationDao
import com.tonial.apicarros.database.model.UserLocation

@Database(entities = [UserLocation::class], version = 1, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userLocationDao(): UserLocationDao


}