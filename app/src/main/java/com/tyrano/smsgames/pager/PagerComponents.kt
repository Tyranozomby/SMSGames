package com.tyrano.smsgames.pager

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import com.tyrano.smsgames.getPreviewLightGameList
import com.tyrano.smsgames.getPreviewLightGameListVM
import com.tyrano.smsgames.getPreviewLightGamemodeList
import com.tyrano.smsgames.getPreviewLightGamemodeListVM
import com.tyrano.smsgames.light.LightGame
import com.tyrano.smsgames.light.LightGamemode
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import com.tyrano.smsgames.ui.theme.themeColors
import com.tyrano.smsgames.viewmodels.ILightGameListVM
import com.tyrano.smsgames.viewmodels.ILightGamemodeListVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Preview(showBackground = true)
@Composable
fun PagerPreview() {
    val gamemodeListVM = remember { getPreviewLightGamemodeListVM() }
    val gameListVM = remember { getPreviewLightGameListVM() }
    val pagerState = rememberPagerState(pageCount = 3, initialPage = 1)

    SMSGamesTheme {
        Scaffold(bottomBar = {
            Tabs(pagerState)
        }) { uselessPaddingValues ->
            Text(text = "", Modifier.padding(uselessPaddingValues))
            TabsContent(
                pagerState,
                gamemodeListVM,
                getPreviewLightGamemodeList(),
                gameListVM,
                getPreviewLightGameList()
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun Tabs(pagerState: PagerState) {
    val list = listOf(
        "Jeux" to Icons.Default.Home,
        "Parties" to Icons.Default.Person,
        "Plus" to Icons.Default.Settings
    )

    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = themeColors.primaryVariant,
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                height = 1.5.dp,
                color = Color.White
            )
        }
    ) {
        list.forEachIndexed { index, _ ->
            Tab(
                icon = { Icon(imageVector = list[index].second, contentDescription = null) },
                text = {
                    Text(
                        list[index].first,
                        color = if (pagerState.currentPage == index) Color.White else Color.LightGray
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabsContent(
    pagerState: PagerState,
    gamemodeVM: ILightGamemodeListVM,
    gamemodeList: List<LightGamemode>,
    gameVM: ILightGameListVM,
    gameList: List<LightGame>
) {
    HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
        when (page) {
            0 -> GamemodeList(gamemodeVM, gamemodeList)
            1 -> GameList(gameVM, gameList)
            2 -> ParametersList()
        }
    }
}
