package com.noobzsociety.smsgames.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer

@Dao
interface PlayerDao {

    @Query("SELECT  * FROM player WHERE id = :id")
    suspend fun get(id: Long): RoomPlayer

    @Query("SELECT * FROM player WHERE phone_number = :number")
    suspend fun getByNumber(number: String): RoomPlayer?
}