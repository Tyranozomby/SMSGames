package com.noobzsociety.smsgames.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noobzsociety.smsgames.room.converters.DefaultConverters
import com.noobzsociety.smsgames.room.entities.RoomGame

@TypeConverters(DefaultConverters::class)
@Database(
    entities = [
        RoomGame::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase()