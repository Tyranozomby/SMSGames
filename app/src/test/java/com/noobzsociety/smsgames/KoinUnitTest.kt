package com.noobzsociety.smsgames

import com.noobzsociety.smsgames.koin.engineModule
import com.noobzsociety.smsgames.koin.roomModule
import com.noobzsociety.smsgames.koin.viewModelModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class CheckModulesTest : KoinTest {

    @Test
    fun checkManagerModule() {
        engineModule.verify()
    }

    @Test
    fun checkViewModelModule() {
        viewModelModule.verify()
    }

    @Test
    fun checkRoomModule() {
        roomModule.verify()
    }
}