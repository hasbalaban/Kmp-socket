package com.example.myapplication.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(modules)
    }
}