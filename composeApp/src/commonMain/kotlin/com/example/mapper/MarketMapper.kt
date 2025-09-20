package com.example.mapper

import com.example.model.MarketDTO
import com.example.model.MarketItem
import com.example.model.OutComesDTO
import com.example.model.OutComesItem
import com.example.myapplication.manager.CouponManagerV2
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList


fun ArrayList<OutComesDTO>.toOutcomeItems(eventId: Int, marketId: Long): ImmutableList<OutComesItem> {
    return this.map {outcome ->
        OutComesItem(
            outcome.outcomeNo,
            outcome.odd,
            outcome.previousOdd,
            outcome.webOdd,
            outcome.name,
            isSelected = CouponManagerV2.isInCoupon(eventId, marketId, outcome.outcomeNo)
        )
    }.toImmutableList()
}

fun MarketDTO.toMarketItem(eventId: Int): MarketItem {
    val outcomes = outComes?.toOutcomeItems(eventId, marketId)
    return MarketItem(
        marketId,
        type,
        subtype,
        version,
        status,
        mbc,
        outcomes,
        specialOddValue
    )
}

fun ArrayList<MarketDTO>.toMarketItems(eventId: Int): ImmutableList<MarketItem> {
    return this.map {marketDto ->
        marketDto.toMarketItem(eventId)
    }.toImmutableList()
}


