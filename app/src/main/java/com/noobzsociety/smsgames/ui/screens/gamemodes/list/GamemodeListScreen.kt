package com.noobzsociety.smsgames.ui.screens.gamemodes.list

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.navigation.AppScreen
import com.noobzsociety.smsgames.ui.utils.PreviewBase
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GamemodeListScreen(
    navHostController: NavHostController,
    viewModel: GamemodeListViewModel = koinViewModel()
) {
    val gamemodesState by viewModel.gamemodes.collectAsState()

    val openGamemodeAction = { id: Long? ->
        navHostController.navigate(AppScreen.Gamemode.DetailsScreen(id))
    }

    ScreenBase(
        navHostController,
        title = stringResource(R.string.title_gamemode_list),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openGamemodeAction(null) },
            ) {
                Icon(Icons.Default.Add, "Add")
            }
        }) {
        when (val gamemodes = gamemodesState) {
            null -> Loading()
            else -> {
                if (gamemodes.isEmpty()) {
                    EmptyList()
                } else {
                    Content(
                        gamemodes = gamemodesState,
                        onGamemodeClick = { openGamemodeAction(it.id) },
                        onToggle = { g, b -> viewModel.toggleGamemode(g, b) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    gamemodes: List<RoomGamemode>?,
    onGamemodeClick: (RoomGamemode) -> Unit,
    onToggle: (RoomGamemode, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        gamemodes?.forEach { gamemode ->
            Row(
                modifier = Modifier
                    .clickable { onGamemodeClick(gamemode) }
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = gamemode.name,
                )

                Switch(
                    checked = gamemode.enabled,
                    onCheckedChange = { onToggle(gamemode, it) }
                )
            }
        }
    }
}

@Composable
private fun EmptyList() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("No gamemodes available")
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
            gamemodes = listOf(
                RoomGamemode(
                    id = 1,
                    name = "Test Gamemode",
                    rules = "Test rules",
                    code = "",
                ),
                RoomGamemode(
                    id = 2,
                    name = "Another Gamemode",
                    rules = "Another rules",
                    code = "",
                ),
                RoomGamemode(
                    id = 3,
                    name = "Third Gamemode",
                    rules = "Third rules",
                    code = "",
                )
            ),
            onGamemodeClick = { gamemode ->
                // Handle gamemode click
            },
            onToggle = { gamemode, enabled ->
                // Handle toggle
            }
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