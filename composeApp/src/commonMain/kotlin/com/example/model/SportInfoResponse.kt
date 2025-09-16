package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SportInfoResponse(
    @SerialName("isSuccess")
    val isSuccess: Boolean?,

    @SerialName("data")
    val data: List<SportInfo>?,

    @SerialName("message")
    val message: String?,

    @SerialName("error")
    val error: String?,

    @SerialName("info")
    val info: String?,

    @SerialName("dateTime")
    val dateTime: String?
)

@Serializable
data class SportInfo(
    @SerialName("i")
    val sportId: Int,

    @SerialName("lc")
    val liveCount: Int?,

    @SerialName("uc")
    val upcomingCount: Int?,

    @SerialName("ec")
    var totalEventCount: Int?,

    @SerialName("oc")
    val outrightEventCount: Int?,

    @SerialName("hr")
    val hasRapidMarkets: Boolean?,

    @SerialName("hk")
    val hasKing: Boolean?,

    @SerialName("hd")
    val hasDuel: Boolean?,
)