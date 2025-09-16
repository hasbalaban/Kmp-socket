package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SocketEvent(
    @SerialName("eid")
    val eventId: Int,
    @SerialName("ev")
    val eventVersion: Long?,
    @SerialName("a")
    val action: String?,
    @SerialName("mb")
    val minimumBetCount: Int?,
    @SerialName("sid")
    val sportId: Int?,
    @SerialName("es")
    val eventStatus: Int?,
    @SerialName("bp")
    val bettingPhase: Int?,
    @SerialName("m")
    val markets: List<SocketEventMarket>?,
    @SerialName("ts")
    val timeStamp: Long?,
    @SerialName("mc")
    val oddCount: Int?
)

@Serializable
data class SocketEventMarket(

    @SerialName("i")
    val id: Long = 0,
    @SerialName("v")
    val version: Long?,
    @SerialName("s")
    val status: Int?,
    @SerialName("m")
    val mbc: Int?,
    @SerialName("k")
    val muk: String?,
    @SerialName("o")
    val outcomes: List<SocketEventOutcome>?,
    @SerialName("sov")
    val speacialOddValue: String?,

    ) {
    fun getOutcomeList(): ArrayList<OutComes> {
        val outcomeList = arrayListOf<OutComes>()
        outcomes?.forEach {
            outcomeList.add(it.toOutcome())
        }
        return outcomeList
    }
}

@Serializable
data class SocketEventOutcome(
    @SerialName("n")
    val no: Int?,
    @SerialName("na")
    val name: String?,
    @SerialName("o")
    val odd: Double?,
    @SerialName("wo")
    val webOdd: Double?,
) {
    fun toOutcome(): OutComes {
        return OutComes(
            outcomeNo = no ?: -1,
            odd = odd,
            previousOdd = null,
            webOdd = webOdd,
            name = name.toString()
        )
    }
}