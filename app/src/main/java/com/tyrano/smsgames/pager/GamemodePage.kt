package com.tyrano.smsgames.pager

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tyrano.smsgames.ConfirmDialog
import com.tyrano.smsgames.activities.EditGamemodeActivity
import com.tyrano.smsgames.getPreviewLightGamemodeList
import com.tyrano.smsgames.getPreviewLightGamemodeListVM
import com.tyrano.smsgames.light.LightGamemode
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import com.tyrano.smsgames.ui.theme.themeColors
import com.tyrano.smsgames.viewmodels.ILightGamemodeListVM

@Preview(showBackground = true)
@Composable
fun GamemodePagePreview() {
    val viewModel = remember { getPreviewLightGamemodeListVM() }

    SMSGamesTheme {
        GamemodeList(
            viewModel,
            getPreviewLightGamemodeList()
        )
    }
}

@Composable
fun GamemodeList(viewModel: ILightGamemodeListVM, gamemodeList: List<LightGamemode>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (gamemode in gamemodeList) {
            GamemodeComponent(viewModel, gamemode)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GamemodeComponent(viewModel: ILightGamemodeListVM, gamemode: LightGamemode) {
    val context = LocalContext.current
    var dropdown by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp)
                .graphicsLayer {
                    shadowElevation = 8.dp.toPx()
                    shape = RoundedCornerShape(12.dp)
                    clip = true
                }
                .background(color = themeColors.background)
                .combinedClickable(
                    onClick = { editOrNewGamemode(context, gamemode.id) },
                    onLongClick = { dropdown = true }
                )
                .padding(16.dp, 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(gamemode.name)
            Switch(
                checked = gamemode.enabled,
                onCheckedChange = {
                    viewModel.toggle(gamemode)
                }
            )
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
            Divider()
            DropdownMenuItem(onClick = { dropdown = false }) {
                Text("Annoncer", color = Color.Red)
            }
        }
        if (delete) {
            if (viewModel.countGames(gamemode) == 0)
                ConfirmDialog(
                    title = "Suppression d'un jeu",
                    content = "Êtes-vous sûr de vouloir supprimer ${gamemode.name} ?",
                    confirm = "Oui",
                    dismiss = "Non",
                    onConfirm = { viewModel.delete(gamemode); delete = false },
                    onDismiss = { delete = false }
                )
            else
                ConfirmDialog(
                    title = "Suppression d'un jeu",
                    content = "Êtes-vous sûr de vouloir supprimer ${gamemode.name} ainsi que toutes les parties en cours lui étant associées ?",
                    confirm = "Oui",
                    dismiss = "Non",
                    onConfirm = { viewModel.delete(gamemode); delete = false },
                    onDismiss = { delete = false }
                )
        }
    }
}

fun editOrNewGamemode(context: Context, id: Long = 0) {
    val intent = Intent(context, EditGamemodeActivity::class.java)
    intent.putExtra("gamemode", id)
    context.startActivity(intent)
}