package com.noobzsociety.smsgames

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.noobzsociety.smsgames.data.datastore.AppSettings
import com.noobzsociety.smsgames.koin.setModules
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
fun App() {
    val context = LocalContext.current

    KoinApplication(application = {
        androidContext(context)
        setModules()
    }) {
        SMSGamesTheme {
            AppNavigator()
        }
    }
}
