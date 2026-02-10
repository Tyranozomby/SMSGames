package com.noobzsociety.smsgames.engine

import android.util.Log
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import com.noobzsociety.smsgames.data.FileManager
import com.noobzsociety.smsgames.data.room.dao.GameDao
import com.noobzsociety.smsgames.data.room.entities.RoomGame
import com.noobzsociety.smsgames.data.room.entities.RoomPlayer
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.JSArray
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import com.whl.quickjs.wrapper.QuickJSException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.reflect.typeOf

private val TAG = IJsExecutor::class.simpleName

abstract class IJsExecutor(
    val game: RoomGame,
    val owner: RoomPlayer,
) {
    sealed class RunError {
        data object MainFunctionNotDefined : RunError()
        data object SetupFunctionNotDefined : RunError()
        data class ScriptParsingError(val message: String) : RunError()
        data class RuntimeError(val message: String) : RunError()
    }

    data class JsPromise(
        val promise: JSObject,
        val resolveFunc: JSFunction,
        val rejectFunc: JSFunction
    ) {
        companion object {
            const val PROMISE_CONSTRUCTOR = "promiseConstructor"
            const val RESOLVE = "resolve"
            const val REJECT = "reject"
            const val PROMISE = "promise"

            const val PROMISE_CONSTRUCTOR_FUNCTION = """
                function $PROMISE_CONSTRUCTOR() {
                    let outsideResolve, outsideReject
                    const promise = new Promise((resolve, reject) => {
                        outsideResolve = resolve
                        outsideReject = reject
                    })
                    return {$PROMISE: promise, $RESOLVE: outsideResolve, $REJECT: outsideReject}
                }
            """
        }
    }

    private val quickJsScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
//    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private val gameEndNotificationChannel = Channel<Either<String, Unit>>()

    private lateinit var promiseConstructor: JSFunction

    private lateinit var quickJs: QuickJSContext

    init {
        QuickJSLoader.init()
    }

    protected suspend fun resetQuickJs() {
        withContext(quickJsScope.coroutineContext) {
            if (::quickJs.isInitialized) {
                quickJs.close()
            }

            quickJs = QuickJSContext.create().apply {
                setConsole(console)

                evaluate(JsPromise.PROMISE_CONSTRUCTOR_FUNCTION)
                promiseConstructor = globalObject.getJSFunction(JsPromise.PROMISE_CONSTRUCTOR)

                globalObject.setFunction("saveData") {
                    saveData(it[0] as JSObject)
                }

                globalObject.setFunction("getOwner") {
                    serializePlayer(owner)
                }

                globalObject.setAsyncFunction<Unit>("notifyGameFinished") {
                    gameEndNotificationChannel.send(Unit.right())
                }

                globalObject.setAsyncFunction<Unit>("notifyGameCrashed") {
                    val error = it[0].stringify()
                    println("Game crashed: $error")
                    gameEndNotificationChannel.send(error.left())
                }

                globalObject.setAsyncFunction("delay") {
                    val ms = when (it[0]) {
                        is Int -> it[0] as Int
                        is String -> (it[0] as String).toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid argument type for delay: ${it[0]::class.simpleName} expected Int")

                        else -> throw IllegalArgumentException("Invalid argument type for delay: ${it[0]::class.simpleName} expected Int")
                    }
                    delay(ms.toLong())
                }

                globalObject.setFunction("broadcast") {
                    broadcast(it[0].stringify(), it[1] as JSArray)
                }

                globalObject.setFunction("readFile") {
                    readFile(it[0].stringify())
                }
            }
        }
    }

    protected abstract val console: QuickJSContext.Console

    protected abstract fun saveData(data: JSObject)

    protected abstract fun broadcast(message: String, players: JSArray)
    protected abstract fun send(message: String, player: RoomPlayer)

    protected abstract fun readFile(fileName: String): String

    protected abstract suspend fun promptInteger(promptMessage: String, player: RoomPlayer): Int
    protected abstract suspend fun promptBoolean(promptMessage: String, player: RoomPlayer): Boolean
    protected abstract suspend fun promptString(promptMessage: String, player: RoomPlayer): String
    protected abstract suspend fun invitePlayer(promptMessage: String, player: RoomPlayer): RoomPlayer

    suspend fun run(script: String, filename: String): Either<RunError, Unit> = either {
        resetQuickJs()
        setupModuleLoader(script, filename)

        checkAll(script, filename).bind()

        try {
            withContext(quickJsScope.coroutineContext) {
                quickJs.evaluateModule(
                    """
                    import { setup, main } from "$filename";
                    
                    const owner = Object.freeze(getOwner());
                    
                    const data = setup({
                        owner:  owner, 
                        params: Object.freeze(${game.parameters})
                    });
                    
                    saveData(data);
                    
                    const dataProxy = new Proxy(data, {
                        set: function (target, key, value) {
                            target[key] = value;
                            console.log(JSON.stringify(target));
                            saveData(target);
                            return true;
                        },
                        deleteProperty: function (target, key) {
                            delete target[key];
                            console.log(JSON.stringify(target));
                            saveData(target);
                            return true;
                        }
                    });
                    
                    main({
                        data: dataProxy,
                        owner: owner
                    }).then(notifyGameFinished).catch(notifyGameCrashed);
                    """.trimIndent(),
                    "main.js"
                )
            }
        } catch (e: QuickJSException) {
            raise(RunError.RuntimeError(e.message!!))
        } catch (e: Exception) {
            e.printStackTrace()
            raise(RunError.RuntimeError(e.message!!))
        }

        println("Waiting for game end notification...")

        gameEndNotificationChannel.receive().fold(
            { error -> raise(RunError.RuntimeError(error)) },
            { Unit.right() }
        )
    }

    private fun serializePlayer(player: RoomPlayer): JSObject {
        val jsPlayer = quickJs.createNewJSObject()

        jsPlayer.setProperty("id", player.id)
        jsPlayer.setProperty("name", player.name)
        jsPlayer.setProperty("phone", player.phoneNumber.number)

        jsPlayer.setFunction("send") {
            send(it[0].stringify(), player)
        }

        jsPlayer.setAsyncFunction("promptInteger") {
            promptInteger(it[0].stringify(), player)
        }

        jsPlayer.setAsyncFunction("promptBoolean") {
            promptBoolean(it[0].stringify(), player)
        }

        jsPlayer.setAsyncFunction("promptString") {
            promptString(it[0].stringify(), player)
        }

        jsPlayer.setAsyncFunction("promptPlayer") {
            serializePlayer(invitePlayer(it[0].stringify(), player))
        }

        return jsPlayer
    }

    private inline fun <reified R : Any> JSObject.setFunction(
        name: String,
        crossinline function: (Array<Any>) -> R
    ) {
        // If R is Unit, we return null
        if (typeOf<R>() == typeOf<Unit>()) {
            setProperty(name) {
                function(it)
                return@setProperty null
            }
        } else {
            // Otherwise, we return the result of the function
            setProperty(name) {
                return@setProperty function(it)
            }
        }
    }

    private inline fun <reified R : Any> JSObject.setAsyncFunction(
        name: String,
        crossinline function: suspend (Array<Any>) -> R
    ) {
        // If R is Unit, we return null
        if (typeOf<R>() == typeOf<Unit>()) {
            setProperty(name) {
                val (promise, resolve, reject) = createPromise()

//                backgroundScope.launch {
                quickJsScope.launch {
                    try {
                        function(it)
                        withContext(quickJsScope.coroutineContext) {
                            resolve.call(null)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in Unit async function $name: ${e.message}")
                        withContext(quickJsScope.coroutineContext) {
                            reject.call(e.message)
                        }
                    }
                }

                return@setProperty promise
            }
        } else {
            // Otherwise, we return the result of the function
            setProperty(name) {
                val (promise, resolve, reject) = createPromise()

//                backgroundScope.launch {
                quickJsScope.launch {
                    try {
                        val result = function(it)
                        withContext(quickJsScope.coroutineContext) {
                            resolve.call(result)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in async function $name: ${e.message}")
                        withContext(quickJsScope.coroutineContext) {
                            reject.call(e.message)
                        }
                    }
                }

                return@setProperty promise
            }
        }
    }

    private fun createPromise(): JsPromise {
        val call = promiseConstructor.call() as JSObject
        return JsPromise(
            call.getJSObject(JsPromise.PROMISE),
            call.getJSFunction(JsPromise.RESOLVE),
            call.getJSFunction(JsPromise.REJECT)
        )
    }

    protected suspend fun setupModuleLoader(script: String, filename: String) {
        withContext(quickJsScope.coroutineContext) {
            quickJs.moduleLoader = object : QuickJSContext.DefaultModuleLoader() {
                override fun getModuleStringCode(moduleName: String?) = when (moduleName) {
                    filename -> script
                    else -> null
                }
            }
        }
    }

    /**
     * Check the syntax of the script.
     * Raises [RunError.ScriptParsingError] if there is a syntax error.
     * Or [RunError.RuntimeError] if there is a runtime error.
     * Returns [Unit] if the script is valid.
     */
    open suspend fun checkAll(script: String, filename: String) = either {
        withContext(quickJsScope.coroutineContext) {
            checkScriptSyntax(script, filename)
            ensure(isFunctionDefined("setup", filename)) { RunError.SetupFunctionNotDefined }
            ensure(isFunctionDefined("main", filename)) { RunError.MainFunctionNotDefined }
        }
    }

    /**
     * Check if the function is defined in the script.
     * Raises [RunError.SetupFunctionNotDefined] or [RunError.MainFunctionNotDefined] if the function is not defined.
     */
    private fun isFunctionDefined(function: String, filename: String): Boolean {
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

            return true
        } catch (e: QuickJSException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Check the syntax of the script.
     * Raises [RunError.ScriptParsingError] if there is a syntax error.
     * Or [RunError.RuntimeError] if there is a runtime error.
     * Returns [Unit] if the script is valid.
     */
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

    private fun Any.stringify(): String {
        return when (this) {
            is JSObject -> this.stringify()
            else -> this.toString()
        }
    }
}

class CheckerJsExecutor() : IJsExecutor(
    RoomGame(0L, 0L, 0L, JSONObject()),
    RoomPlayer(0L, "", PhoneNumber("")),
) {
    override val console: QuickJSContext.Console = object : QuickJSContext.Console {
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
    }

    override suspend fun checkAll(script: String, filename: String): Either<RunError, Unit> {
        resetQuickJs()
        setupModuleLoader(script, filename)
        return super.checkAll(script, filename)
    }

    override fun saveData(data: JSObject) = Unit

    override fun broadcast(message: String, players: JSArray) = Unit
    override fun send(message: String, player: RoomPlayer) = Unit

    override fun readFile(fileName: String): String = "Hello, world!"

    override suspend fun promptInteger(
        promptMessage: String,
        player: RoomPlayer
    ): Int = 42

    override suspend fun promptBoolean(
        promptMessage: String,
        player: RoomPlayer
    ): Boolean = true

    override suspend fun promptString(
        promptMessage: String,
        player: RoomPlayer
    ): String = "Hello, world!"

    override suspend fun invitePlayer(
        promptMessage: String,
        player: RoomPlayer
    ): RoomPlayer = RoomPlayer(0L, "", PhoneNumber(""))
}

class TestJsExecutor(
    game: RoomGame,
    owner: RoomPlayer,
) : IJsExecutor(game, owner) {
    override val console: QuickJSContext.Console = object : QuickJSContext.Console {
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
    }

    val data: MutableStateFlow<JSONObject> = MutableStateFlow(JSONObject())

    override fun saveData(data: JSObject) {
        this.data.value = JSONObject(data.stringify())
    }

    override fun broadcast(message: String, players: JSArray) {
        TODO("Not yet implemented")
    }

    override fun send(message: String, player: RoomPlayer) {
        TODO("Not yet implemented")
    }

    override fun readFile(fileName: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun promptInteger(promptMessage: String, player: RoomPlayer): Int {
        delay(3000) // Simulate a delay for the prompt
        return 42
    }

    override suspend fun promptBoolean(promptMessage: String, player: RoomPlayer): Boolean {
        delay(2000) // Simulate a delay for the prompt
        return true
    }

    override suspend fun promptString(promptMessage: String, player: RoomPlayer): String {
        delay(2000) // Simulate a delay for the prompt
        return "Hello, world!"
    }

    override suspend fun invitePlayer(promptMessage: String, player: RoomPlayer): RoomPlayer {
        TODO("Not yet implemented")
    }
}

class JsExecutor(
    game: RoomGame,
    owner: RoomPlayer,
    private val gameDao: GameDao,
    private val smsSender: SmsSender,
    private val fileManager: FileManager,
    private val promptWaiter: GameInstance.PromptWaiter
) : IJsExecutor(game, owner) {
    override val console: QuickJSContext.Console = object : QuickJSContext.Console {
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
    }


    override fun saveData(data: JSObject) {
        gameDao.update(game.copy(data = JSONObject(data.stringify())))
    }

    override fun broadcast(message: String, players: JSArray) {
        for (i in 0 until players.length()) {
            val player = players[i] as JSObject
            smsSender.sendSMS(SMSMessage(PhoneNumber(player.getString("phone")), message))
        }
    }

    override fun send(message: String, player: RoomPlayer) {
        println("Sending message to ${player.name}: $message")
        smsSender.sendSMS(SMSMessage(player.phoneNumber, message))
    }

    override fun readFile(fileName: String): String {
        println("Reading file: $fileName")
        return fileManager.readFile(fileName)
    }

    override suspend fun promptInteger(promptMessage: String, player: RoomPlayer): Int {
        smsSender.sendSMS(SMSMessage(player.phoneNumber, promptMessage))

        return promptWaiter.waitForPrompt<Int>(player) {
            val number = it.toIntOrNull()
            ensure(number != null) { GameInstance.PromptWaiter.ErrorMessage.RepeatDefault(promptMessage) }
            number
        }
    }

    override suspend fun promptBoolean(promptMessage: String, player: RoomPlayer): Boolean {
        TODO()
    }

    override suspend fun promptString(promptMessage: String, player: RoomPlayer): String {
        smsSender.sendSMS(SMSMessage(player.phoneNumber, promptMessage))

        return promptWaiter.waitForPrompt<String>(player) {
            ensure(it.isNotBlank()) { GameInstance.PromptWaiter.ErrorMessage.RepeatDefault(promptMessage) }
            it
        }
    }

    override suspend fun invitePlayer(promptMessage: String, player: RoomPlayer): RoomPlayer {
        TODO("Not yet implemented")
    }
}