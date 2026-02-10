package com.noobzsociety.smsgames.koin

import android.content.Context
import android.telephony.SmsManager
import com.noobzsociety.smsgames.engine.AndroidSmsSender
import com.noobzsociety.smsgames.engine.GameManager
import com.noobzsociety.smsgames.engine.SmsHandler
import com.noobzsociety.smsgames.engine.SmsSender
import org.koin.dsl.module

val engineModule = module {
    single<SmsSender> {
        val context = get<Context>()
        val smsManager = context.getSystemService(SmsManager::class.java)

        AndroidSmsSender(smsManager)
    }

    single<SmsHandler> { SmsHandler(get(), get(), get(), get(), get()) }

    single<GameManager> { GameManager(get(), get(), get(), get()) }
}