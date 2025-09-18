package com.example.mapper

import com.example.model.EventItem
import com.example.model.EventScoreItem
import com.example.model.EventsDTO
import com.example.model.MarketItem
import com.example.myapplication.manager.value
import kotlinx.collections.immutable.ImmutableList

fun EventsDTO.toEventItem(isSelected : Boolean): EventItem {
    val markets : ImmutableList<MarketItem>? = markets?.toMarketItems()
    val score : EventScoreItem? = score?.toEventScoreItem()
    val sliderMarkets : ImmutableList<MarketItem>? = sliderMarkets?.toMarketItems()
    return EventItem(
        // Her bir property'yi sırasıyla kopyala
        eventId,
        mappedId,
        betRadarId,
        cref,
        eventVersion,
        eventName,
        sportId,
        eventDate,
        status,
        bettingPhase,
        isLive,
        minimumBetCount,
        kingOdds,
        kingMbc,
        isKingLive,
        hasDuel,
        hasRapid,
        markets,
        competitionId,
        realHomeEventId,
        realAwayEventId,
        score,
        oddCount,
        homeTeamName,
        awayTeamName,
        hasComments,
        hasStream,
        sliderMarkets,
        kingCount = kingMbc.value() + kingOdds.value() + isKingLive.value(),
        isSelected = isSelected
    )
}
