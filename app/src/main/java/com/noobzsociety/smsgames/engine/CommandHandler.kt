package com.noobzsociety.smsgames.engine

import com.github.ajalt.clikt.command.SuspendingNoOpCliktCommand
import com.github.ajalt.clikt.command.parse
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInfo
import com.github.ajalt.mordant.terminal.TerminalInterface
import com.noobzsociety.smsgames.R
import kotlin.properties.Delegates

const val HELP_CMD = "help"
const val REGISTER_CMD = "register"
const val REMOVE_ACCOUNT_CMD = "farewell"
const val NICK_CMD = "nick"
const val PLAY_CMD = "play"
const val RULES_CMD = "rules"
const val FORFEIT_CMD = "forfeit"
const val JOIN_CMD = "join"
const val REJECT_CMD = "reject"

class CommandHandler(
    private val commandPrefix: Char,
    from: PhoneNumber,
    smsSender: SmsSender,
    context: android.content.Context
) : SuspendingNoOpCliktCommand("") {

    companion object {
        var COMMAND_PREFIX by Delegates.notNull<Char>()
    }

    suspend fun handle(message: String) {
        val args = message.substring(1).trimStart().split(" ").toMutableList()
        args[0] = args[0].lowercase()

        try {
            this.parse(args)
        } catch (e: CliktError) {
            println(e)
            this.echoFormattedHelp(e)
        }
    }

    init {
        COMMAND_PREFIX = commandPrefix

        context {
            this.terminal = Terminal(terminalInterface = CustomTerminal(smsSender, from))
            this.localization = CustomLocalization(commandPrefix, context)

            this.helpFormatter = { CustomHelpFormatter(it, commandPrefix) }
            this.helpOptionNames = emptySet()
        }
    }
}

private class CustomTerminal(
    private val smsSender: SmsSender,
    private val from: PhoneNumber
) : TerminalInterface {
    override fun info(
        ansiLevel: AnsiLevel?,
        hyperlinks: Boolean?,
        outputInteractive: Boolean?,
        inputInteractive: Boolean?
    ): TerminalInfo = TerminalInfo(AnsiLevel.NONE, false, false, false, false)

    override fun completePrintRequest(request: PrintRequest) {
        println(request.text)
        smsSender.sendSMS(SMSMessage(from, request.text))
    }

    override fun readLineOrNull(hideInput: Boolean): String? = null
}

class CustomLocalization(private val commandPrefix: Char, private val context: android.content.Context) : Localization {

    override fun commandsTitle(): String = context.resources.getString(R.string.commands_commands_title)

    override fun usageTitle(): String = context.resources.getString(R.string.commands_usage_title, commandPrefix)

    override fun usageError(): String = context.resources.getString(R.string.commands_usage_error)
    override fun missingArgument(paramName: String): String =
        context.resources.getString(R.string.commands_missing_argument, paramName)

    override fun commandMetavar(): String = context.resources.getString(R.string.commands_command_metavar)
    override fun argumentsMetavar(): String = context.resources.getString(R.string.commands_arguments_metavar)

    override fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return if (possibilities.isEmpty()) {
            context.resources.getString(R.string.commands_no_such_subcommand_empty, name, commandPrefix + HELP_CMD)
        } else {
            context.resources.getString(R.string.commands_no_such_subcommand, name, possibilities.joinToString(", "))
        }
    }
}

private class CustomHelpFormatter(context: Context, private val commandPrefix: Char) :
    HelpFormatter {
    private val subFormatter = PlaintextHelpFormatter(context)

    override fun formatHelp(
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String
    ): String = subFormatter.formatHelp(
        error,
        prolog,
        epilog,
        parameters,
        programName.replace(Regex("^command-handler\\s?"), commandPrefix.toString())
    )
}