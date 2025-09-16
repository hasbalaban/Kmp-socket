package com.example.model


import com.example.myapplication.manager.MarketConfig
import com.mgmbk.iddaa.manager.EventStoreManager
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class CouponItem @OptIn(ExperimentalTime::class) constructor(
    var eventId: Int,
    var marketId: Int,
    var outcomeNo: Int,
    var oddPrev: Double,
    var sportId: Int,
    var bettingPhase: Int,

    var eventName: String,
    var marketName: String,
    var eventDate: Long,

    var isBank: Boolean = false,
    var editDate: Long = Clock.System.now().toEpochMilliseconds(),
    var isRemoved: Boolean = false,
    var itemData : CouponItemData? = null
) : DisplayItem(2) {

    init {
        itemData = CouponItemData(getCouponItemData())
    }

    fun getCouponItemData(): Triple<Events?, Market?, OutComes?> {
        val event = EventStoreManager.findEvent(eventId, sportId, bettingPhase)
        val market = event?.markets?.firstOrNull { it.marketId.toInt() == marketId }
        val outcome = market?.outComes?.firstOrNull { it.outcomeNo == outcomeNo }

        return Triple(event, market, outcome)
    }

    // TODO burayı sil, companion objectte de aynısı var. Zaman kıtlığı buradaki çirkin koda sebebiyet verdi :(:(
    fun getMarketName(withOutcome: Boolean = true): String {
        val marketLookup = MarketConfig.getMarketLookup(itemData?.couponItemData?.second?.key() ?: "")
        val marketNameStr = marketLookup?.getName(itemData?.couponItemData?.second?.specialOddValue ?: "") ?: "-"
        val outcomeStr = marketLookup?.getOutcomeName(
            outcomeNo = itemData?.couponItemData?.third?.outcomeNo ?: -1,
            sov = itemData?.couponItemData?.second?.specialOddValue
        ) ?: itemData?.couponItemData?.third?.name

        return if(withOutcome) "$marketNameStr: $outcomeStr" else "$marketNameStr"
    }

    fun isBettingPhaseLive(): Boolean {
        return bettingPhase == 1
    }

    fun isClosed(): Boolean {
        return (
                (itemData?.couponItemData?.first?.bettingPhase == BettingPhase.LIVE_EVENT.value &&
                itemData?.couponItemData?.first?.status != MarketStatus.Open.value)
                || itemData?.couponItemData?.second?.status != MarketStatus.Open.value
                )
    }

    companion object {
        fun getMarketName(market: Market, outComes: OutComes, withOutcome: Boolean = true): String {
            val marketLookup = MarketConfig.getMarketLookup(market.key() ?: "")
            val marketNameStr = marketLookup?.getName(market.specialOddValue ?: "") ?: "-"
            val outcomeStr = marketLookup?.getOutcomeName(outComes.outcomeNo, market.specialOddValue) ?: outComes.name

            return if(withOutcome) "$marketNameStr: $outcomeStr" else "$marketNameStr"
        }

        fun getCompetitionName(competitionId: Int): String {
            return MarketConfig.getCompetition(competitionId).competitionName.ignoreNull()
        }
    }
}

@Serializable
data class CouponItemData(
    val couponItemData : Triple<Events?, Market?, OutComes?>?
)