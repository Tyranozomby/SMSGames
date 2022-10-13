package com.tyrano.smsgames.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SMSReceiver : BroadcastReceiver() {

    @Inject
    lateinit var handler: IncomingSMSHandler

    override fun onReceive(context: Context, intent: Intent) {

        val bundle: Bundle = intent.extras!!
        val format: String = bundle.getString("format")!!
        @Suppress("UNCHECKED_CAST") val pdus = bundle.get("pdus") as Array<ByteArray>

        val fullMessage = pdus.map { SmsMessage.createFromPdu(it, format) }
            .filter { it.originatingAddress != null }
            .fold("" to StringBuilder()) { acc, msg -> msg.originatingAddress!! to acc.second.append(msg.messageBody) }

        handler.handleMessage(fullMessage.first, fullMessage.second.toString())
    }
}