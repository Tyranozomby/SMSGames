package com.noobzsociety.smsgames.engine

import android.content.Context
import android.util.Log
import com.github.ajalt.clikt.core.subcommands
import com.noobzsociety.smsgames.data.datastore.AppSettings
import com.noobzsociety.smsgames.data.room.AppDatabase
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val TAG = SmsHandler::class.simpleName

@JvmInline
value class PhoneNumber(val number: String)
data class SMSMessage(val phoneNumber: PhoneNumber, val message: String)

class SmsHandler(
    private val smsSender: SmsSender,
    private val settings: AppSettings,
    private val gameManager: GameManager,
    database: AppDatabase,
    private val context: Context
) {

    private val playerDao = database.playerDao()
    private val gameDao = database.gameDao()
    private val gamemodeDao = database.gamemodeDao()

    private val gameScope = CoroutineScope(Dispatchers.Default)

    private val messagesChannel = Channel<SMSMessage>(Channel.UNLIMITED)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            messagesChannel.consumeAsFlow()
                .collect { smsMessage ->
                    handleMessage(smsMessage.phoneNumber, smsMessage.message.trim())
                }
        }
    }

    fun dispatchMessageHandling(smsMessage: SMSMessage) {
        messagesChannel.trySendBlocking(smsMessage).onFailure {
            Log.e(TAG, "Failed to send message to channel: ${it?.message}")
        }
    }

    private suspend fun handleMessage(from: PhoneNumber, message: String) {
        val player: RoomPlayer? = playerDao.getByNumber(from.number).first()

        if (player == null) {
            handleUnknownPlayer(from, message)
        } else {
            handleKnownPlayer(player, message)
        }
    }

    private suspend fun handleUnknownPlayer(from: PhoneNumber, message: String) {
        val commandPrefix: Char = settings.commandPrefix.flow.first()

        if (message.startsWith(commandPrefix)) {
            val commandHandler = CommandHandler(commandPrefix, from, smsSender, context)

            commandHandler.subcommands(
                HelpCommand(commandHandler),
                RegisterCommand(from, playerDao, context),
                PlayCommand(null, gamemodeDao, gameManager, gameScope, context),
            ).handle(message)
        }
    }

    private suspend fun handleKnownPlayer(player: RoomPlayer, message: String) {
        val commandPrefix: Char = settings.commandPrefix.flow.first()
        val ownerCommand: String = settings.ownerCommand.flow.first()

        val playerGame = gameDao.getPlayerGame(player.id).first()

        // If not a command
        if (!message.startsWith(commandPrefix)) {
            // And if in a game and is not the admin
            if (playerGame != null && !player.isAdmin) {
                handleGameMessage(player, message)
            }
            return
        }

        val commandHandler = CommandHandler(commandPrefix, player.phoneNumber, smsSender, context)
        commandHandler.subcommands(HelpCommand(commandHandler))

        // Check if the player is in a game
        if (playerGame != null) {
            val ownerCmdWithPrefix = "${commandPrefix}${ownerCommand}"

            // If the player is the admin and the message starts with the owner command
            if (player.isAdmin && message.startsWith(ownerCmdWithPrefix)) {
                handleGameMessage(player, message.removePrefix(ownerCmdWithPrefix).trimStart())
                return
            }

            commandHandler.subcommands(RulesCommand(), ForfeitCommand())
        } else {
            // Not in a game
            commandHandler.subcommands(
                NickCommand(player, playerDao, context),
                RemoveAccountCommand(player, context),
                PlayCommand(player, gamemodeDao, gameManager, gameScope, context)
            )

            // if (gameManager.isInvited(player)) {
            //     commandHandler.subcommands(
            //         JoinCommand(player, gameManager),
            //         RejectCommand(player, gameManager)
            //     )
            // }
        }

        commandHandler.handle(message)
    }

    private suspend fun handleGameMessage(player: RoomPlayer, message: String) {
        gameManager.handleMessage(player, message)
    }
}