package com.noobzsociety.smsgames.ui.screens.init.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.components.init.NavNextButton
import com.noobzsociety.smsgames.ui.navigation.AppScreen

@Composable
fun ThemeScreen(
    navHostController: NavHostController,
) {
    ScreenBase(navHostController, stringResource(R.string.title_theme)) {
        Column {
            Text("TODO Theme")
            NavNextButton(navHostController, AppScreen.Init.PermissionsScreen)
        }
    }
}