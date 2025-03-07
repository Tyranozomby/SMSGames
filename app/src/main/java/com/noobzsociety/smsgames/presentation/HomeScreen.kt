package com.noobzsociety.smsgames.presentation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.noobzsociety.smsgames.components.CodeEditor
import com.noobzsociety.smsgames.quickjs.QuickJsExecutor
import com.noobzsociety.smsgames.ui.theme.SMSGamesTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeViewModel = koinViewModel()
) {
    var text by remember {
        mutableStateOf(
            """
             export function setup() {
                return {
                    foo: 42,
                    bar: "Hello, test!",
                    test: {
                        truc: 69
                    },
                    array: [1, 2, 3],
                    func: function() {
                        return 42;
                    }
                };
            };

             export function main() {
                console.log(this);
                console.log(JSON.stringify(this));
                console.log(this.func())

                this.foo = 43;
                this.bar = "Hello, universe!";
                this.test.truc = 70;
            };
            """.trimIndent()
        )
    }

    val quickJs = QuickJsExecutor()

    val scope = rememberCoroutineScope()

    Column {
        CodeEditor(text) { text = it }
        Button(onClick = {
            scope.launch {
                quickJs.run(text, "My Game").onLeft {
                    when (it) {
                        is QuickJsExecutor.RunError.ScriptParsingError -> {
                            println(it.message)
                        }

                        is QuickJsExecutor.RunError.MainFunctionNotDefined -> {
                            println("Main function not defined")
                        }

                        is QuickJsExecutor.RunError.SetupFunctionNotDefined -> {
                            println("Setup function not defined")
                        }

                        is QuickJsExecutor.RunError.RuntimeError -> {
                            println(it.message)
                        }
                    }
                }
            }
        }) {
            Text("Run")
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