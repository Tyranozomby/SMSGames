package com.tyrano.smsgames.activities

import android.Manifest
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.tyrano.smsgames.*
import com.tyrano.smsgames.light.LightGame
import com.tyrano.smsgames.light.LightGamemode
import com.tyrano.smsgames.pager.Tabs
import com.tyrano.smsgames.pager.TabsContent
import com.tyrano.smsgames.pager.editOrNewGamemode
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import com.tyrano.smsgames.ui.theme.systemUiController
import com.tyrano.smsgames.ui.theme.themeColors
import com.tyrano.smsgames.viewmodels.ILightGameListVM
import com.tyrano.smsgames.viewmodels.ILightGamemodeListVM
import com.tyrano.smsgames.viewmodels.LightGameListVM
import com.tyrano.smsgames.viewmodels.LightGamemodeListVM
import com.whl.quickjs.android.QuickJSLoader
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    init {
        QuickJSLoader.init()
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            ), 1
        )

        setContent {
            SMSGamesTheme {
                MainInterface()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SMSGamesTheme {
        val gamemodeVM = remember {
            getPreviewLightGamemodeListVM()
        }
        val gameVM = remember {
            getPreviewLightGameListVM()
        }
        MainInterface(gamemodeVM, getPreviewLightGamemodeList(), gameVM, getPreviewLightGameList())
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainInterface(
    gamemodeVM: ILightGamemodeListVM = hiltViewModel<LightGamemodeListVM>(),
    gamemodeList: List<LightGamemode> = gamemodeVM.lightGamemodes.collectAsState(initial = emptyList()).value,
    gameVM: ILightGameListVM = hiltViewModel<LightGameListVM>(),
    gameList: List<LightGame> = gameVM.lightGames.collectAsState(initial = emptyList()).value
) {
    val context = LocalContext.current

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            systemUiController.isSystemBarsVisible = false
        }

        else -> {
            systemUiController.isSystemBarsVisible = true
        }
    }

    val pagerState = rememberPagerState(pageCount = 3)

    Scaffold(
        topBar = {
            TopAppBar({ Text(stringResource(R.string.app_name)) }, Modifier.background(themeColors.primary))
        },
        floatingActionButton = {
            FloatingActionButton(
                {
                    editOrNewGamemode(context)
                },
                backgroundColor = Color.Gray,
                shape = CircleShape,
                content = { Text("+", color = Color.White, fontSize = 5.em) },
            )
        },
        bottomBar = {
            Tabs(pagerState)
        }) { uselessPaddingValues ->
        Text(text = "", Modifier.padding(uselessPaddingValues))
        TabsContent(pagerState, gamemodeVM, gamemodeList, gameVM, gameList)
    }
}


