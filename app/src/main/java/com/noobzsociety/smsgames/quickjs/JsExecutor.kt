package com.noobzsociety.smsgames.quickjs

import android.util.Log
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext
import com.whl.quickjs.wrapper.QuickJSException

private val TAG = QuickJsExecutor::class.simpleName

class QuickJsExecutor {

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
        // TODO: Save logs for in-app console for easier debugging
        setConsole(object : QuickJSContext.Console {
            override fun log(info: String?) {
                Log.d(TAG, "log: $info")
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

    fun run(script: String, filename: String): Either<RunError, Unit> = either {
        checkScriptSyntax(script, filename)

        quickJs.moduleLoader = object : QuickJSContext.DefaultModuleLoader() {
            override fun getModuleStringCode(moduleName: String?) = when (moduleName) {
                filename -> script
                else -> null
            }
        }

        checkForFunction("setup", filename, RunError.SetupFunctionNotDefined)
        checkForFunction("main", filename, RunError.MainFunctionNotDefined)

        try {
            quickJs.evaluateModule(
                // TODO: On update, save in DB
                """
                import { setup, main } from "$filename";
                
                const truc = setup();

                const proxy = new Proxy(truc, {
                    set: function (target, key, value) {
                        target[key] = value;
                        console.log(JSON.stringify(target));
                        return true;
                    },
                    deleteProperty: function (target, key) {
                        delete target[key];
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

    private fun Raise<RunError>.checkForFunction(
        function: String,
        filename: String,
        error: RunError
    ) {
        try {
            quickJs.evaluateModule(
                """
                import { $function } from "$filename";
    
                if (typeof $function !== "function") {
                    throw new Error("Function is not defined");
                }
                """.trimIndent(),
                "${function}_testing.js",
            )
        } catch (e: QuickJSException) {
            e.printStackTrace()
            raise(error)
        }
    }

    private fun Raise<RunError>.checkScriptSyntax(script: String, filename: String) {
        try {
            quickJs.evaluateModule(script, filename)
        } catch (e: QuickJSException) {
            val message = e.message!!

            // If the error is a syntax error, we return a ScriptParsingError
            ensure(!message.startsWith("SyntaxError")) { RunError.ScriptParsingError(message) }

            // Otherwise, we return a RuntimeError
            raise(RunError.RuntimeError(message))
        }
    }
}