package com.noobzsociety.smsgames.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.data.room.entities.RoomPlayerToGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class GameDao {
    @Query("SELECT * FROM game WHERE id = :id")
    protected abstract fun protectedGet(id: Long): Flow<RoomGame>

    fun get(id: Long): Flow<RoomGame> = protectedGet(id).distinctUntilChanged()

//    @Query("SELECT * FROM game WHERE gamemode_id = :gamemodeId")
//    protected abstract fun protectedGetByGamemodeId(gamemodeId: Long): Flow<List<RoomGame>>
//
//    fun getByGamemodeId(gamemodeId: Long): Flow<List<RoomGame>> =
//        protectedGetByGamemodeId(gamemodeId).distinctUntilChanged()

    @Query("SELECT game.* FROM game JOIN player_to_game ON game.id = player_to_game.game_id WHERE player_to_game.player_id = :playerId")
    protected abstract fun protectedGetPlayerGame(playerId: Long): Flow<RoomGame?>

    fun getPlayerGame(playerId: Long): Flow<RoomGame?> =
        protectedGetPlayerGame(playerId).distinctUntilChanged()

    @Insert
    protected abstract suspend fun protectedInsert(game: RoomGame): Long

    @Insert
    protected abstract suspend fun protectedInsertGameToPlayer(gameToPlayerDao: RoomPlayerToGame): Long

    @Transaction
    open suspend fun insert(game: RoomGame, playerId: Long): Long {
        val gameId = protectedInsert(game)
        protectedInsertGameToPlayer(RoomPlayerToGame(playerId, gameId))
        return gameId
    }

//    @Query("UPDATE game SET data = :newData WHERE id = :gameId")
//    abstract fun updateDataOf(gameId: Long, newData: JSONObject)

    @Update
    abstract fun update(game: RoomGame)

    @Delete
    abstract fun delete(game: RoomGame)
}
