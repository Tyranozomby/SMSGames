package com.tyrano.smsgames.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.entities.GamemodeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import kotlin.reflect.KProperty

interface IGamemodeVM {
    var id: Long
    var name: String
    var code: String
    var rules: String
    var settings: JSONObject
    val isDirty: Boolean
    fun insert()
    fun update()
}

@HiltViewModel
class GamemodeVM @Inject constructor(private val dao: GamemodeDao) : ViewModel(), IGamemodeVM {

    override var id: Long by DirtyDelegate(mutableStateOf(0))

    override var name: String by DirtyDelegate(mutableStateOf("Nouveau jeu"))

    private var enabled: Boolean by DirtyDelegate(mutableStateOf(false))

    override var code: String by DirtyDelegate(
        mutableStateOf(
            """
    // Fonction appelée au début de la partie
    async function main() {
        const crashed = setData({
            // Définition des données pour remplir l'objet data
            key: ""
        });
        
        // Code exécuté si la partie n'a pas été interrompue
        if (!crashed) {
            data.key = "value";
        }
        
        // owner est un Player correspondant au créateur de la partie
        const mot = await owner.promptString("Entrez un mot");
        const n = await owner.promptInteger("Entrez un nombre");
        
        log("Vous avez entré le mot " + mot + " et le nombre " + n); // Log dans la console
        
        // Attend n secondes
        await delay(n * 1000);
        
        data.key = mot;
        saveData(); // Sauvegarde les données (pour supporter les crashs)
        
        owner.send("Vous avez entré le mot " + mot);
        
        const player = await owner.askForPlayer("Choisissez un joueur");
        
        player.send("Bienvenue " + player.name); // Nom du joueur
        
        // Envoie un message à tous les joueurs
        broadcast("Bonjour à tous " + params.clé, [owner, player]);
    }
    """.trimIndent()
        )
    )

    override var rules: String by DirtyDelegate(
        mutableStateOf(
            """
        - Première règle
        - Autre chose
    """.trimIndent()
        )
    )

    override var settings: JSONObject by DirtyDelegate(mutableStateOf(JSONObject("{clé:valeur,autre_clé:42}")))

    override var isDirty: Boolean = false
        private set

    inner class DirtyDelegate<T>(private var value: MutableState<T>) {
        operator fun getValue(gamemodeVM: GamemodeVM, property: KProperty<*>): T = value.value

        operator fun setValue(gamemodeVM: GamemodeVM, property: KProperty<*>, newValue: T) {
            value.value = newValue
            isDirty = true
        }
    }

    fun get(idGamemode: Long) {
        if (idGamemode != 0L)
            viewModelScope.launch {
                val gamemode = dao.get(idGamemode)
                id = gamemode.id
                name = gamemode.name
                enabled = gamemode.enabled
                code = gamemode.code
                rules = gamemode.rules
                settings = gamemode.settings
                isDirty = false
            }
    }

    private fun toEntity(): GamemodeEntity {
        return GamemodeEntity(
            this.id,
            this.name,
            this.enabled,
            this.code,
            this.rules,
            this.settings
        )
    }

    override fun insert() {
        val copy = this
        viewModelScope.launch {
            dao.insert(copy.toEntity())
        }
    }

    override fun update() {
        val copy = this
        viewModelScope.launch {
            dao.update(copy.toEntity())
        }
    }
}