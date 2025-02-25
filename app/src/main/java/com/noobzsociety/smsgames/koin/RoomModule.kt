package com.noobzsociety.smsgames.koin

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.noobzsociety.smsgames.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

private const val DB_NAME = "sms_games.db"

val roomModule = module {
    single<AppDatabase>(createdAtStart = true) {
        val appContext = get<Context>().applicationContext
        val dbFile = appContext.getDatabasePath(DB_NAME)

        Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        ).addMigrations()
            .fallbackToDestructiveMigration(true) // fixme: À supprimer (dev)
            .setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}