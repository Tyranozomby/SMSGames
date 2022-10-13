package com.tyrano.smsgames.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.dao.PlayerDao
import com.tyrano.smsgames.entities.PlayerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject

interface IGameVM {
    var id: Long
    var gamemodeName: String
    var owner: PlayerEntity
    var start: Instant?
    var players: List<PlayerEntity>
    var data: JSONObject
    var settings: JSONObject
    override fun toString(): String
}

@HiltViewModel
class GameVM @Inject constructor(private val gameDao: GameDao, private val playerDao: PlayerDao) : ViewModel(),
    IGameVM {
    override var id: Long by mutableStateOf(0)
    override var gamemodeName: String by mutableStateOf("Nom de jeu")
    override var owner: PlayerEntity by mutableStateOf(PlayerEntity(1, "nom", false, "+33", 4))
    override var start: Instant? by mutableStateOf(Instant.now().minusSeconds(6000))
    override var players: List<PlayerEntity> by mutableStateOf(
        listOf(
            owner,
            PlayerEntity(2, "random", false, "+33", 5)
        )
    )
    override var data: JSONObject by mutableStateOf(JSONObject())
    override var settings: JSONObject by mutableStateOf(JSONObject())

    fun get(gameId: Long) {
        if (gameId != 0L) {
            viewModelScope.launch {
                val detailed = gameDao.getDetailed(gameId)
                Log.d("TAG", "get: $detailed")
                id = detailed.id
                gamemodeName = detailed.gamemodeName
                owner = playerDao.getById(detailed.ownerId)
                start = detailed.start
                players = gameDao.getAllInGame(id)
                data = detailed.data
                settings = detailed.settings
            }
        }
    }

    override fun toString(): String {
        return "GameVM(id=$id, gamemodeName='$gamemodeName', owner=$owner, start=$start, teams=$players, data=${
            data.toString(
                2
            )
        }, settings=${settings.toString(2)})"
    }
}