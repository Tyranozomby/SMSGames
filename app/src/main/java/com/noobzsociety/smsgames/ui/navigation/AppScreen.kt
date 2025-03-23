package com.noobzsociety.smsgames.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {

    @Serializable
    data object PermissionsScreen : AppScreen

    @Serializable
    data object HomeScreen : AppScreen
}