package com.example.model

import kotlinx.serialization.Serializable


@Serializable
data class MarketByIdRequestBody(
    val eventId: Int,
    val marketId: Int,
    val outComeNo : Int
)
