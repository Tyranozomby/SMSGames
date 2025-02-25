package com.noobzsociety.smsgames.koin

import com.noobzsociety.smsgames.presentation.HomeViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single {
        HomeViewModel()
    }
}