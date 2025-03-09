package com.noobzsociety.smsgames.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle: Bundle = intent.extras!!
        val format: String = bundle.getString("format")!!

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        val pdus = bundle.get("pdus") as Array<ByteArray>

        val fullMessage = pdus.map { SmsMessage.createFromPdu(it, format) }
            .filter { it.originatingAddress != null }
            .fold("" to "") { acc, msg -> msg.originatingAddress!! to acc.second + (msg.messageBody) }

        Log.i("SMSReceiver", "Received SMS from ${fullMessage.first}: ${fullMessage.second}")
    }
}