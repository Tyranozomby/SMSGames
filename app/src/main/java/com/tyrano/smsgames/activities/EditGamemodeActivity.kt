package com.tyrano.smsgames.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tyrano.smsgames.ConfirmDialog
import com.tyrano.smsgames.getPreviewGamemodeVM
import com.tyrano.smsgames.ui.theme.SMSGamesTheme
import com.tyrano.smsgames.viewmodels.GamemodeVM
import com.tyrano.smsgames.viewmodels.IGamemodeVM
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class EditGamemodeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra("gamemode", 0)

        setContent {
            SMSGamesTheme {
                val hiltViewModel: GamemodeVM = hiltViewModel()
                LaunchedEffect(key1 = id) {
                    hiltViewModel.get(id)
                }
                EditGamemodeInterface(this::finish, hiltViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditGamemodePreview() {
    SMSGamesTheme {
        EditGamemodeInterface(EditGamemodeActivity()::finish, getPreviewGamemodeVM())
    }
}

@Composable
fun EditGamemodeInterface(activityFinish: () -> Unit, gamemodeVM: IGamemodeVM) {
    var quit by remember { mutableStateOf(false) }

    val tryQuit = {
        if (!gamemodeVM.isDirty)
            activityFinish()
        else
            quit = true
    }

    BackHandler(onBack = tryQuit)

    Scaffold(
        topBar = {
            TopAppBar({ Text(gamemodeVM.name) },
                navigationIcon = {
                    IconButton(onClick = tryQuit) {
                        Icon(Icons.Filled.Close, "Quitter sans sauver")
                    }
                },
                actions = {
                    IconButton(onClick = { saveAndQuit(gamemodeVM, activityFinish) }) {
                        Icon(Icons.Filled.Check, "Sauvegarder et quitter")
                    }
                }
            )
        }
    ) { uselessPaddingValues ->
        Text(text = "", Modifier.padding(uselessPaddingValues))
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LabelComponent("Nom du jeu") {
                OutlinedTextField(
                    value = gamemodeVM.name,
                    onValueChange = { if (!it.contains('\n')) gamemodeVM.name = it },
                    singleLine = true,
                    modifier = Modifier.padding(top = 15.dp, bottom = 8.dp)
                )
            }
            LabelComponent("Paramètres :") {
                var json by remember(gamemodeVM.id) { mutableStateOf(gamemodeVM.settings.toString(2)) }
                var error by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = json,
                    onValueChange = {
                        json = it
                        try {
                            gamemodeVM.settings = JSONObject(json)
                            error = false
                        } catch (e: JSONException) {
                            gamemodeVM.settings = JSONObject()
                            error = true
                        }
                    },
                    trailingIcon = {
                        Column(modifier = Modifier.fillMaxHeight()) {
                            if (!error) {
                                IconButton(onClick = { json = JSONObject(json).toString(2) }) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        "Icône"
                                    )
                                }
                            }
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(errorBorderColor = Color.Red),
                    isError = error,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false
                    ),
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                )
            }
            LabelComponent("Règles du jeu :") {
                OutlinedTextField(
                    value = gamemodeVM.rules,
                    onValueChange = { gamemodeVM.rules = it },
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                )
            }
            LabelComponent("Code javascript :") {
                OutlinedTextField(
                    value = gamemodeVM.code,
                    onValueChange = { gamemodeVM.code = it },
                    modifier = Modifier
                        .padding(top = 15.dp, bottom = 25.dp)
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false
                    ),
                )
            }
            if (quit) {
                ConfirmDialog(
                    title = "Quitter la page",
                    content = "Vous avez des modifications non sauvegardées",
                    confirm = "Quitter",
                    dismiss = "Rester",
                    onDismiss = { quit = false },
                    onConfirm = activityFinish
                )
            }
        }
    }
}

@Composable
fun LabelComponent(label: String, component: @Composable () -> Unit) {
    Text(label, modifier = Modifier.padding(top = 20.dp))
    component()
}

fun saveAndQuit(gamemode: IGamemodeVM, activityFinish: () -> Unit) {
    if (gamemode.id == 0.toLong())
        gamemode.insert()
    else
        gamemode.update()

    activityFinish()
}
