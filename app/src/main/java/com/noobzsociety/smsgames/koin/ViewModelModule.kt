package com.noobzsociety.smsgames.koin

import com.noobzsociety.smsgames.ui.screens.home.HomeViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single {
        HomeViewModel()
    }
}