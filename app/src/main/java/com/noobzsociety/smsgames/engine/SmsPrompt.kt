package com.noobzsociety.smsgames.engine

import android.util.Log
import com.noobzsociety.smsgames.data.room.dao.GameDao
import com.noobzsociety.smsgames.data.room.dao.PlayerDao
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first

//private val TAG = SmsPrompt::class.simpleName

/*
abstract class SmsPrompt<T>(
    internal val promptMessage: SMSMessage,
    internal val smsSender: SmsSender,
) {
    val channel = Channel<T>()

    protected abstract suspend fun validation(message: String): T?

    open suspend fun onSMS(message: String): T? {
        Log.d(TAG, "onSMS: $message")

        val validated = validation(message)

        return if (validated != null) {
            validated
        } else {
            smsSender.sendSMS(promptMessage)
            null
        }
    }
}

class PromptInteger(
    promptMessage: SMSMessage,
    smsSender: SmsSender,
) : SmsPrompt<Int>(promptMessage, smsSender) {
    override suspend fun validation(message: String): Int? = message.toIntOrNull()
}

class PromptString(
    promptMessage: SMSMessage,
    smsSender: SmsSender,
) : SmsPrompt<String>(promptMessage, smsSender) {
    override suspend fun validation(message: String): String? = message
}

class PromptPlayer(
    promptMessage: SMSMessage,
    smsSender: SmsSender,
    private val gameOwner: RoomPlayer,
    private val gameInstance: GameInstance,
) : SmsPrompt<RoomPlayer>(promptMessage, smsSender) {

    private val playerDao: PlayerDao = gameInstance.playerDao
    private val gameDao: GameDao = gameInstance.gameDao

    override suspend fun validation(message: String): RoomPlayer? {
        // TODO: Use Phone library to format the number
        val number = (if (message.startsWith("0")) "+33" + message.substring(1) else message).replace(" ", "")
        val player = playerDao.getByNumber(number).first()

        // Didn't find the player
        if (player == null) {
            smsSender.sendSMS(
                promptMessage.copy(message = "Cette personne n'est pas inscrite ou vous avez mal entré son numéro.\nEssayez encore")
            )
        }

        // Player is itself
        else if (player.phoneNumber == promptMessage.phoneNumber) {
            smsSender.sendSMS(
                promptMessage.copy(message = "Vous ne pouvez pas vous ajouter vous-même.\nEssayez encore")
            )
        }

        // Player is already in a game
        else if (gameDao.getPlayerGame(player.id).first() != null) {
            smsSender.sendSMS(
                promptMessage.copy(message = "Cette personne est déjà dans une partie.\nIl faut qu'elle la finisse pour que vous puissiez l'inviter")
            )
        }

        // Send the invitation message
        else {
            smsSender.sendSMS(
                SMSMessage(
                    phoneNumber = player.phoneNumber,
                    message = "Vous avez été invité·e par ${gameOwner.name} (${gameOwner.phoneNumber}) pour jouer au ${gameInstance.gamemode.name}.\n\nFais !join ${gameInstance.game.id} pour rejoindre sa partie ou !reject ${gameInstance.game.id} pour la refuser immédiatement"
                )
            )

            // Send success message to the game owner
            smsSender.sendSMS(
                promptMessage.copy(message = "Invitation envoyée à ${player.name} (${player.phoneNumber})")
            )

            // TODO Add timeout ?

            gameInstance.gameManager.registerInvitation(gameInstance, this)
        }

        // Always return null because the invitation is not accepted nor rejected yet
        return null
    }

    suspend fun accept() {
//        timeout!!.interrupt()

        gameInstance.playerDao.setGame(player!!.id, gameInstance.game.id)
        gameInstance.gameDao.savePlayersOrderOf(
            gameInstance.game.id,
            gameInstance.playersOrder.put(player!!.id)
        )

        smsSender.sendSMS(player!!.phoneNumber, gameInstance.gamemode.rules)

        gameInstance.executor.submit {
            val serializedPlayer = gameInstance.serializePlayer(player!!)
            resolveFunc.call(serializedPlayer)
        }

        gameInstance.invitationMap.remove(player!!.phoneNumber)
    }

    suspend fun reset(reject: Boolean = false) {
        if (reject) {
            smsHandler.sendSMS(
                player!!.phoneNumber,
                "L'invitation de ${owner.name} pour ${gameInstance.gamemode.name} a été rejeté"
            )
            smsHandler.sendSMS(
                to,
                "${player!!.name} a rejeté votre invitation.\nVeuillez entrer un nouveau numéro"
            )
        } else {
            smsHandler.sendSMS(
                player!!.phoneNumber,
                "Votre invitation pour ${gameInstance.gamemode.name} de ${owner.name} a expiré"
            )
            smsHandler.sendSMS(
                to,
                "L'invitation de ${player!!.name} a expiré.\nVeuillez entrer un nouveau numéro"
            )
        }
        player = null
    }

    override suspend fun onSMS(message: String): RoomPlayer? {
        Log.d(TAG, "onSMS: $message")

        validation(message)

        // We don't want to send the prompt message again
        return null
    }
}*/
