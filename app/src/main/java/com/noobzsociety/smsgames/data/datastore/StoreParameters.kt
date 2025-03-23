package com.noobzsociety.smsgames.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreParameters(private val context: Context) {

    companion object {
        private val Context.datastore: DataStore<Preferences> by preferencesDataStore("settings")

        val COMMAND_PREFIX_KEY = stringPreferencesKey("command_char")
        val REGISTER_MESSAGE_KEY = stringPreferencesKey("register_message")
    }

    fun getCommandPrefix(): Flow<String> =
        context.datastore.data.map { it[COMMAND_PREFIX_KEY] ?: "!" }

    suspend fun setCommandPrefix(char: String) {
        context.datastore.edit { it[COMMAND_PREFIX_KEY] = char }
    }

    fun getRegisterMessage(): Flow<String> = context.datastore.data.map {
        it[REGISTER_MESSAGE_KEY] ?: """
        Ceci est mon numéro personnel, donc merci de ne pas spammer comme un dégénéré.
        En vous enregistrant vous acceptez que votre numéro soit accessible par ma personne (historique des SMS)
        C'est tout, si vous êtes ok avec ça, faites la commande suivante ↓
        """.trimIndent()
    }

    suspend fun setRegisterMessage(message: String) {
        context.datastore.edit { it[REGISTER_MESSAGE_KEY] = message }
    }
}