package com.noobzsociety.smsgames.koin

import com.noobzsociety.smsgames.data.FileManager
import org.koin.dsl.module

val managersModule = module {
    single<FileManager> { FileManager(get()) }
}