package com.noobzsociety.smsgames.koin

import com.noobzsociety.smsgames.data.datastore.AppSettings
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
val settingsModule = module {
    single<ObservableSettings> { Settings().makeObservable() }
    single { AppSettings(get()) }
}
