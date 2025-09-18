package com.example.mapper

import com.example.model.MarketDTO
import com.example.model.MarketItem
import com.example.model.OutComesDTO
import com.example.model.OutComesItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList


fun ArrayList<OutComesDTO>.toOutcomeItems(): ImmutableList<OutComesItem> {
    return this.map {outcome ->
        OutComesItem(
            outcome.outcomeNo,
            outcome.odd,
            outcome.previousOdd,
            outcome.webOdd,
            outcome.name
        )
    }.toImmutableList()
}

fun MarketDTO.toMarketItem(): MarketItem {
    val outcomes = outComes?.toOutcomeItems()
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

fun ArrayList<MarketDTO>.toMarketItems(): ImmutableList<MarketItem> {
    return this.map {marketDto ->
        marketDto.toMarketItem()
    }.toImmutableList()
}


