package com.noobzsociety.smsgames.engine

import android.content.Context
import android.util.Log
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.data.FileManager
import com.noobzsociety.smsgames.data.room.AppDatabase
import com.noobzsociety.smsgames.data.room.dao.GameDao
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.data.room.entities.RoomGamemode
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import com.noobzsociety.smsgames.data.room.entities.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

private val TAG: String = GameManager::class.simpleName.toString()

class GameManager(
    database: AppDatabase,
    private val smsSender: SmsSender,
    private val fileManager: FileManager,
    private val appContext: Context,
) {
    private val gameDao = database.gameDao()

    private val _games: MutableStateFlow<Map<Long, GameInstance>> = MutableStateFlow(emptyMap())
    val games: StateFlow<Map<Long, GameInstance>> = _games.asStateFlow()
//    private val invitations: MutableList<Pair<GameInstance, PromptPlayer>> = mutableListOf()

    suspend fun startNewGame(
        gamemode: RoomGamemode,
        player: RoomPlayer,
        settings: JSONObject?
    ) {
        val gameEntity = RoomGame(
            gamemodeId = gamemode.id,
            ownerId = player.id,
            parameters = settings ?: gamemode.parameters.toJson()
        )

        Log.d(TAG, "createGame: $gameEntity")

        val gameId = gameDao.insert(gameEntity, player.id)
        val game = gameDao.get(gameId).first()

        _games.update { current ->
            current + (game.id to GameInstance(
                game,
                gamemode,
                player,
                gameDao,
                smsSender,
                this,
                fileManager,
                appContext
            ))
        }
        _games.value[game.id]!!.run()
    }

    suspend fun handleMessage(player: RoomPlayer, message: String) {
        val game = gameDao.getPlayerGame(player.id).first()!!
        val gameInstance = _games.value.getOrDefault(game.id, null)
            ?: run {
                Log.e(TAG, "Game instance not found for game ${game.id}")
                return
            }

        gameInstance.handleMessage(player, message)
    }

    fun cleanUpGameInstance(gameInstance: GameInstance) {
        _games.update { it - gameInstance.game.id }
//        invitations.removeIf { it.first == gameInstance }
    }

//    fun registerInvitation(
//        game: GameInstance,
//        prompt: PromptPlayer
//    ) {
//        invitations += Pair(game, prompt)
//    }
//
//    fun isInvited(player: RoomPlayer): Boolean =
//        invitations.any { it.second == player }
//
//    fun getInvitationOrNull(gameId: Long, player: RoomPlayer): PromptPlayer? =
//        invitations.firstOrNull { it.first.game.id == gameId && it.second == player }?.second
}

class GameInstance(
    val game: RoomGame,
    val gamemode: RoomGamemode,
    val owner: RoomPlayer,
    val gameDao: GameDao,
    private val smsSender: SmsSender,
    val gameManager: GameManager,
    fileManager: FileManager,
    private val appContext: Context
) {
    private val messageQueue: MutableStateFlow<Map<RoomPlayer, List<String>>> = MutableStateFlow(emptyMap())
    private val prompts: MutableStateFlow<Map<RoomPlayer, SendChannel<String>>> = MutableStateFlow(emptyMap())
    private val messageScope = CoroutineScope(Dispatchers.IO.apply { this.limitedParallelism(1) })

    private val quickJs: JsExecutor = JsExecutor(game, owner, gameDao, smsSender, fileManager, object : PromptWaiter {
        override suspend fun <R> waitForPrompt(
            player: RoomPlayer,
            convertor: Raise<PromptWaiter.ErrorMessage>.(String) -> R
        ): R = this@GameInstance.waitForPrompt(player, convertor)
    })

    suspend fun run() {
        messageScope.launch {
            combine(messageQueue, prompts, ::Pair).collect { (messages, prompts) ->
                val map = messages.mapValues { (key, value) ->
                    val last = value.lastOrNull() ?: return@mapValues value
                    val channel = prompts[key] ?: return@mapValues value

                    try {
                        channel.send(last)
                        value.dropLast(1)
                    } catch (_: ClosedSendChannelException) {
                        value
                    }
                }

                messageQueue.value = map
            }
        }

        quickJs.run(gamemode.code, "${gamemode.name}-${game.id}").fold({
            Log.e(TAG, "Error while executing JS code: $it")
            val error = when (it) {
                IJsExecutor.RunError.MainFunctionNotDefined -> appContext.resources.getString(R.string.error_js_main_function_not_defined)
                IJsExecutor.RunError.SetupFunctionNotDefined -> appContext.resources.getString(R.string.error_js_setup_function_not_defined)
                is IJsExecutor.RunError.ScriptParsingError -> it.message
                is IJsExecutor.RunError.RuntimeError -> it.message
            }
            smsSender.sendSMS(
                SMSMessage(
                    owner.phoneNumber,
                    appContext.resources.getString(R.string.error_js_execution, error)
                )
            )
        }, {
            Log.d(TAG, "Game ${game.id} ended")
        })

        gameManager.cleanUpGameInstance(this)
        gameDao.delete(game)
    }

    fun handleMessage(player: RoomPlayer, message: String) {
        messageQueue.update { current ->
            current + (player to listOf(message) + current[player].orEmpty())
        }
    }

    private suspend fun <R> waitForPrompt(
        player: RoomPlayer,
        convertor: Raise<PromptWaiter.ErrorMessage>.(String) -> R,
    ): R {
        if (prompts.value.containsKey(player)) {
            throw IllegalStateException("Player already has a prompt")
        }

        val channel = Channel<String>()
        prompts.update {
            it + (player to channel)
        }

        return channel.consumeAsFlow().transform { it ->
            when (val res = either { convertor(it) }) {
                is Either.Left -> {
                    when (val error = res.value) {
                        is PromptWaiter.ErrorMessage.Custom -> {
                            this@GameInstance.smsSender.sendSMS(
                                SMSMessage(
                                    player.phoneNumber,
                                    error.message,
                                )
                            )
                        }

                        is PromptWaiter.ErrorMessage.RepeatDefault -> {
                            this@GameInstance.smsSender.sendSMS(
                                SMSMessage(
                                    player.phoneNumber,
                                    error.message,
                                )
                            )
                        }

                        PromptWaiter.ErrorMessage.Nothing -> {
                            // Do nothing
                        }
                    }
                }

                is Either.Right -> {
                    emit(res.value)
                }
            }
        }.first().also {
            channel.close()
            prompts.update { current ->
                current - player
            }
        }
    }


    interface PromptWaiter {
        sealed class ErrorMessage {
            data class RepeatDefault(val message: String) : ErrorMessage()
            data class Custom(val message: String) : ErrorMessage()
            data object Nothing : ErrorMessage()
        }

        suspend fun <R> waitForPrompt(player: RoomPlayer, convertor: Raise<ErrorMessage>.(String) -> R): R
    }
}