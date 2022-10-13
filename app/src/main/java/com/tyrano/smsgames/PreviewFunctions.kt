package com.tyrano.smsgames

import com.tyrano.smsgames.entities.PlayerEntity
import com.tyrano.smsgames.light.LightGame
import com.tyrano.smsgames.light.LightGamemode
import com.tyrano.smsgames.viewmodels.IGameVM
import com.tyrano.smsgames.viewmodels.IGamemodeVM
import com.tyrano.smsgames.viewmodels.ILightGameListVM
import com.tyrano.smsgames.viewmodels.ILightGamemodeListVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.time.Instant

fun getPreviewLightGamemodeListVM(): ILightGamemodeListVM {
    return object : ILightGamemodeListVM {

        override val lightGamemodes: Flow<List<LightGamemode>>
            get() = MutableStateFlow(emptyList())

        override fun toggle(lightGamemode: LightGamemode) {
        }

        override fun delete(lightGamemode: LightGamemode) {
        }
    }
}

fun getPreviewLightGameListVM(): ILightGameListVM {
    return object : ILightGameListVM {
        override val lightGames: Flow<List<LightGame>>
            get() = MutableStateFlow(emptyList())

        override fun delete(lightGame: LightGame) {
            TODO("Not yet implemented")
        }
    }
}

fun getPreviewGamemodeVM(): IGamemodeVM {
    return object : IGamemodeVM {
        override var id: Long
            get() = 0
            set(_) {}
        override var name: String
            get() = "Nouveau jeu"
            set(_) {}
        override var code: String
            get() = """
        // Fonction appelée à chaque message
        async function game() {
            
        }
    """.trimIndent()
            set(_) {}
        override var rules: String
            get() = """
        - Première règle
        - Autre chose
    """.trimIndent()
            set(_) {}
        override var settings: JSONObject
            get() = JSONObject("{clé:valeur,autre_clé:42}")
            set(_) {}
        override val isDirty: Boolean
            get() = false

        override fun insert() {
            TODO("Not yet implemented")
        }

        override fun update() {
            TODO("Not yet implemented")
        }
    }
}

fun getPreviewGameVM(): IGameVM {
    return object : IGameVM {
        override var id: Long
            get() = 1
            set(_) {}
        override var gamemodeName: String
            get() = "Jeu de fou"
            set(_) {}
        override var owner: PlayerEntity
            get() = PlayerEntity(1, "Le chef", false, "+33", 1)
            set(_) {}
        override var start: Instant?
            get() = Instant.ofEpochSecond(1661698695)
            set(_) {}
        override var players: List<PlayerEntity>
            get() = listOf(
                owner,
                PlayerEntity(2, "Joueur 1", false, "+33", 1),
                PlayerEntity(3, "Joueur 2", false, "+33", 1),
                PlayerEntity(4, "Joueur 3", false, "+33", 1)
            )
            set(_) {}
        override var data: JSONObject
            get() = JSONObject("{key: 42, key2:'osef', key3: [3,2,1]}")
            set(_) {}
        override var settings: JSONObject
            get() = JSONObject()
            set(_) {}

        override fun toString(): String {
            return "GameVM(id=$id, gamemodeName='$gamemodeName', owner=$owner, start=$start, teams=$players, data=${data.toString(2)}, settings=${settings.toString(2)})"
        }
    }
}

fun getPreviewLightGamemodeList(): List<LightGamemode> {
    return listOf(LightGamemode(1, "Exemple", true), LightGamemode(2, "Autre jeu", false))
}

fun getPreviewLightGameList(): List<LightGame> {
    return listOf(
        LightGame(1, "Puissance 4", "Michel", Instant.now().minusSeconds(5000)),
        LightGame(2, "Pendu", "Sardou", Instant.now().minusSeconds(10)),
        LightGame(3, "Plus ou Moins", "42", null)
    )
}