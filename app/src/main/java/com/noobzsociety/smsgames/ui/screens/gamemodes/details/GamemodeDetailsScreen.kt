package com.noobzsociety.smsgames.ui.screens.gamemodes.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import com.noobzsociety.smsgames.engine.IJsExecutor
import com.noobzsociety.smsgames.ui.components.CodeEditor
import com.noobzsociety.smsgames.ui.components.ScreenBase
import com.noobzsociety.smsgames.ui.screens.gamemodes.details.GamemodeDetailsViewModel.ValidationResult
import com.noobzsociety.smsgames.ui.utils.PreviewBase
import kotlinx.coroutines.flow.MutableStateFlow
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.asOutcome
import opensavvy.state.progressive.successfulWithProgress
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GamemodeDetailsScreen(
    navHostController: NavHostController,
    viewModel: GamemodeDetailsViewModel = koinViewModel(),
) {
    val gamemodeState by viewModel.gamemode.collectAsState()
    val isDirtyState by viewModel.isDirty.collectAsState()
    val isAllGoodState by viewModel.isAllGood.collectAsState()
    val validationResult = viewModel.validationResult

    ScreenBase(
        navHostController,
        title = if (gamemodeState.name.isNotBlank()) gamemodeState.name else stringResource(R.string.title_gamemode_details),
        actions = {
            if (isAllGoodState == null) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            IconButton(
                enabled = isDirtyState && isAllGoodState == true,
                onClick = {
                    viewModel.saveGamemode()
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = stringResource(R.string.label_save))
            }
        }
    ) {
        Content(
            validationResult = validationResult,
            gamemode = gamemodeState
        ) { viewModel.editGamemode(it) }
    }
}

@Composable
private fun Content(
    validationResult: ValidationResult,
    gamemode: RoomGamemode,
    onGamemodeChange: (RoomGamemode) -> Unit,
) {
    val nameError by validationResult.nameError.collectAsState()
    val rulesError by validationResult.rulesError.collectAsState()
    val codeError by validationResult.codeError.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = stringResource(R.string.label_gamemode_name),
            style = MaterialTheme.typography.titleMedium
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = gamemode.name,
            onValueChange = { onGamemodeChange(gamemode.copy(name = it)) },
            placeholder = { Text(stringResource(R.string.placeholder_gamemode_name)) },
            singleLine = true,
            isError = nameError.asOutcome() is Outcome.Failure<*>,
            supportingText = {
                when (val outcome = nameError.asOutcome()) {
                    is Outcome.Failure<GamemodeDetailsViewModel.NameError> -> when (outcome.failure) {
                        GamemodeDetailsViewModel.NameError.Empty -> Text(stringResource(R.string.error_gamemode_name_empty))
                    }

                    null, is Outcome.Success -> {}
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.label_gamemode_rules),
            style = MaterialTheme.typography.titleMedium
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = gamemode.rules,
            onValueChange = { onGamemodeChange(gamemode.copy(rules = it)) },
            placeholder = { Text(stringResource(R.string.placeholder_gamemode_rules)) },
            isError = rulesError.asOutcome() is Outcome.Failure<*>,
            supportingText = {
                var outcome = rulesError.asOutcome()
                if (outcome is Outcome.Failure<GamemodeDetailsViewModel.RulesError>) {
                    when (outcome.failure) {
                        GamemodeDetailsViewModel.RulesError.Empty -> Text(stringResource(R.string.error_gamemode_rules_empty))
                    }
                }
            },
        )

        Spacer(Modifier.height(8.dp))

        // TODO: Add Parameters

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.label_gamemode_code),
            style = MaterialTheme.typography.titleMedium
        )
        // TODO: Better code error message
        Text(
            text = codeError.let { outcome ->
                if (outcome is ProgressiveOutcome.Failure<IJsExecutor.RunError>) {
                    when (val failure = outcome.failure) {
                        is IJsExecutor.RunError.MainFunctionNotDefined -> stringResource(R.string.error_js_main_function_not_defined)
                        is IJsExecutor.RunError.SetupFunctionNotDefined -> stringResource(R.string.error_js_setup_function_not_defined)
                        is IJsExecutor.RunError.ScriptParsingError -> failure.message
                        is IJsExecutor.RunError.RuntimeError -> failure.message
                    }
                } else ""
            }, color = MaterialTheme.colorScheme.error
        )
        CodeEditor(
            value = gamemode.code,
            onValueChange = { onGamemodeChange(gamemode.copy(code = it)) },
        )
    }
}


@Composable
@PreviewLightDark
private fun GamePreview() {
    PreviewBase {
        Content(
            validationResult = ValidationResult(
                nameError = MutableStateFlow(ProgressiveOutcome.Failure(GamemodeDetailsViewModel.NameError.Empty)),
                rulesError = MutableStateFlow(ProgressiveOutcome.Failure(GamemodeDetailsViewModel.RulesError.Empty)),
                codeError = MutableStateFlow(ProgressiveOutcome.Failure(IJsExecutor.RunError.SetupFunctionNotDefined))
            ),
            gamemode = RoomGamemode(
                id = 1,
                name = "",
                rules = "",
                code = ""
            )
        ) {}
    }
}

@Composable
@PreviewLightDark
private fun NullPreview() {
    PreviewBase {
        Content(
            validationResult = ValidationResult(
                nameError = MutableStateFlow(Unit.successfulWithProgress()),
                rulesError = MutableStateFlow(Unit.successfulWithProgress()),
                codeError = MutableStateFlow(Unit.successfulWithProgress())
            ),
            gamemode = newGamemode
        ) {}
    }
}
