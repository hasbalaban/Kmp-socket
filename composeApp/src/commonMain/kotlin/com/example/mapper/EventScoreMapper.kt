package com.example.mapper

import com.example.model.EventScoreDTO
import com.example.model.EventScoreItem
import com.example.model.EventTeamScoreItem

fun EventScoreDTO.toEventScoreItem(): EventScoreItem {
    val homeTeam: EventTeamScoreItem? = homeTeam?.toEventTeamScoreItem()
    val awayTeam: EventTeamScoreItem? = awayTeam?.toEventTeamScoreItem()
    return EventScoreItem(
        eventId,
        id,
        updateDate,
        status,
        homeTeam,
        awayTeam,
        minute,
        second
    )
}