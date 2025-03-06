package com.noobzsociety.smsgames.quickjs

import android.util.Log
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.JSMethod
import com.whl.quickjs.wrapper.QuickJSContext
import com.whl.quickjs.wrapper.QuickJSException

private const val TAG = "QuickJsExecutor"

abstract class QuickJsExecutor {

    sealed class RunError {
        data class ScriptParsingError(val message: String) : RunError()
        data object SetupFunctionNotDefined : RunError()
        data object MainFunctionNotDefined : RunError()
        data class RuntimeError(val message: String) : RunError()
    }

    init {
        QuickJSLoader.init()
    }

    private val quickJs: QuickJSContext = QuickJSContext.create().apply {
        setConsole(object : QuickJSContext.Console {
            @JSMethod
            override fun log(info: String?) {
                Log.i(TAG, "log: $info")
            }

            override fun info(info: String?) {
                Log.i(TAG, "info: $info")
            }

            override fun warn(info: String?) {
                Log.w(TAG, "warn: $info")
            }

            override fun error(info: String?) {
                Log.e(TAG, "error: $info")
            }
        })
    }

    abstract fun log(vararg args: Any?)

    fun run(script: String, filename: String): Either<RunError, Unit> = either {
        try {
            quickJs.evaluateModule(script, filename)
        } catch (e: QuickJSException) {
            val message = e.message!!

            // If the error is a syntax error, we return a ScriptParsingError
            ensure(!message.startsWith("SyntaxError")) { RunError.ScriptParsingError(message) }

            // Otherwise, we return a RuntimeError
            raise(RunError.RuntimeError(message))
        }

        quickJs.moduleLoader = object : QuickJSContext.DefaultModuleLoader() {
            override fun getModuleStringCode(moduleName: String?) = when (moduleName) {
                filename -> script
                else -> null
            }
        }

        try {
            quickJs.evaluateModule(
                """
                import { setup } from "$filename";

                // Check if the setup function is defined
                if (typeof setup !== "function") {
                    throw new Error("The setup function is not defined");
                }
                """.trimIndent(),
                "setup_testing.js"
            )
        } catch (e: QuickJSException) {
            e.printStackTrace()
            raise(RunError.SetupFunctionNotDefined)
        }

        try {
            quickJs.evaluateModule(
                """
                import { main } from "$filename";

                // Check if the main function is defined
                if (typeof main !== "function") {
                    throw new Error("The main function is not defined");
                }
                """.trimIndent(),
                "main_testing.js",
            )
        } catch (e: QuickJSException) {
            e.printStackTrace()
            raise(RunError.MainFunctionNotDefined)
        }

        try {
            quickJs.evaluateModule(
                """
                import { setup, main } from "$filename";
                
                const truc = setup();

                const proxy = new Proxy(truc, {
                    set: function (target, key, value) {
                        target[key] = value;
                        console.log(JSON.stringify(target));
                        return true;
                    }
                });

                main.call(proxy);
                """.trimIndent(),
                "main.js"
            )
        } catch (e: QuickJSException) {
            raise(RunError.RuntimeError(e.message!!))
        }
    }
}

class DefaultQuickJsExecutor : QuickJsExecutor() {
    override fun log(vararg args: Any?) {
        println("Log: ${args.joinToString(separator = " ")}")
    }
}