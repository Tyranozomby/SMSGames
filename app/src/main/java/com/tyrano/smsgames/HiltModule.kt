package com.tyrano.smsgames

import android.content.Context
import androidx.room.Room
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.dao.PlayerDao
import com.tyrano.smsgames.engine.IncomingSMSHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    @Provides
    @Singleton
    fun provideGamemodeDao(database: SmsGameDatabase): GamemodeDao {
        return database.gamemodeDao()
    }

    @Provides
    @Singleton
    fun provideGameDao(database: SmsGameDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun providePlayerDao(database: SmsGameDatabase): PlayerDao {
        return database.playerDao()
    }

    @Provides
    @Singleton
    fun provideIncomingSMSHandler(database: SmsGameDatabase, @ApplicationContext context: Context): IncomingSMSHandler {
        return IncomingSMSHandler(database, context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmsGameDatabase {
        return Room.databaseBuilder(context, SmsGameDatabase::class.java, "sms_game_database").build()
    }
}