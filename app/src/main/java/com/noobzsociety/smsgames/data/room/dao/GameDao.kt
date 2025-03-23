package com.noobzsociety.smsgames.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.noobzsociety.smsgames.data.room.entities.RoomGame

@Dao
interface GameDao {
    @Query("SELECT * FROM game WHERE id = :id")
    suspend fun get(id: Long): RoomGame
}
