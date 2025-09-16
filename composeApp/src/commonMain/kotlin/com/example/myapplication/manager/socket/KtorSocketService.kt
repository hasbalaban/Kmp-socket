package com.example.myapplication.manager.socket

import com.example.model.EventScore
import com.example.model.SocketEvent
import com.mgmbk.iddaa.manager.EventStoreManager
import eu.lepicekmichal.signalrkore.AutomaticReconnect
import eu.lepicekmichal.signalrkore.HubConnection
import eu.lepicekmichal.signalrkore.HubConnectionBuilder
import eu.lepicekmichal.signalrkore.TransportEnum
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration.Companion.seconds

private val mainJsonParser = Json {
    isLenient = true;
    ignoreUnknownKeys = true
    explicitNulls = false
}


class SportsBookSocketManager() {
    var job : Job? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Socket Urls
    private val liteSocketUrl: String = "https://sportsbooksocketV2.iddaa.com/sportsBookHubLite"
    private val liveSocketUrl =  "https://sportsbooksocketV2.iddaa.com/sportsBookHub"

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }



    private fun createKtorClient(): HttpClient {
        return HttpClient {
            install(WebSockets)
            install(SSE)
            install(HttpTimeout)
            install(ContentNegotiation) { json(json = jsonConfig) }
        }
    }


    private val liteConnection : HubConnection by lazy {
        HubConnectionBuilder.create(liteSocketUrl){
            skipNegotiate = true
            transportEnum = TransportEnum.WebSockets
            automaticReconnect = AutomaticReconnect.Active
            handshakeResponseTimeout= 40.seconds
            httpClient = createKtorClient()
            json = jsonConfig
        }
    }

    private val liveConnection : HubConnection by lazy {
        HubConnectionBuilder.create(liveSocketUrl){
            skipNegotiate = true
            transportEnum = TransportEnum.WebSockets
            automaticReconnect = AutomaticReconnect.Active
            handshakeResponseTimeout= 40.seconds
            httpClient = createKtorClient()
            json = jsonConfig
        }
    }




    private val eventStore by lazy { EventStoreManager() }

    private val _eventsFlow = MutableSharedFlow<SocketEvent>()
    val eventsFlow: SharedFlow<SocketEvent> = _eventsFlow.asSharedFlow()

    private val _scoresFlow = MutableSharedFlow<EventScore>()
    val scoresFlow: SharedFlow<EventScore> = _scoresFlow.asSharedFlow()


    fun startConnections() {
        scope.launch {
            try {
                liteConnection.start()
                liveConnection.start()
            } catch (e: Exception) {
                println("Bağlantı başlangıç hatası: ${e.message}")
            }

            launch {
                listenToConnectionStates()
            }

            launch { listenToCurrentScore() }
            launch { listenToCurrentEvents() }
            launch { listenToCurrentOdds() }
        }
    }

    private fun listenToConnectionStates() {
        scope.launch {
            liteConnection.connectionState.collect { state ->
                println("Lite Connection State: $state")
            }
        }
        scope.launch {
            liveConnection.connectionState.collect { state ->
                println("Live Connection State: $state")
            }
        }
    }

    private suspend fun listenToCurrentScore() {
        try {
            liveConnection.on("currentScore", JsonObject.serializer()).collect { strScore ->
                val score = mainJsonParser.decodeFromString<EventScore?>(strScore.arg1.toString())
                score?.let {
                    eventStore.updateScoreFromSocket(it)
                }
                println(strScore.component1().toString())


            }
        } catch (e: Exception) {
            println("currentScore dinlerken hata: ${e.message}")
        }
    }

    private suspend fun listenToCurrentEvents() {
        try {
            liteConnection.on("currentEvents", JsonObject.serializer()).collect { strEvent ->
                val score = mainJsonParser.decodeFromString<SocketEvent?>(strEvent.arg1.toString())
                score?.let {
                    eventStore.updateEventFromSocket(it)
                }
                println(strEvent.component1().toString())
            }
        } catch (e: Exception) {
            println("currentEvents dinlerken hata: ${e.message}")
        }
    }

    private suspend fun listenToCurrentOdds() {
        try {
            liteConnection.on("currentOdds", JsonObject.serializer()).collect { strEvent ->
                val score = mainJsonParser.decodeFromString<SocketEvent?>(strEvent.arg1.toString())
                score?.let {
                    eventStore.updateEventFromSocket(it)
                }
                println(strEvent.component1().toString())
            }

        } catch (e: Exception) {
            println("currentOdds dinlerken hata: ${e.message}")
        }
    }


    fun keepSocketAlive(){
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (shouldKeepSocketAlive){
                delay(45_000)

                if (shouldKeepSocketAlive){
                    liteConnection.stop()
                }
            }
        }
    }


    companion object {
        var shouldKeepSocketAlive = true
        // HubConnections
    }

}