package com.example.myapplication.manager.socket

import com.example.model.EventScore
import com.example.model.SocketEvent

interface SignalRService {
    fun connect()
    fun disconnect()
    fun onCurrentEvents(onEvent: (SocketEvent) -> Unit)
    fun onCurrentOdds(onEvent: (SocketEvent) -> Unit)
    fun onCurrentScore(onEvent: (EventScore) -> Unit)
    fun stopListening(methodName: String)
}