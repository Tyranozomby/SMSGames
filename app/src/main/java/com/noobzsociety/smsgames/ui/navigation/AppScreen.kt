package com.noobzsociety.smsgames.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {

    @Serializable
    data object Test : AppScreen

    @Serializable
    data object Init : AppScreen {
        @Serializable
        data object ThemeScreen : AppScreen

        @Serializable
        data object PermissionsScreen : AppScreen

        @Serializable
        data object UserInfosScreen : AppScreen
    }

    @Serializable
    data object Gamemode : AppScreen {
        @Serializable
        data object ListScreen : AppScreen

        @Serializable
        data class DetailsScreen(val gamemodeId: Long?) : AppScreen
    }

    @Serializable
    data object Game : AppScreen {
        @Serializable
        data object ListScreen : AppScreen

        @Serializable
        data class DetailsScreen(val gameId: Long?) : AppScreen
    }

    @Serializable
    data object Files : AppScreen
}