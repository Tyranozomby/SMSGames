package com.tyrano.smsgames.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = DarkerGrey,
    primaryVariant = DarkGrey,
    secondary = Teal200,
    background = Color.DarkGray,
    surface = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColors(
    primary = PrimaryGreen,
    primaryVariant = SecondaryGreen,
    secondary = Teal200,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

lateinit var themeColors: Colors
lateinit var systemUiController: SystemUiController

@Composable
fun SMSGamesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    themeColors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(themeColors.primary)

    MaterialTheme(
        colors = themeColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}