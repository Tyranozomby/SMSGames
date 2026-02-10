package com.noobzsociety.smsgames.ui.screens.games.list

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.utils.PreviewBase
import org.json.JSONObject
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GameListScreen(
    navHostController: NavHostController,
    viewModel: GameListViewModel = koinViewModel()
) {
    val gamesState by viewModel.games.collectAsState()

    ScreenBase(
        navHostController,
        title = stringResource(R.string.title_game_list)
    ) {
        when (val games = gamesState) {
            null -> Loading()
            else -> {
                if (games.isEmpty()) {
                    EmptyList()
                } else {
                    Content(
                        games = gamesState,
//                onGameClick = { gamemode ->
//                    navHostController.navigate(
//                        AppScreen.Game.DetailsScreen(gamemode.id)
//                    )
//                }
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    games: List<RoomGame>?,
//    onGamemodeClick: (RoomGame) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        games?.forEach { game ->
            Text(
                text = game.id.toString(),
                modifier = Modifier
//                    .clickable { onGamemodeClick(gamemode) }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyList() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("No games available")
    }
}

@Composable
private fun Loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
@PreviewLightDark
private fun ContentPreview() {
    PreviewBase {
        Content(
            games = listOf(
                RoomGame(
                    id = 0L,
                    gamemodeId = 0L,
                    ownerId = 1L,
                    parameters = JSONObject(mapOf("bar" to "foo")),
                )
            )
        )
    }
}

@Composable
@PreviewLightDark
private fun LoadingPreview() {
    PreviewBase {
        Loading()
    }
}

@Composable
@PreviewLightDark
private fun EmptyListPreview() {
    PreviewBase {
        EmptyList()
    }
}