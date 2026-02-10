package com.noobzsociety.smsgames.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class GamemodeDao {
    @Query("SELECT * FROM gamemode WHERE id = :id")
    protected abstract fun protectedGet(id: Long): Flow<RoomGamemode>
    fun get(id: Long): Flow<RoomGamemode> = protectedGet(id).distinctUntilChanged()

    @Query("SELECT * FROM gamemode")
    protected abstract fun protectedGetAll(): Flow<List<RoomGamemode>>
    fun getAll(): Flow<List<RoomGamemode>> = protectedGetAll().distinctUntilChanged()

    @Query("SELECT * FROM gamemode WHERE enabled")
    protected abstract fun protectedGetAllEnabled(): Flow<List<RoomGamemode>>
    fun getAllEnabled(): Flow<List<RoomGamemode>> = protectedGetAllEnabled().distinctUntilChanged()

    @Upsert
    abstract suspend fun upsert(gamemode: RoomGamemode): Long
}