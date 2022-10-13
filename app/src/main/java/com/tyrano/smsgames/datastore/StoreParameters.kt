package com.tyrano.smsgames.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class StoreParameters(private val context: Context) {

    companion object {
        private val Context.datastore: DataStore<Preferences> by preferencesDataStore("parametersStore")
        val COMMAND_PREFIX_KEY = stringPreferencesKey("command_char")

        //        val BLOCKED_NUMBERS_KEY = stringSetPreferencesKey("blocked_numbers")
        val MY_NUMBER_KEY = stringPreferencesKey("my_number")
        val REGISTER_MESSAGE_KEY = stringPreferencesKey("register_message")
    }

    suspend fun getCommandPrefix(): String {
        return context.datastore.data.first()[COMMAND_PREFIX_KEY] ?: "!"
    }

    suspend fun saveCommandPrefix(char: String) {
        context.datastore.edit { it[COMMAND_PREFIX_KEY] = char }
    }

//    val getBlockedNumbers: Flow<Set<String>> =
//        context.datastore.data.map { it[BLOCKED_NUMBERS_KEY] ?: emptySet() }
//
//    suspend fun saveBlockedNumbers(numbers: Set<String>) {
//        context.datastore.edit { it[BLOCKED_NUMBERS_KEY] = numbers }
//    }

    suspend fun getMyNumber(): String {
        return context.datastore.data.first()[MY_NUMBER_KEY] ?: "+33"
    }

    suspend fun saveMyNumber(number: String) {
        context.datastore.edit { it[MY_NUMBER_KEY] = number }
    }

    suspend fun getRegisterMessage(): String {
        return context.datastore.data.first()[REGISTER_MESSAGE_KEY] ?: """
            Ceci est mon numéro personnel donc merci de ne pas spammer comme un dégénéré.
            En vous enregistrant vous acceptez que votre numéro soit accessible par ma personne (historique des SMS)
            C'est tout, si vous êtes ok avec ça, faites la commande suivante ↓
        """.trimIndent()
    }

    suspend fun saveRegisterMessage(message: String) {
        context.datastore.edit { it[REGISTER_MESSAGE_KEY] = message }
    }
}