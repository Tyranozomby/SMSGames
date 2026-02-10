package com.noobzsociety.smsgames.ui.screens.gamemodes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobzsociety.smsgames.data.room.AppDatabase
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GamemodeListViewModel(
    database: AppDatabase
) : ViewModel() {
    private val gamemodeDao = database.gamemodeDao()

    val gamemodes: StateFlow<List<RoomGamemode>?> = channelFlow {
        gamemodeDao.getAll().collectLatest {
            send(it)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun toggleGamemode(gamemode: RoomGamemode, enabled: Boolean) {
        viewModelScope.launch {
            gamemodeDao.upsert(gamemode.copy(enabled = enabled))
        }
    }
}