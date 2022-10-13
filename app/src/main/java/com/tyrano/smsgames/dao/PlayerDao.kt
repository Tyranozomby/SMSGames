package com.tyrano.smsgames.dao

import androidx.room.*
import com.tyrano.smsgames.entities.GameEntity
import com.tyrano.smsgames.entities.GamemodeEntity
import com.tyrano.smsgames.entities.PlayerEntity

@Dao
interface PlayerDao {

    @Query("SELECT  * FROM player WHERE id = :id")
    suspend fun getById(id: Long): PlayerEntity

    @Query("SELECT * FROM player WHERE phone_number = :number")
    suspend fun getByNumber(number: String): PlayerEntity?

//    @Query("SELECT * FROM player WHERE tester")
//    suspend fun getTesters(): List<PlayerEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun register(newPlayer: PlayerEntity): Long

    @Delete
    suspend fun delete(player: PlayerEntity)

    @Query("UPDATE player SET name = :name WHERE id = :id")
    suspend fun updateNick(id: Long, name: String)

    @Query("SELECT gamemode.* FROM gamemode, game, player WHERE player.game_id = game.id AND game.gamemode_id = gamemode.id AND player.id = :playerId")
    suspend fun getPlayingGamemode(playerId: Long): GamemodeEntity?

    @Query("UPDATE player SET game_id = :gameId WHERE id = :id")
    suspend fun setGame(id: Long, gameId: Long)

    @Query("SELECT game.* FROM game, player WHERE player.game_id = game.id AND player.id = :playerId")
    suspend fun getPlayingGame(playerId: Long): GameEntity?
}