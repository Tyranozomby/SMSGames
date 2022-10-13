package com.tyrano.smsgames.engine

import android.content.Context
import android.util.Log
import com.tyrano.smsgames.dao.GameDao
import com.tyrano.smsgames.dao.GamemodeDao
import com.tyrano.smsgames.dao.PlayerDao
import com.tyrano.smsgames.entities.GameEntity
import com.tyrano.smsgames.entities.GamemodeEntity
import com.tyrano.smsgames.entities.PlayerEntity
import com.whl.quickjs.wrapper.JSArray
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Thread.sleep
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.random.Random

const val PROMISE_CONSTRUCTOR = "promiseConstructor"
const val OUTSIDE_RESOLVE = "outsideResolve"
const val OUTSIDE_REJECT = "outsideReject"
const val PROMISE = "promise"

const val promiseConstructorFunc = """
                function $PROMISE_CONSTRUCTOR() {
                    let outsideResolve, outsideReject
                    const promise = new Promise((resolve, reject) => {
                        outsideResolve = resolve
                        outsideReject = reject
                    })
                    return {$PROMISE: promise, $OUTSIDE_RESOLVE: outsideResolve, $OUTSIDE_REJECT: outsideReject}
                }
            """

const val TAG = "GameManager"

data class CreatedPromise(
    val promise: JSObject,
    val resolveFunc: JSFunction,
    val rejectFunc: JSFunction
)

class GameManager(
    private val gamemodeDao: GamemodeDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val smsHandler: IncomingSMSHandler,
    private val appContext: Context
) {
    val games: HashMap<Long, GameInstance> = HashMap()

    init {
        val gameManager = this
        runBlocking {
            gameDao.getAll().forEach {
                games[it.id] = GameInstance(
                    it,
                    gamemodeDao.get(it.gamemodeId),
                    gameDao,
                    playerDao,
                    smsHandler,
                    gameManager,
                    appContext
                )
            }
        }
    }

    fun createGame(gamemode: GamemodeEntity, player: PlayerEntity) {
        val game = runBlocking {
            val gameEntity = GameEntity(
                0,
                gamemode.id,
                player.id
            )
            Log.d(TAG, "createGame: $gameEntity")
            val gameId = gameDao.insert(
                gameEntity
            )
            return@runBlocking gameDao.get(gameId)
        }
        Log.d(TAG, "createGame: start")
        games[game.id] = GameInstance(game, gamemode, gameDao, playerDao, smsHandler, this, appContext)
    }

    fun dramaticallyDestroyTheGameAndReduceItToDustWithALotOfVFXAndBigExplosionsLikeMichealBay(gameId: Long) {
        games.remove(gameId)
        runBlocking {
            gameDao.delete(gameId)
        }
    }

    fun handleMessage(player: PlayerEntity, message: String) {
        val game = runBlocking {
            playerDao.getPlayingGame(player.id)
        }
        games[game!!.id]!!.handleMessage(player, message)
    }
}

class GameInstance(
    val game: GameEntity,
    val gamemode: GamemodeEntity,
    val gameDao: GameDao,
    val playerDao: PlayerDao,
    private val smsHandler: IncomingSMSHandler,
    private val gameManager: GameManager,
    private val appContext: Context
) {

    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var context: QuickJSContext

    private lateinit var promiseConstructor: JSFunction

    private lateinit var data: JSObject

    init {
        Log.d(TAG, "Init context")
        executor.submit {
            context = QuickJSContext.create()
            context.evaluate(promiseConstructorFunc)
            promiseConstructor = context.globalObject.getJSFunction(PROMISE_CONSTRUCTOR)
            data = context.createNewJSObject()
        }
    }

    private val lastPlayersMessage: HashMap<Long, ArrayDeque<SMSPrompt<Any>>> = HashMap()

    val invitationMap: HashMap<String, PromptPlayer> = HashMap()

    private var serializedData: JSONObject? = game.data

    private var startTime: Instant? = game.start

    val playersOrder: JSONArray = game.playersOrder

    init {
        executor.submit {
            Log.d(TAG, "Evaluating game code : ${gamemode.name}")
            context.evaluate(gamemode.code, gamemode.name)
            Log.d(TAG, "Filling library")
            fillLibrary()
            Log.d(TAG, "Starting game")
            startGame()
        }
    }


    private fun fillLibrary() {
        val owner = runBlocking {
            val player = playerDao.getById(game.ownerId)
            Log.d(TAG, "fillLibrary: $player - ${game.id}")
            playerDao.setGame(player.id, game.id)
            serializePlayer(player)
        }
        context.globalObject.setProperty("owner", owner)

        val params = context.createNewJSObject()
        for (key in gamemode.settings.keys()) {
            context.setProperty(params, key, if (game.settings.has(key)) game.settings[key] else gamemode.settings[key])
        }
        context.globalObject.setProperty("params", params)

        context.globalObject.setProperty("delay") {
            val ms = it[0] as Int
            val (promise, resolveFunc, _) = createPromise()

            executor.submit {
                sleep(ms.toLong())
                resolveFunc.call()
            }

            return@setProperty promise
        }

        context.globalObject.setProperty("log") {
            Log.d(TAG, "[${game.id} - ${gamemode.name} (${gamemode.id})] - ${it[0]}")
            return@setProperty true
        }

        context.globalObject.setProperty("setData") {
            var recovering = false

            if (it.size == 1) {
                val newData = it[0] as? JSObject
                if (newData != null)
                    recovering = setData(newData)
                else
                    context.throwJSException("setData doit contenir un objet")
            } else
                context.throwJSException("setData doit contenir un objet")

            return@setProperty recovering
        }

        context.globalObject.setProperty("getRandomWord") {
            val text = appContext.assets.open("words.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "fillLibrary: $text")
            val jsonArray = JSONArray(text)

            val total = jsonArray.length()
            val randIndex = Random(Instant.now().toEpochMilli()).nextInt(total - 1)
            return@setProperty jsonArray[randIndex]
        }

        context.globalObject.setProperty("saveData") {
            saveData()
            return@setProperty true
        }

        context.globalObject.setProperty("broadcast") {
            try {
                val msg = it[0] as String
                val playerList = it[1] as JSArray
                for (i in 0 until playerList.length()) {
                    val player = playerList[i] as JSObject
                    smsHandler.sendSMS(player.getString("phone"), msg)
                }
            } catch (e: TypeCastException) {
                // Jamais tkt
            }
            return@setProperty true
        }
    }

    fun serializePlayer(player: PlayerEntity): JSObject {
        val jsPlayer = context.createNewJSObject()
        jsPlayer.setProperty("id", player.id.toDouble())
        jsPlayer.setProperty("name", player.name)
        jsPlayer.setProperty("phone", player.phoneNumber)

        jsPlayer.setProperty("send") {
            val message = it[0].toString()
            smsHandler.sendSMS(player.phoneNumber, message)

            return@setProperty true
        }

        jsPlayer.setProperty("promptInteger") {
            val (promise, resolveFunc, _) = createPromise()
            val promptMessage = it[0] as String
            val arrayDeque = lastPlayersMessage.getOrPut(player.id) { ArrayDeque() }
            val promptInteger = PromptInteger(
                resolveFunc,
                promptMessage,
                player.phoneNumber,
                smsHandler,
                executor,
                arrayDeque
            )
            smsHandler.sendSMS(player.phoneNumber, promptMessage)
            arrayDeque.add(promptInteger)
            return@setProperty promise
        }

        jsPlayer.setProperty("promptString") {
            val (promise, resolveFunc, _) = createPromise()
            val promptMessage = it[0] as String
            val arrayDeque = lastPlayersMessage.getOrPut(player.id) { ArrayDeque() }
            val promptInteger = PromptString(
                resolveFunc,
                promptMessage,
                player.phoneNumber,
                smsHandler,
                executor,
                arrayDeque
            )
            smsHandler.sendSMS(player.phoneNumber, promptMessage)
            arrayDeque.add(promptInteger)
            return@setProperty promise
        }

        jsPlayer.setProperty("askForPlayer") {
            Log.d(TAG, "askForPlayer: $playersOrder")
            if (playersOrder.length() > 0) {
                val newPlayer = runBlocking {
                    playerDao.getById((playersOrder.remove(0) as Int).toLong())
                }
                return@setProperty serializePlayer(newPlayer)
            } else {
                val (promise, resolveFunc, _) = createPromise()
                val promptMessage = it[0] as String
                val arrayDeque = lastPlayersMessage.getOrPut(player.id) { ArrayDeque() }
                val promptPlayer = PromptPlayer(
                    resolveFunc,
                    promptMessage,
                    player.phoneNumber,
                    smsHandler,
                    this,
                    player,
                    arrayDeque
                )

                smsHandler.sendSMS(player.phoneNumber, promptMessage)
                arrayDeque.add(promptPlayer)
                return@setProperty promise
            }
        }

        Log.d(TAG, "Player serialized")
        return jsPlayer
    }

    private fun startGame() {
        Log.d(TAG, "startGame: Starting !")
        context.globalObject.setProperty("gameFinished") {
            Log.d(TAG, "Game finished")
            cleanupGame()
            return@setProperty true
        }

        context.globalObject.setProperty("gameError") {
            val error = it[0] as JSObject
            val errorMessage = "${it[0]}\n${error.getProperty("stack")}"
            Log.e(TAG, "[${game.id} - ${gamemode.name} (${gamemode.id})] - $errorMessage")
            broadcast(
                "⚠ Une erreur est survenue durant votre partie car je ne sais pas coder ⚠\n$errorMessage\nN'hésitez pas à venir m'insulter pour me le faire remarquer",
                runBlocking { gameDao.getAllInGame(game.id) })

            cleanupGame()
            return@setProperty false
        }

        context.globalObject.setProperty("data", data)

        context.evaluate("main().then(gameFinished).catch(gameError);")
    }

    private fun broadcast(message: String, players: List<PlayerEntity>) {
        players.forEach { smsHandler.sendSMS(it.phoneNumber, message) }
    }

    private fun setData(newData: JSObject): Boolean {
        var recovering = false

        if (serializedData == null) {
            serializedData = JSONObject(newData.stringify())
        } else {
            recovering = true
        }
        val parsedData = context.parseJSON(serializedData!!.toString())
        Log.d(TAG, "setData: ${parsedData.stringify()}")
        for (i in 0 until parsedData.names.length()) {
            val key = parsedData.names[i] as String
            context.setProperty(data, key, parsedData.getProperty(key))
        }

        if (startTime == null) {
            startTime = Instant.now()
            runBlocking {
                gameDao.start(game.id, startTime!!.toEpochMilli())
            }
        }
        context.globalObject.setProperty("startTime", startTime!!.toEpochMilli().toDouble())

        return recovering
    }

    private fun saveData() {
        serializedData = JSONObject(data.stringify())
        runBlocking {
            gameDao.saveDataOf(game.id, serializedData!!)
        }
    }

    fun cleanupGame() {
        executor.submit {
            context.destroyContext()
        }
        gameManager.dramaticallyDestroyTheGameAndReduceItToDustWithALotOfVFXAndBigExplosionsLikeMichealBay(game.id)
    }

    private fun createPromise(): CreatedPromise {
        val call = promiseConstructor.call() as JSObject
        return CreatedPromise(
            call.getJSObject(PROMISE),
            call.getJSFunction(OUTSIDE_RESOLVE),
            call.getJSFunction(OUTSIDE_REJECT)
        )
    }

    fun handleMessage(player: PlayerEntity, message: String) {
        lastPlayersMessage[player.id]?.firstOrNull()
            ?.let { if (it.onSMS(message)) lastPlayersMessage[player.id]!!.removeFirst() }
    }
}

abstract class SMSPrompt<out T>(
    private val resolveFunc: JSFunction,
    private val to: String,
    private val smsHandler: IncomingSMSHandler,
    private val executor: ExecutorService,
    private val promptList: ArrayDeque<SMSPrompt<Any>>
) {

    protected abstract val promptMessage: String

    protected abstract fun validation(message: String): T?

    open fun onSMS(message: String): Boolean {
        Log.d(TAG, "onSMS: $message")
        val validated = validation(message)
        return if (validated != null) {
            executor.submit {
                resolveFunc.call(validated)
            }
            promptList.remove(this as SMSPrompt<*>)
            true
        } else {
            smsHandler.sendSMS(to, promptMessage)
            false
        }
    }
}

class PromptInteger(
    resolveFunc: JSFunction,
    override val promptMessage: String,
    to: String,
    smsHandler: IncomingSMSHandler,
    executor: ExecutorService,
    promptList: ArrayDeque<SMSPrompt<Any>>
) : SMSPrompt<Int>(resolveFunc, to, smsHandler, executor, promptList) {

    override fun validation(message: String): Int? {
        return message.toIntOrNull()
    }
}

class PromptString(
    resolveFunc: JSFunction,
    override val promptMessage: String,
    to: String,
    smsHandler: IncomingSMSHandler,
    executor: ExecutorService,
    promptList: ArrayDeque<SMSPrompt<Any>>
) : SMSPrompt<String>(resolveFunc, to, smsHandler, executor, promptList) {

    override fun validation(message: String): String {
        return message
    }
}

class PromptPlayer(
    private val resolveFunc: JSFunction,
    override val promptMessage: String,
    private val to: String,
    private val smsHandler: IncomingSMSHandler,
    private val gameInstance: GameInstance,
    private val owner: PlayerEntity,
    private val promptList: ArrayDeque<SMSPrompt<Any>>
) : SMSPrompt<JSObject>(resolveFunc, to, smsHandler, gameInstance.executor, promptList) {

    private var player: PlayerEntity? = null

    private var timeout: Thread? = null

    override fun validation(message: String): JSObject? {
        Log.d(TAG, "validation: $message")
        val number = (if (message.startsWith("0")) "+33" + message.substring(1) else message)
            .replace(" ", "")

        if (player == null) {
            player = runBlocking { gameInstance.playerDao.getByNumber(number) }
            if (player != null) {
                if (player!!.phoneNumber == to)
                    smsHandler.sendSMS(to, "Vous ne pouvez pas vous inviter")
                else if (player!!.gameId != null)
                    smsHandler.sendSMS(
                        to,
                        "Cette personne est déjà dans une partie.\nIl faut qu'elle la finisse pour que vous puissiez l'inviter"
                    )
                else {
                    smsHandler.sendSMS(
                        player!!.phoneNumber,
                        "Vous avez été invité·e par ${owner.name} (${owner.phoneNumber}) pour jouer au ${gameInstance.gamemode.name}.\n\nFais !join ${gameInstance.game.id} pour rejoindre sa partie ou !reject ${gameInstance.game.id} pour la refuser immédiatement"
                    )
                    smsHandler.sendSMS(
                        to,
                        "${player!!.name} a bien été invité·e.\nCette personne a 5 minutes pour accepter l'invitation"
                    )
                    timeout = thread {
                        try {
                            sleep(5 * 60 * 1000)
                            this.reset()
                        } catch (_: InterruptedException) {
                        }
                    }

                    gameInstance.invitationMap[player!!.phoneNumber] = this
                }
            } else {
                smsHandler.sendSMS(
                    to,
                    "Cette personne n'est pas inscrite ou vous avez mal entré son numéro.\nEssayez encore"
                )
            }
        }

        return null
    }

    fun accept() {
        timeout!!.interrupt()

        runBlocking {
            gameInstance.playerDao.setGame(player!!.id, gameInstance.game.id)
            gameInstance.gameDao.savePlayersOrderOf(gameInstance.game.id, gameInstance.playersOrder.put(player!!.id))
        }
        smsHandler.sendSMS(player!!.phoneNumber, gameInstance.gamemode.rules)

        gameInstance.executor.submit {
            val serializedPlayer = gameInstance.serializePlayer(player!!)
            resolveFunc.call(serializedPlayer)
        }
        promptList.removeLast()
        gameInstance.invitationMap.remove(player!!.phoneNumber)
    }

    fun reset(reject: Boolean = false) {
        if (reject) {
            smsHandler.sendSMS(
                player!!.phoneNumber,
                "L'invitation de ${owner.name} pour ${gameInstance.gamemode.name} a été rejeté"
            )
            smsHandler.sendSMS(to, "${player!!.name} a rejeté votre invitation.\nVeuillez entrer un nouveau numéro")
        } else {
            smsHandler.sendSMS(
                player!!.phoneNumber,
                "Votre invitation pour ${gameInstance.gamemode.name} de ${owner.name} a expiré"
            )
            smsHandler.sendSMS(to, "L'invitation de ${player!!.name} a expiré.\nVeuillez entrer un nouveau numéro")
        }
        player = null
    }

    override fun onSMS(message: String): Boolean {
        Log.d(TAG, "onSMS: $message")
        validation(message)
        return false
    }
}