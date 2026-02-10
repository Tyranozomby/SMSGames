package com.noobzsociety.smsgames.engine

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.noobzsociety.smsgames.data.room.dao.GamemodeDao
import com.noobzsociety.smsgames.data.room.dao.PlayerDao
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseCommand(
    name: String,
    val help: String,
    context: android.content.Context
) : SuspendingCliktCommand(name) {
    override val printHelpOnEmptyArgs: Boolean = true
    override fun help(context: Context): String = help

    init {
        context {
            this.localization = CustomLocalization(CommandHandler.COMMAND_PREFIX, context)
        }
    }
}

class HelpCommand(private val commandHandler: CommandHandler) : SuspendingCliktCommand(HELP_CMD) {

    override fun help(context: Context): String = "Afficher les commandes existantes"

    override suspend fun run() {
        commandHandler.echoFormattedHelp()
    }
}

class RegisterCommand(
    private val from: PhoneNumber,
    private val playerDao: PlayerDao,
    context: android.content.Context
) : BaseCommand(REGISTER_CMD, "S'enregistrer ou lire les conditions d'utilisation", context) {

    private val name by argument(
        name = "pseudo",
        help = "Votre pseudo",
    ).multiple(required = true)

    override suspend fun run() {
        val joinedName = name.joinToString(" ")

        playerDao.insert(
            RoomPlayer(
                name = joinedName,
                phoneNumber = from,
            )
        )

        echo(
            """
            Bienvenue $joinedName !
            
            Faites ${CommandHandler.COMMAND_PREFIX}${HELP_CMD} pour voir les commandes existantes
            """.trimIndent()
        )
    }
}

class NickCommand(
    private val player: RoomPlayer,
    private val playerDao: PlayerDao,
    context: android.content.Context
) : BaseCommand(NICK_CMD, "Changer de pseudo", context) {

    private val name by argument(
        name = "pseudo",
        help = "Votre nouveau pseudo"
    ).multiple(required = true)

    override suspend fun run() {
        val joinedName = name.joinToString(" ")

        playerDao.update(
            player.copy(name = joinedName)
        )

        echo(
            """
            Votre pseudo a été changé en $joinedName !
            
            Faites ${CommandHandler.COMMAND_PREFIX}${HELP_CMD} pour voir les commandes existantes
            """.trimIndent()
        )
    }
}

class RemoveAccountCommand(
    private val player: RoomPlayer,
    context: android.content.Context
) : BaseCommand(REMOVE_ACCOUNT_CMD, "Supprimer son compte", context) {

    private val code by argument(name = "code", help = "Code de confirmation").optional()

    override suspend fun run() {

    }
}

class PlayCommand(
    private val player: RoomPlayer?,
    private val gamemodeDao: GamemodeDao,
    private val gameManager: GameManager,
    private val gameScope: CoroutineScope,
    context: android.content.Context
) : BaseCommand(
    PLAY_CMD,
    if (player == null) "Nécessite d'être inscrit·e" else "Lancer une partie",
    context
) {
    override val printHelpOnEmptyArgs: Boolean = false

    private val id by argument(name = "id", help = "Le numéro du jeu").int().optional()

    override suspend fun run() {
        if (player == null) {
            echo(
                """
                Vous devez d'abord vous inscrire pour lancer une partie.
                Pour cela, faites ${CommandHandler.COMMAND_PREFIX}${REGISTER_CMD}""".trimIndent()
            )
        } else {
            val gamemodeList =
                (if (!player.isTester) gamemodeDao.getAllEnabled() else gamemodeDao.getAll()).first()

            if (id == null || id!! <= 0 || id!! > gamemodeList.size) {
                val stringList = gamemodeList.mapIndexed { index, gamemode ->
                    "${index + 1} - ${gamemode.name}"
                }.joinToString("\n")

                echo(
                    """
                    Liste des jeux :
                    $stringList
                    
                    Faites ${CommandHandler.COMMAND_PREFIX}${PLAY_CMD} <numéro du jeu>
                    """.trimIndent()
                )
            } else {
                val gamemode = gamemodeList[id!! - 1]

                echo("Votre partie de ${gamemode.name} va commencer.\n\n${gamemode.rules}")

                gameScope.launch {
                    gameManager.startNewGame(gamemode, player, null) // TODO Get settings from options
                }
            }
        }
    }
}

class RulesCommand() : SuspendingCliktCommand(RULES_CMD) {

    override fun help(context: Context): String = "Lire les règles du jeu"

    override suspend fun run() {

    }
}

class JoinCommand(private val player: RoomPlayer, private val gameManager: GameManager) :
    SuspendingCliktCommand(JOIN_CMD) {
    private val gameId by argument(name = "id", help = "Le numéro de la partie").long()

    override fun help(context: Context): String = "Rejoindre une partie"

    override suspend fun run() {
//        val invitation = gameManager.getInvitationOrNull(gameId, player)
//        if (invitation == null) {
//            echo("Vous n'avez pas été invité·e dans cette partie, désolé")
//        } else {
//            invitation.accept()
//        }
    }
}

class RejectCommand(player: RoomPlayer, gameManager: GameManager) : SuspendingCliktCommand(REJECT_CMD) {
    private val gameId by argument(name = "id", "Le numéro de la partie").long()

    override fun help(context: Context): String = "Rejeter une invitation"

    override suspend fun run() {

    }
}

class ForfeitCommand() : SuspendingCliktCommand(FORFEIT_CMD) {
    override fun help(context: Context): String = "Abandonner une partie"

    override suspend fun run() {

    }
}