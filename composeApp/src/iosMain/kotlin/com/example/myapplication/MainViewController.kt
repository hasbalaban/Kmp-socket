package com.example.myapplication

import androidx.compose.ui.window.ComposeUIViewController
import com.example.myapplication.di.initKoin
import com.example.myapplication.manager.socket.SportsBookSocketManager

fun MainViewController() = ComposeUIViewController(
    configure = {
        SportsBookSocketManager().startConnections()
        initKoin()
    }
){ App() }