package com.noobzsociety.smsgames.presentation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.noobzsociety.smsgames.components.CodeEditor
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeViewModel = koinViewModel()
) {
    Column {
        CodeEditor("function test") {
            println("------")
            println(it)
            println("------")
        }
    }
}

@Preview("Light")
@Preview("Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    SMSGamesTheme {
        HomeScreen(
            navHostController = rememberNavController(),
            viewModel = HomeViewModel()
        )
    }
}