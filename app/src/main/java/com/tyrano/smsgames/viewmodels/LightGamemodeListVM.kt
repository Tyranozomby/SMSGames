package com.tyrano.smsgames.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.light.LightGamemode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ILightGamemodeListVM {
    val lightGamemodes: Flow<List<LightGamemode>>
    fun toggle(lightGamemode: LightGamemode)
    fun delete(lightGamemode: LightGamemode)
}

@HiltViewModel
class LightGamemodeListVM @Inject constructor(private val dao: GamemodeDao) : ViewModel(), ILightGamemodeListVM {

    override val lightGamemodes = dao.getAllLight()

    override fun toggle(lightGamemode: LightGamemode) {
        viewModelScope.launch {
            dao.toggle(lightGamemode.id)
        }
    }

    override fun delete(lightGamemode: LightGamemode) {
        viewModelScope.launch {
            dao.delete(lightGamemode)
        }
    }
}