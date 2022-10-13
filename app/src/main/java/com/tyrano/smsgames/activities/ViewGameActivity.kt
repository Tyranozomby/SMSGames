package com.tyrano.smsgames.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tyrano.smsgames.getPreviewGameVM
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import com.tyrano.smsgames.viewmodels.GameVM
import com.tyrano.smsgames.viewmodels.IGameVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra("game", 0)

        setContent {
            SMSGamesTheme {
                val hiltViewModel: GameVM = hiltViewModel()
                LaunchedEffect(key1 = id) {
                    hiltViewModel.get(id)
                }
                ViewGameInterface(this::finish, hiltViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewGamePreview() {
    SMSGamesTheme {
        ViewGameInterface(EditGamemodeActivity()::finish, getPreviewGameVM())
    }
}

@Composable
fun ViewGameInterface(activityFinish: () -> Unit, gameVM: IGameVM) {
    Text(text = gameVM.toString())
    Scaffold(topBar = {
        TopAppBar({ Text("${gameVM.gamemodeName} - ${gameVM.owner.name}") },
            navigationIcon = {
                IconButton(onClick = activityFinish) {
                    Icon(Icons.Filled.Close, "Quitter")
                }
            }
        )
    }) { uselessPaddingValues ->
        Text(text = "", Modifier.padding(uselessPaddingValues))
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = gameVM.data.toString(2), onValueChange = {}, readOnly = true)
            val players = gameVM.players
            for (player in players) {
                Text("${player.name} - ${player.phoneNumber}")
            }
        }
    }
}