package com.example.myapplication

import androidx.compose.ui.window.ComposeUIViewController
import com.example.myapplication.manager.socket.SportsBookSocketManager

fun MainViewController() = ComposeUIViewController(
    configure = {
        SportsBookSocketManager().startConnections()
    }
){ App() }