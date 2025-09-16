package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBetOrderRequestModel(
    @SerialName("alo") var acceptLowers: Boolean,
    @SerialName("aho") var acceptHighers: Boolean,
    @SerialName("ss") val selectedSystems: List<Int>,
    @SerialName("m") val multiplier: Int,
    @SerialName("cc") val couponCount: Int,
    @SerialName("modd") val maxOdd: String,
    @SerialName("ub") val useBonus: Boolean,
    @SerialName("a") val amount: String,
    @SerialName("mw") val maxWinning: String,
    @SerialName("mkw") val maxKingWinning: String,
    @SerialName("e") val events: List<BetslipEvent>,
) 

@Serializable
data class BetslipEvent(
    @SerialName("eid") val eventId: Int,
    @SerialName("ev") val eventVersion: Int,
    @SerialName("mid") val marketId: Int,
    @SerialName("mv") val marketVersion: Int,
    @SerialName("ono") val outComeNo: Int,
    @SerialName("odd") val odd: Double,
    @SerialName("b") val isBanker: Boolean,
    @SerialName("mn") val marketName: String,
    @SerialName("cn") val competitionName: String,
) 