package com.tyrano.smsgames.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.engine.IncomingSMSHandler
import com.tyrano.smsgames.light.LightGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ILightGameListVM {
    val lightGames: Flow<List<LightGame>>
    fun delete(lightGame: LightGame)
}

@HiltViewModel
class LightGameListVM @Inject constructor(private val gameDao: GameDao, private val smsHandler: IncomingSMSHandler) :
    ViewModel(), ILightGameListVM {

    override val lightGames = gameDao.getAllLight()

    override fun delete(lightGame: LightGame) {
        viewModelScope.launch {
            gameDao.delete(lightGame.id)
            smsHandler.gameManager.games[lightGame.id]!!.cleanupGame()
        }
    }
}