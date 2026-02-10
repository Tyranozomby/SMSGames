package com.noobzsociety.smsgames.ui.screens.init.userinfos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobzsociety.smsgames.data.datastore.AppSettings
import com.noobzsociety.smsgames.data.room.AppDatabase
import com.noobzsociety.smsgames.data.room.dao.PlayerDao
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import com.noobzsociety.smsgames.engine.PhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserInfosViewModel(
    private val settings: AppSettings,
    database: AppDatabase,
) : ViewModel() {
    private val playerDao: PlayerDao = database.playerDao()

    private val _name: MutableStateFlow<String> = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phoneNumber: MutableStateFlow<String?> = MutableStateFlow(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber.asStateFlow()

    val isEveryFieldFilled: StateFlow<Boolean> = channelFlow {
        combine(_name, _phoneNumber) { name, phoneNumber ->
            name.isNotBlank() && phoneNumber != null
        }.collect { send(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setName(name: String) {
        _name.value = name
    }

    fun setPhoneNumber(phoneNumber: String?) {
        _phoneNumber.value = phoneNumber
    }

    fun save() {
        viewModelScope.launch {
            val id = playerDao.insert(
                RoomPlayer(
                    name = _name.value.trim(),
                    phoneNumber = PhoneNumber(_phoneNumber.value!!.trim()),
                    isAdmin = true,
                    isTester = true
                )
            )

            assert(id == 1L) {
                "Player ID should be 1, but was $id"
            }

            settings.isInitDone.value = true
        }
    }
}