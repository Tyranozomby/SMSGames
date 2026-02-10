package com.noobzsociety.smsgames

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.noobzsociety.smsgames.data.datastore.AppSettings
import com.noobzsociety.smsgames.ui.navigation.AppScreen
import com.noobzsociety.smsgames.ui.screens.files.FilesScreen
import com.noobzsociety.smsgames.ui.screens.gamemodes.details.GamemodeDetailsScreen
import com.noobzsociety.smsgames.ui.screens.gamemodes.list.GamemodeListScreen
import com.noobzsociety.smsgames.ui.screens.games.list.GameListScreen
import com.noobzsociety.smsgames.ui.screens.init.permissions.PermissionsScreen
import com.noobzsociety.smsgames.ui.screens.init.theme.ThemeScreen
import com.noobzsociety.smsgames.ui.screens.init.userinfos.UserInfosScreen
import com.noobzsociety.smsgames.ui.screens.test.TestScreen
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalPermissionsApi::class, KoinExperimentalAPI::class)
@Composable
fun AppNavigator() {
    val settings: AppSettings = koinInject<AppSettings>()

    val navHostController: NavHostController = rememberNavController()

    val isInitDone by settings.isInitDone.flow.collectAsState(false)

    NavHost(
        modifier = Modifier.fillMaxSize(),
            navController = navHostController,
        startDestination = when (isInitDone) {
            true -> AppScreen.Gamemode.ListScreen
//            true -> AppScreen.Test
            false -> AppScreen.Init.ThemeScreen
        },
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = {
                ExitTransition.None
            }
    ) {
        composable<AppScreen.Test> {
            TestScreen(navHostController)
        }

        initScreens(navHostController)

        gamemodeScreens(navHostController)

        gameScreens(navHostController)

        composable<AppScreen.Files> {
            FilesScreen(navHostController)
        }
    }
}

private fun NavGraphBuilder.initScreens(navHostController: NavHostController) {
    composable<AppScreen.Init.ThemeScreen> {
        ThemeScreen(navHostController)
    }

    composable<AppScreen.Init.PermissionsScreen> {
        PermissionsScreen(navHostController)
    }

    composable<AppScreen.Init.UserInfosScreen> {
        UserInfosScreen(navHostController)
    }
}

private fun NavGraphBuilder.gamemodeScreens(navHostController: NavHostController) {
    composable<AppScreen.Gamemode.ListScreen> {
        GamemodeListScreen(navHostController)
    }

    composable<AppScreen.Gamemode.DetailsScreen> {
        GamemodeDetailsScreen(
            navHostController = navHostController,
        )
    }
}

private fun NavGraphBuilder.gameScreens(navHostController: NavHostController) {
    composable<AppScreen.Game.ListScreen> {
        GameListScreen(navHostController)
    }

    composable<AppScreen.Game.DetailsScreen> {
//        GameDetailsScreen(
//            navHostController = navHostController,
//        )
    }
}