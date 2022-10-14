package com.tyrano.smsgames

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.tyrano.smsgames.converters.InstantConverter
import com.tyrano.smsgames.converters.JsonConverter
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.dao.PlayerDao
import com.tyrano.smsgames.entities.GameEntity
import com.tyrano.smsgames.entities.GamemodeEntity
import com.tyrano.smsgames.entities.PlayerEntity

@Database(
    entities = [GamemodeEntity::class, GameEntity::class, PlayerEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = SmsGameDatabase.MyAutoMigration::class),
        AutoMigration(from = 2, to = 3, spec = SmsGameDatabase.MyAutoMigration::class)
    ]
)
@TypeConverters(value = [InstantConverter::class, JsonConverter::class])
abstract class SmsGameDatabase : RoomDatabase() {
    class MyAutoMigration : AutoMigrationSpec

    abstract fun gamemodeDao(): GamemodeDao
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
}