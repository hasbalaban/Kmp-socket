package com.example.model

import kotlinx.serialization.Serializable


@Serializable
data class SportsBookUpdateInfo(
    val updateType:Int,
    var events: ArrayList<Int> = arrayListOf()
)
