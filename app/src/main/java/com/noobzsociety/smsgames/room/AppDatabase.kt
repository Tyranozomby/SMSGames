package com.noobzsociety.smsgames.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noobzsociety.smsgames.room.converters.DefaultConverters
import com.noobzsociety.smsgames.room.converters.JsonConverters
import com.noobzsociety.smsgames.room.entities.RoomGame
import com.noobzsociety.smsgames.room.entities.RoomGamemode
import com.noobzsociety.smsgames.room.entities.RoomPlayer

@TypeConverters(DefaultConverters::class, JsonConverters::class)
@Database(
    entities = [RoomGame::class, RoomGamemode::class, RoomPlayer::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase()