package com.noobzsociety.smsgames.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {

    @Serializable
    data object HomeScreen : AppScreen
}