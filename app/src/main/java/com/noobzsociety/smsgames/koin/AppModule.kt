package com.noobzsociety.smsgames.koin

import org.koin.core.KoinApplication

fun KoinApplication.setModules() = run {
    modules(
        managerModule,
        viewModelModule,
        roomModule
    )
}