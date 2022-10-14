package com.tyrano.smsgames.dao

import androidx.room.*
import com.tyrano.smsgames.entities.GamemodeEntity
import com.tyrano.smsgames.light.LightGamemode
import kotlinx.coroutines.flow.Flow

@Dao
interface GamemodeDao {

    @Query("SELECT * FROM gamemode WHERE id = :id")
    suspend fun get(id: Long): GamemodeEntity

    @Query("SELECT id, name, enabled FROM gamemode")
    fun getAllLight(): Flow<List<LightGamemode>>

    @Query("SELECT * FROM gamemode")
    suspend fun getAll(): List<GamemodeEntity>

    @Query("SELECT * FROM gamemode WHERE enabled")
    suspend fun getAllEnabled(): List<GamemodeEntity>

    @Query("UPDATE gamemode SET enabled = not enabled WHERE id = :id")
    suspend fun toggle(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(newGamemode: GamemodeEntity): Long

    @Update
    suspend fun update(modifiedGamemode: GamemodeEntity)

    @Delete(entity = GamemodeEntity::class)
    suspend fun delete(gamemode: LightGamemode)

    @Query("SELECT COUNT(id) FROM game WHERE gamemode_id = :id")
    suspend fun countRelatedGames(id: Long) : Int
}