package com.noobzsociety.smsgames.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode

@Dao
interface GamemodeDao {
    @Query("SELECT * FROM gamemode WHERE id = :id")
    suspend fun get(id: Long): RoomGamemode
}