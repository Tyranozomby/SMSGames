package com.tyrano.smsgames.engine

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.github.ajalt.clikt.core.subcommands
import com.tyrano.smsgames.SmsGameDatabase
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.dao.PlayerDao
import com.tyrano.smsgames.datastore.StoreParameters
import com.tyrano.smsgames.entities.PlayerEntity
import kotlinx.coroutines.runBlocking

class IncomingSMSHandler(database: SmsGameDatabase, context: Context) {

    private val TAG = IncomingSMSHandler::class.simpleName

    private val playerDao: PlayerDao = database.playerDao()
    private val gamemodeDao: GamemodeDao = database.gamemodeDao()
    private val gameDao: GameDao = database.gameDao()

    @Suppress("DEPRECATION")
    private val smsManager = SmsManager.getDefault()

    private val datastore = StoreParameters(context)

    val gameManager = GameManager(gamemodeDao, gameDao, playerDao, this, context)


    fun handleMessage(from: String, message: String) {
        Log.d(TAG, "analyseMessage: $from - $message")

        val player: PlayerEntity? = runBlocking {
            playerDao.getByNumber(from)
        }

        if (player == null) {
            handleUnknownPlayer(from, message.trim())
        } else {
            handleKnownPlayer(player, message.trim())
        }
    }

    private fun handleKnownPlayer(player: PlayerEntity, message: String) {
        val commandPrefix: String = runBlocking { datastore.getCommandPrefix() }
        val myNumber = runBlocking { datastore.getMyNumber() }

        if (message.startsWith(commandPrefix)) {

            val mainCommand = Main(commandPrefix, this, player.phoneNumber)
            mainCommand.subcommands(HelpCommand(mainCommand))

            if (player.phoneNumber == myNumber)
                mainCommand.subcommands(DeleteGamesCommand(gameDao))

            if (player.gameId == null) { // Pas en jeu
                mainCommand.subcommands(
                    NickCommand(playerDao, player),
                    RemoveAccountCommand(playerDao, player),
                    PlayCommand(gamemodeDao, player, gameManager, player.phoneNumber == myNumber),
                    JoinCommand(gameManager, player),
                    RejectCommand(gameManager, player)
                )
            } else {
                if (player.phoneNumber == myNumber && message.startsWith("${commandPrefix}do")) {
                    handleGameMessage(player, message.removePrefix("${commandPrefix}do "))
//                    Fais comme un joueur normal sans le !go
                }
                mainCommand.subcommands(RulesCommand(playerDao, player))
//                when (substring) {
//                    "forfeit" -> Log.d(TAG, "handleUnknownPlayer: FORFAIT")
//                }
            }
            mainCommand parsedWith getArgs(message)
        } else {
            if (player.gameId != null && player.phoneNumber != myNumber) { // Pas commande, mais en jeu et pas moi
                handleGameMessage(player, message)
            }
        }
    }

    private fun handleGameMessage(player: PlayerEntity, message: String) {
        gameManager.handleMessage(player, message)
    }

    private fun handleUnknownPlayer(from: String, message: String) {
        val commandPrefix: String = runBlocking { datastore.getCommandPrefix() }
        if (message.startsWith(commandPrefix)) {

            val mainCommand = Main(commandPrefix, this, from)

            mainCommand.subcommands(
                HelpCommand(mainCommand),
                RegisterCommand(playerDao, from, runBlocking { datastore.getRegisterMessage() }),
                PlayCommand(gamemodeDao, null, gameManager)
                // Join
            ) parsedWith getArgs(message)
        }
    }

    fun sendSMS(to: String, text: String) {
        if (text != "") {
            Log.d(TAG, "sendSMS: $to - $text")
            smsManager.sendMultipartTextMessage(to, null, smsManager.divideMessage(text), null, null)
        }
    }

    private fun getArgs(message: String): List<String> {
        val args = message.substring(1).trimStart().split(" ").toMutableList()
        args[0] = args[0].lowercase()
        return args
    }
}