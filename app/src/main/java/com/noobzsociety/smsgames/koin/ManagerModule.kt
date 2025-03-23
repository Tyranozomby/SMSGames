package com.noobzsociety.smsgames.koin

import com.noobzsociety.smsgames.engine.SMSHandler
import org.koin.dsl.module

val managerModule = module {
    single { SMSHandler(get(), get()) }
}