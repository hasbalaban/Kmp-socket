package com.example.model

import kotlinx.serialization.Serializable


@Serializable
data class LeagueSelectionFilterItem(
    var isChecked: Boolean = false,

    val competitionId : Int,
    var competitionIcon : String? = null,
    val competitionPriority : Int,

    val leagueName : String,
    val size : Int,

    var isFavoriteLeague : Boolean = false,
    val isBottomSheet : Boolean = false,
): DisplayItem(1)
