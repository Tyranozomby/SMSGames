package com.noobzsociety.smsgames.engine

import android.telephony.SmsManager
import android.util.Log

private val TAG = SmsSender::class.simpleName

interface SmsSender {
    fun sendSMS(sms: SMSMessage)
}

class AndroidSmsSender(
    private val smsManager: SmsManager,
) : SmsSender {
    override fun sendSMS(sms: SMSMessage) {
        if (sms.message.isBlank()) {
            Log.w(TAG, "sendSMS: ${sms.phoneNumber} - empty message")
        } else {
            smsManager.sendMultipartTextMessage(
                sms.phoneNumber.number,
                null,
                smsManager.divideMessage(sms.message),
                null,
                null
            )
        }
    }
}