package com.example.mapper

import com.example.model.EventTeamScoreDTO
import com.example.model.EventTeamScoreItem

fun EventTeamScoreDTO.toEventTeamScoreItem(): EventTeamScoreItem {
    return EventTeamScoreItem(
        regularScore,
        currentScore,
        quarterScores,
        extraTimeScore,
        penaltiesScore,
        corner,
        halfScore,
        yellowCard,
        redCard,
        gameScore,servingPlayer,
        setScores,
        halfScore
    )
}