package com.noobzsociety.smsgames.engine

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.noobzsociety.smsgames.data.datastore.StoreParameters
import com.noobzsociety.smsgames.data.room.AppDatabase
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val TAG = SMSHandler::class.simpleName

class SMSHandler(context: Context, database: AppDatabase) {
    private val smsManager = context.getSystemService(SmsManager::class.java)
    private val datastore = StoreParameters(context)

    private val playerDao = database.playerDao()
    private val gamemodeDao = database.gamemodeDao()

    fun handleMessage(from: String, message: String) {
        Log.i(TAG, "Received SMS from ${from}: $message")

        val player: RoomPlayer? = runBlocking {
            playerDao.getByNumber(from)
        }

        if (player == null) {
            handleUnknownPlayer(from, message.trim())
        } else {
            handleKnownPlayer(player, message.trim())
        }
    }

    private fun handleUnknownPlayer(from: String, message: String) {
        val commandPrefix: String = runBlocking { datastore.getCommandPrefix().first() }
        if (message.startsWith(commandPrefix)) {
            Log.i(TAG, "handleUnknownPlayer: $from is not registered")
        }
    }

    private fun handleKnownPlayer(player: RoomPlayer, message: String) {
        Log.i(TAG, "handleKnownPlayer: $player")
    }
}