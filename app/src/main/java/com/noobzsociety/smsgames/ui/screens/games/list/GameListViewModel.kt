package com.noobzsociety.smsgames.ui.screens.games.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.engine.GameManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GameListViewModel(
    gameManager: GameManager
) : ViewModel() {
    val games: StateFlow<List<RoomGame>?> = gameManager.games.map { games ->
        val map: List<RoomGame> = games.values.map { it.game }
        map
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}