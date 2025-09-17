package com.example.mapper

import com.example.model.MarketDTO
import com.example.model.MarketItem
import com.example.model.OutComesDTO
import com.example.model.OutComesItem


fun ArrayList<OutComesDTO>.toOutcomeItems(): List<OutComesItem> {
    return map {outcome ->
        OutComesItem(
            outcome.outcomeNo,
            outcome.odd,
            outcome.previousOdd,
            outcome.webOdd,
            outcome.name
        )
    }
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

fun ArrayList<MarketDTO>.toMarketItems(): List<MarketItem> {
    return this.map {marketDto ->
        marketDto.toMarketItem()
    }
}


