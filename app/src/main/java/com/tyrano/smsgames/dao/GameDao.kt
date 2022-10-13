package com.tyrano.smsgames.dao

import androidx.room.*
import com.tyrano.smsgames.entities.GameEntity
import com.tyrano.smsgames.entities.PlayerEntity
import com.tyrano.smsgames.light.LightGame
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

@Dao
interface GameDao {

    @Query("SELECT * FROM game WHERE id = :id")
    suspend fun get(id: Long): GameEntity

    @Query("SELECT game.id, gamemode.name gameName, player.name ownerName, start FROM game, gamemode, player WHERE gamemode_id = gamemode.id AND owner_id = player.id")
    fun getAllLight(): Flow<List<LightGame>>

    @Query("SELECT * FROM player WHERE player.game_id = :gameId")
    suspend fun getAllInGame(gameId: Long): List<PlayerEntity>

    @Query("SELECT * FROM game")
    suspend fun getAll(): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(newGame: GameEntity): Long

    @Update
    suspend fun update(modifiedGame: GameEntity)

    @Query("DELETE FROM game WHERE id = :id ")
    suspend fun delete(id: Long)

    @Query("UPDATE game SET data = :newData WHERE id = :gameId")
    suspend fun saveDataOf(gameId: Long, newData: JSONObject)

    @Query("UPDATE game SET players_order = :newOrder WHERE id = :gameId")
    suspend fun savePlayersOrderOf(gameId: Long, newOrder: JSONArray)

    @Query("SELECT game.id id, game.owner_id ownerId, game.data data, game.settings settings, game.start start, gamemode.name gamemodeName FROM game, gamemode WHERE gamemode_id = gamemode.id AND game.id = :gameId")
    suspend fun getDetailed(gameId: Long): DetailedGame

    @Query("UPDATE game SET start = :time WHERE id = :id")
    suspend fun start(id: Long, time: Long)

    data class DetailedGame(
        val id: Long,
        val ownerId: Long,
        val data: JSONObject,
        val settings: JSONObject,
        val start: Instant?,
        val gamemodeName: String
    ) {
        override fun toString(): String {
            return "DetailedGame(id=$id, ownerId=$ownerId, data=$data, settings=$settings, start=$start, gamemodeName='$gamemodeName')"
        }
    }
}
