package com.tyrano.smsgames.pager

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tyrano.smsgames.ConfirmDialog
import com.tyrano.smsgames.activities.ViewGameActivity
import com.tyrano.smsgames.getPreviewLightGameList
import com.tyrano.smsgames.getPreviewLightGameListVM
import com.tyrano.smsgames.light.LightGame
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import com.tyrano.smsgames.ui.theme.themeColors
import com.tyrano.smsgames.viewmodels.ILightGameListVM
import java.time.Duration
import java.time.Instant

@Preview(showBackground = true)
@Composable
fun GamePagePreview() {
    val gameListVM = remember { getPreviewLightGameListVM() }

    SMSGamesTheme {
        GameList(
            gameListVM,
            getPreviewLightGameList()
        )
    }
}

@Composable
fun GameList(viewModel: ILightGameListVM, gameList: List<LightGame>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (game in gameList) {
            GameComponent(viewModel, game)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameComponent(viewModel: ILightGameListVM, game: LightGame) {
    val context = LocalContext.current

    var dropdown by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }

    Box {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(12.dp)
                clip = true
            }
            .background(color = themeColors.background)
            .combinedClickable(
                onClick = { viewGame(context, game.id) },
                onLongClick = { dropdown = true }
            )
            .padding(16.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(game.gameName)
                Text(game.ownerName, fontWeight = FontWeight.Light)
            }

            if (game.start == null)
                Text("En attente")
            else {
                val seconds = Duration.between(game.start, Instant.now()).seconds
                if (seconds <= 10)
                    Text("À l'instant")
                else if (seconds < 60)
                    Text("${seconds}s")
                else {
                    var minutes = seconds / 60
                    if (minutes < 60)
                        Text("${minutes}m")
                    else {
                        val hours = minutes / 60
                        minutes %= 60
                        if (minutes == 0L)
                            Text("${hours}h")
                        else
                            if (minutes > 9)
                                Text("${hours}h${minutes}m")
                            else
                                Text("${hours}h0${minutes}m")
                    }
                }
            }
        }
        DropdownMenu(
            expanded = dropdown,
            onDismissRequest = { dropdown = false }
        ) {
            DropdownMenuItem(onClick = {
                dropdown = false
                delete = true
            }) {
                Text("Supprimer")
            }
        }
        if (delete) {
            ConfirmDialog(
                title = "Suppression d'une partie",
                content = "Êtes-vous sûr de vouloir supprimer ${game.gameName} de ${game.ownerName} ?",
                confirm = "Oui",
                dismiss = "Non",
                onConfirm = { viewModel.delete(game); delete = false },
                onDismiss = { delete = false }
            )
        }

    }
}

fun viewGame(context: Context, id: Long) {
    val intent = Intent(context, ViewGameActivity::class.java)
    intent.putExtra("game", id)
    context.startActivity(intent)
}
