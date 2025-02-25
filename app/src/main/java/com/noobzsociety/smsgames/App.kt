package com.noobzsociety.smsgames

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.noobzsociety.smsgames.koin.setModules
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication

@Composable
fun App() {
    KoinApplication(application = KoinApplication::setModules) {
        SMSGamesTheme {
            AppNavigator()
        }
    }
}

@PreviewLightDark
@Composable
private fun FullPreview() {
    SMSGamesTheme {
        AppNavigator()
    }
}