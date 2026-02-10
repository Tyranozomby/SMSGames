package com.noobzsociety.smsgames.ui.screens.test

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import com.noobzsociety.smsgames.engine.IJsExecutor
import com.noobzsociety.smsgames.engine.PhoneNumber
import com.noobzsociety.smsgames.engine.TestJsExecutor
import com.noobzsociety.smsgames.ui.components.CodeEditor
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun TestScreen(
    navHostController: NavHostController,
) {
    var text by remember {
        mutableStateOf(
            """
             export function setup() {
                console.log("Setup function called");
                return {
                    foo: 42,
                    bar: "Hello, test!",
                    test: {
                        truc: 69
                    },
                    array: [1, 2, 3]
                };
            };

             export async function main({data, settings, owner}) {
                console.log(data);
                console.log(settings);
                console.log(owner);
                
                // owner.send(`Hello World!`);
                console.log(await owner.promptInteger("Enter a number"))
                console.log(await owner.promptString("Enter a string"))
            };
            """.trimIndent()
        )
    }

    val game = RoomGame(
        0,
        0,
        0,
        JSONObject(mapOf<String, String>("foo" to "bar")),
    )

    val player = RoomPlayer(
        0,
        "Test User",
        PhoneNumber("1234567890"),
        true,
        true
    )

    val quickJs = remember { TestJsExecutor(game, player) }

    val dataState by quickJs.data.collectAsState()

    val scope = rememberCoroutineScope()

    ScreenBase(navHostController, "TEST") {
    Column {
        CodeEditor(text) { text = it }
        Button(onClick = {
            scope.launch {
                quickJs.run(text, "My Game").onLeft {
                    when (it) {
                        is IJsExecutor.RunError.ScriptParsingError -> {
                            println(it.message)
                        }

                        is IJsExecutor.RunError.MainFunctionNotDefined -> {
                            println("Main function not defined")
                        }

                        is IJsExecutor.RunError.SetupFunctionNotDefined -> {
                            println("Setup function not defined")
                        }

                        is IJsExecutor.RunError.RuntimeError -> {
                            println(it.message)
                        }
                    }
                }
            }
        }) {
            Text("Run")
        }
        Text(dataState.toString())
    }
    }
}

@Preview("Light")
@Preview("Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TestScreenPreview() {
    SMSGamesTheme {
        TestScreen(
            navHostController = rememberNavController(),
        )
    }
}