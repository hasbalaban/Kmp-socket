package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewmodel : ViewModel() {
    protected val viewModelScopee: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    override fun onCleared() {
        super.onCleared()
        viewModelScopee.cancel()
    }
}