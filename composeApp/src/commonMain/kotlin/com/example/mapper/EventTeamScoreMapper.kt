package com.example.mapper

import com.example.model.EventTeamScoreDTO
import com.example.model.EventTeamScoreItem
import com.example.model.SetScoreDTO
import com.example.model.SetScoreItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList


fun ArrayList<SetScoreDTO>.toSetScoreItem(): ImmutableList<SetScoreItem> {
    return map {item ->
        SetScoreItem(
            item.score,
            item.number,
            item.tieBreakScore
        )
    }.toImmutableList()
}

fun EventTeamScoreDTO.toEventTeamScoreItem(): EventTeamScoreItem {
    return EventTeamScoreItem(
        regularScore,
        currentScore,
        quarterScores?.toSetScoreItem(),
        extraTimeScore,
        penaltiesScore,
        corner,
        halfScore,
        yellowCard,
        redCard,
        gameScore,servingPlayer,
        setScores?.toSetScoreItem(),
        halfScore
    )
}