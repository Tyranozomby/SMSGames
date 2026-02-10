package com.noobzsociety.smsgames.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noobzsociety.smsgames.data.room.converters.DefaultConverters
import com.noobzsociety.smsgames.data.room.converters.JsonConverters
import com.noobzsociety.smsgames.data.room.dao.GameDao
import com.noobzsociety.smsgames.data.room.dao.GamemodeDao
import com.noobzsociety.smsgames.data.room.dao.PlayerDao
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import com.noobzsociety.smsgames.data.room.entities.RoomPlayerToGame

@TypeConverters(DefaultConverters::class, JsonConverters::class)
@Database(
    entities = [RoomGame::class, RoomGamemode::class, RoomPlayer::class, RoomPlayerToGame::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun gamemodeDao(): GamemodeDao
    abstract fun playerDao(): PlayerDao
}