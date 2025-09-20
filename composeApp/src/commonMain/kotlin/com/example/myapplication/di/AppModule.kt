package com.example.myapplication.di

import com.example.myapplication.viewmodel.SportsbookViewmodel
import com.example.myapplication.manager.EventStoreManager
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    single { EventStoreManager() }
}

val viewModelModule = module {
    viewModelOf(::SportsbookViewmodel)
}

val modules = listOf(sharedModule, viewModelModule)