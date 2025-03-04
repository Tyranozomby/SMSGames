package com.noobzsociety.smsgames.quickjs

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.QuickJsException
import com.dokar.quickjs.binding.define
import com.dokar.quickjs.binding.function
import kotlinx.coroutines.Dispatchers

abstract class QuickJsExecutor {
    sealed class RunError {
        data class ScriptParsingError(val message: String) : RunError()
        data object SetupFunctionNotDefined : RunError()
        data object MainFunctionNotDefined : RunError()
        data class RuntimeError(val message: String) : RunError()
    }

    private val quickJs = QuickJs.create(Dispatchers.Default)

    init {
        quickJs.define("console") {
            function("log", ::log)
        }

        quickJs.function("log") { args -> println(args.joinToString(" ")) }
    }

    abstract fun log(vararg args: Any?)

    suspend fun run(script: String, filename: String): Either<RunError, Unit> = either {
        try {
            quickJs.evaluate<Unit>(script, filename = filename)
        } catch (e: QuickJsException) {
            val message = e.message!!
            ensure(!message.startsWith("SyntaxError")) { RunError.ScriptParsingError(message) }
            raise(RunError.RuntimeError(message))
        }

        try {
            quickJs.evaluate<Unit>(
                """
                // Check if the setup function is defined
                if (typeof setup !== "function") {
                    throw new Error("The setup function is not defined")
                }
                """.trimIndent()
            )
        } catch (e: QuickJsException) {
            raise(RunError.SetupFunctionNotDefined)
        }

        try {
            quickJs.evaluate<Unit>(
                """
                // Check if the main function is defined
                if (typeof main !== "function") {
                    throw new Error("The main function is not defined")
                }
                """.trimIndent()
            )
        } catch (e: QuickJsException) {
            raise(RunError.MainFunctionNotDefined)
        }

        try {
            quickJs.evaluate<Unit>(
                """
                main.call({
                    data: setup(),
                    params: {},
                })
                """.trimIndent()
            )
        } catch (e: QuickJsException) {
            raise(RunError.RuntimeError(e.message!!))
        }
    }
}

class DefaultQuickJsExecutor : QuickJsExecutor() {
    override fun log(vararg args: Any?) = println(args.joinToString(" "))
}