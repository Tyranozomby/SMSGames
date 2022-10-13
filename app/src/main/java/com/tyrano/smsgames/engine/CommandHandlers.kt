package com.tyrano.smsgames.engine

import android.util.Log
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.dao.PlayerDao
import com.tyrano.smsgames.entities.PlayerEntity
import kotlinx.coroutines.runBlocking

const val REGISTER_CMD = "register"
const val HELP_CMD = "help"
const val NICK_CMD = "nick"
const val PLAY_CMD = "play"
const val RULES_CMD = "rules"
const val FORFEIT_CMD = "forfeit"
const val JOIN_CMD = "join"
const val REJECT_CMD = "reject"
const val REMOVE_ACCOUNT_CMD = "farewell"
const val DELETE_GAMES_CMD = "deletegames"

class MyConsole(private val smsHandler: IncomingSMSHandler, private val from: String) : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        return null
    }

    override fun print(text: String, error: Boolean) {
        smsHandler.sendSMS(from, text.trim())
    }

    override val lineSeparator: String get() = "\n"
}

abstract class BaseCommand(
    name: String? = null,
    help: String = "",
    epilog: String = "",
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    allowMultipleSubcommands: Boolean = false,
    treatUnknownOptionsAsArgs: Boolean = false,
    hidden: Boolean = false,
) : CliktCommand(
    help,
    epilog,
    name,
    invokeWithoutSubcommand,
    printHelpOnEmptyArgs,
    helpTags,
    autoCompleteEnvvar,
    allowMultipleSubcommands,
    treatUnknownOptionsAsArgs,
    hidden
) {
    init {
        context {
            localization = CustomLocalization()
            helpOptionNames = emptySet()
        }
    }
}

class CustomLocalization : Localization {
    override fun commandMetavar(): String {
        return "\n!commande valeur (facultative)"
    }

    override fun commandsTitle(): String {
        return "Liste des commandes :"
    }


    override fun usageTitle(): String {
        return "Toutes les commandes commencent par un !\n\nUtilisation :\n"
    }
}

class Main(commandPrefix: String, smsHandler: IncomingSMSHandler, from: String) : BaseCommand("") {
    override fun run() = Unit

    companion object {
        lateinit var COMMAND_PREFIX: String
    }

    init {
        COMMAND_PREFIX = commandPrefix
    }

    infix fun parsedWith(argv: List<String>) {
        try {
            parse(argv)
        } catch (_: ProgramResult) {
        } catch (e: PrintHelpMessage) {
//            echo(e.command.getFormattedHelp())
        } catch (e: PrintCompletionMessage) {
            val s = if (e.forceUnixLineEndings) "\n" else currentContext.console.lineSeparator
            echo(e.message, lineSeparator = s)
        } catch (e: PrintMessage) {
            echo(e.message)
        } catch (e: UsageError) {
//            echo(e.helpMessage(), err = true)
        } catch (e: CliktError) {
            echo(e.message, err = true)
        } catch (e: Abort) {
            echo(currentContext.localization.aborted(), err = true)
        }
    }

    init {
        context { console = MyConsole(smsHandler, from) }
    }
}

class RegisterCommand(private val dao: PlayerDao, private val from: String, private val registerMessage: String) :
    BaseCommand(REGISTER_CMD, "S'enregistrer ou lire les conditions d'utilisation") {

    private val name by argument(help = "Votre pseudo").multiple().optional()

    override fun run() {
        if (name == null) {
            echo("${registerMessage}\n\n${Main.COMMAND_PREFIX}$REGISTER_CMD <pseudo>")
        } else {
            val joinedList = name!!.joinToString(" ")
            runBlocking {
                dao.register(PlayerEntity(0, joinedList, false, from, null))
            }
            echo("Bienvenue $joinedList !\nFaites ${Main.COMMAND_PREFIX}$HELP_CMD pour voir les commandes existantes")
        }
    }
}

class HelpCommand(private val mainCommand: Main) : BaseCommand(HELP_CMD, "Afficher les commandes existantes") {

    override fun run() {
        echo(mainCommand.getFormattedHelp())
    }
}

class NickCommand(private val dao: PlayerDao, private val player: PlayerEntity) :
    BaseCommand(NICK_CMD, "Changer de pseudo", treatUnknownOptionsAsArgs = true) {

    private val name by argument(help = "Votre nouveau pseudo").multiple()

    override fun run() {
        val joinedString = name.joinToString(" ").trim()
        if (name.isEmpty() || joinedString.isEmpty()) {
            echo("Vous devez entrer un nouveau pseudo\nExemple: !nick Robert le Fougère")
        } else {
            runBlocking {
                dao.updateNick(player.id, joinedString)
            }
            echo("Votre nouveau pseudo est $joinedString")
        }
    }
}

class RemoveAccountCommand(private val dao: PlayerDao, private val player: PlayerEntity) :
    BaseCommand(REMOVE_ACCOUNT_CMD, "Supprimer son compte") {
    override fun run() {
        runBlocking {
            dao.delete(player)
        }
        echo("Votre compte a bien été supprimé")
    }
}

class PlayCommand(
    private val gamemodeDao: GamemodeDao,
    private val player: PlayerEntity?,
    private val gameManager: GameManager,
    private val isMe: Boolean = false
) :
    BaseCommand(
        PLAY_CMD,
        if (player == null) "Nécessite d'être inscrit·e" else "Lancer une partie",
        treatUnknownOptionsAsArgs = true
    ) {

    private val id by argument(help = "Le numéro du jeu").int().optional()

    override fun run() {
        if (player == null) {
            echo("Vous devez d'abord vous inscrire pour lancer une partie.\nPour cela, faites ${Main.COMMAND_PREFIX}$REGISTER_CMD")
        } else {
            val gamemodeList = runBlocking {
                if (!isMe)
                    gamemodeDao.getAllEnabled()
                else
                    gamemodeDao.getAll()
            }

            if (id == null || id!! <= 0 || id!! > gamemodeList.size) {
                var stringList = ""

                for (i in 1..gamemodeList.size) {
                    stringList += "$i - ${gamemodeList[i - 1].name}\n"
                }

                echo(
                    "Liste des jeux :\n$stringList\nFaites ${Main.COMMAND_PREFIX}$PLAY_CMD <numéro du jeu>"
                )
            } else {
                val gamemode = gamemodeList[id!! - 1]
                echo("Votre partie de ${gamemode.name} va commencer.\n\n${gamemode.rules}")
                gameManager.createGame(gamemode, player)
            }
        }
    }
}

class RulesCommand(
    private val playerDao: PlayerDao,
    private val player: PlayerEntity
) :
    BaseCommand(RULES_CMD, "Lire les règles du jeu") {

    override fun run() {
        val gamemode = runBlocking {
            playerDao.getPlayingGamemode(player.id)!!
        }
        echo(gamemode.rules)
    }
}

class JoinCommand(private val gameManager: GameManager, private val player: PlayerEntity) :
    BaseCommand(JOIN_CMD, "Rejoindre une partie après une invitation") {

    private val gameId by argument("Le numéro de la partie").long().optional()

    override fun run() {
        if (gameId == null) {
            echo("Vous devez donner le numéro de partie présent dans l'invitation.\nExemple : ${Main.COMMAND_PREFIX}$JOIN_CMD 42")
        } else {
            val gameInstance = gameManager.games[gameId]
            if (gameInstance == null) {
                echo("Le numéro de partie est invalide. Celle-ci a peut-être été abandonnée")
            } else {
                val invitation = gameInstance.invitationMap[player.phoneNumber]
                if (invitation == null) {
                    echo("Vous n'avez pas été invité·e dans cette partie, désolé")
                } else {
                    invitation.accept()
                }
            }
        }
    }
}

class RejectCommand(private val gameManager: GameManager, private val player: PlayerEntity) :
    BaseCommand(REJECT_CMD, "Refuser une invitation") {

    private val gameId by argument("Le numéro de la partie").long().optional()

    override fun run() {
        if (gameId == null) {
            echo("Vous devez donner le numéro de partie présent dans l'invitation.\nExemple : ${Main.COMMAND_PREFIX}$REJECT_CMD 42")
        } else {
            val gameInstance = gameManager.games[gameId]
            if (gameInstance == null) {
                echo("Le numéro de partie est invalide. Celle-ci a peut-être été abandonnée")
            } else {
                val invitation = gameInstance.invitationMap[player.phoneNumber]
                if (invitation == null) {
                    echo("Vous n'avez pas été invité·e dans cette partie")
                } else {
                    invitation.reset(true)
                }
            }
        }
    }
}

class DeleteGamesCommand(private val gameDao: GameDao) :
    BaseCommand(DELETE_GAMES_CMD, "Supprimer les parties parce que j'ai tout cassé") {

    override fun run() {
        Log.d(TAG, "run: ici")
        runBlocking {
            gameDao.getAll().forEach { gameDao.delete(it.id) }
        }
        echo("C'est fait 👍🏼")
    }
}