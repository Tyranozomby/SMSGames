package com.noobzsociety.smsgames.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class PlayerDao {

    @Query("SELECT  * FROM player WHERE id = :id")
    protected abstract fun protectedGet(id: Long): Flow<RoomPlayer>
    fun get(id: Long): Flow<RoomPlayer> = protectedGet(id).distinctUntilChanged()

    @Query("SELECT * FROM player WHERE phone_number = :number")
    protected abstract fun protectedGetByNumber(number: String): Flow<RoomPlayer?>
    fun getByNumber(number: String): Flow<RoomPlayer?> = protectedGetByNumber(number).distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(player: RoomPlayer): Long

    @Update
    abstract suspend fun update(player: RoomPlayer)
}