package com.noobzsociety.smsgames.ui.screens.gamemodes.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import arrow.core.raise.ensure
import com.noobzsociety.smsgames.data.room.AppDatabase
import com.noobzsociety.smsgames.data.room.entities.ParameterDefinition
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import com.noobzsociety.smsgames.engine.CheckerJsExecutor
import com.noobzsociety.smsgames.engine.IJsExecutor
import com.noobzsociety.smsgames.ui.navigation.AppScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.report
import opensavvy.progress.done
import opensavvy.progress.loading
import opensavvy.state.arrow.out
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.coroutines.combineCompleted
import opensavvy.state.outcome.failureOrNull
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.asOutcome
import opensavvy.state.progressive.successfulWithProgress
import kotlin.time.Duration.Companion.seconds

class GamemodeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    database: AppDatabase
) : ViewModel() {
    data class ValidationResult(
        val nameError: StateFlow<ProgressiveOutcome<NameError, Unit>>,
        val rulesError: StateFlow<ProgressiveOutcome<RulesError, Unit>>,
        val codeError: StateFlow<ProgressiveOutcome<IJsExecutor.RunError, Unit>>,
    )

    sealed class NameError {
        object Empty : NameError()
//        object AlreadyExists : NameError()
    }

    sealed class RulesError {
        object Empty : RulesError()
    }

    private val gamemodeDao = database.gamemodeDao()

    private var originalGamemode: MutableStateFlow<RoomGamemode?> = MutableStateFlow(null)

    init {
        val gamemodeId = savedStateHandle.toRoute<AppScreen.Gamemode.DetailsScreen>().gamemodeId
        if (gamemodeId != null) {
            viewModelScope.launch {
                gamemodeDao.get(gamemodeId).firstOrNull()?.let { gamemode ->
                    originalGamemode.value = gamemode
                    _gamemode.value = gamemode
                }
            }
        }
    }

    private val _gamemode: MutableStateFlow<RoomGamemode> = MutableStateFlow(newGamemode)
    val gamemode: StateFlow<RoomGamemode> = _gamemode.asStateFlow()

    private val quickJs: CheckerJsExecutor = CheckerJsExecutor()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val validationResult = ValidationResult(
        nameError = _gamemode.map { it.name }.distinctUntilChanged().map {
            out {
                report(loading())
                ensure(it.isNotBlank()) { NameError.Empty }
                report(done())
            }
        }.captureProgress().stateIn(viewModelScope, SharingStarted.Lazily, Unit.successfulWithProgress()),

        rulesError = _gamemode.map { it.rules }.distinctUntilChanged().map {
            out {
                report(loading())
                ensure(it.isNotBlank()) { RulesError.Empty }
                report(done())
            }
        }.captureProgress().stateIn(viewModelScope, SharingStarted.Lazily, Unit.successfulWithProgress()),

        codeError = _gamemode.map { it.code }.distinctUntilChanged().mapLatest {
            out {
                report(loading())
                delay(1.seconds) // To avoid too many calls
                quickJs.checkAll(it, "check-${_gamemode.value.name}").onLeft { println(it) }.bind()
                report(done())
            }
        }.captureProgress().combineCompleted()
            .stateIn(viewModelScope, SharingStarted.Lazily, Unit.successfulWithProgress()),
    )

    val isDirty: StateFlow<Boolean> = combine(
        _gamemode,
        originalGamemode
    ) { gamemode, original ->
        gamemode != original
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isAllGood: StateFlow<Boolean?> = combine(
        validationResult.nameError,
        validationResult.rulesError,
        validationResult.codeError
    ) { name, rules, code ->
        // if any of them is loading, return null
        if (listOf(name, rules, code).any { it.progress is Progress.Loading }) null
        // else returns false if there is an error
        else listOf(name, rules, code).all { it.asOutcome()?.failureOrNull == null }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun editGamemode(gamemode: RoomGamemode) {
        _gamemode.value = gamemode
    }

    fun saveGamemode() {
        if (isAllGood.value != true) return

        viewModelScope.launch {
            gamemodeDao.upsert(_gamemode.value).let {
                if (it != -1L) {
                    val copy = _gamemode.value.copy(id = it)
                    originalGamemode.value = copy
                    _gamemode.value = copy
                } else {
                    originalGamemode.value = _gamemode.value
                }
            }
        }
    }
}

val newGamemode = RoomGamemode(
    id = 0,
    name = "",
    rules = "",
    parameters = mapOf("foo" to ParameterDefinition.Int("foo def", 42), "bar" to ParameterDefinition.Boolean("bar def", true)),
    code = """
    export function setup({params}) {
        // This is your game data
        return {
            foo: params.foo || 69,
            bar: params.bar || false,
            obj: {
                nested: "Hello, World!"
            },
            array: [1, 2, 3]
        };
    };

    export async function main({data, owner}) {
        console.log(data);
        // console.log(owner);
       
        owner.send(`Hello World!`);
        await delay(5000);
        owner.send("5 seconds later...");
        console.log(await owner.promptInteger("Enter a number"))
        console.log(await owner.promptString("Enter a string"))
    };
    """.trimIndent(),
)