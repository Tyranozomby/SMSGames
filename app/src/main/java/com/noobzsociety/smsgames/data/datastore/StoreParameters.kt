package com.noobzsociety.smsgames.data.datastore

import com.russhwolf.settings.ObservableSettings

interface AppSettings {
    val isInitDone: EditableFlowSetting<Boolean>
    val commandPrefix: EditableFlowSetting<Char>
    val registerMessage: EditableFlowSetting<String>
    val ownerCommand: EditableFlowSetting<String>

    companion object {
        operator fun invoke(
            settings: ObservableSettings,
        ) = object : AppSettings {
            override val isInitDone = settings.serializedFlowSetting(
                key = "isInitDone",
                default = false,
            )

            override val commandPrefix = settings.serializedFlowSetting(
                key = "commandPrefix",
                default = '!',
            )

            override val registerMessage = settings.serializedFlowSetting(
                key = "registerMessage",
                default = """
                Ceci est mon numéro personnel, donc merci de ne pas spammer comme un dégénéré.
                En vous enregistrant vous acceptez que votre numéro soit accessible par ma personne (historique des SMS)
                C'est tout, si vous êtes ok avec ça, faites la commande suivante ↓
                """.trimIndent(),
            )

            override val ownerCommand = settings.serializedFlowSetting(
                key = "ownerCommand",
                default = "do",
            )
        }
    }
}