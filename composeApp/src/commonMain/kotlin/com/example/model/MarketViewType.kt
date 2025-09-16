package com.example.model

enum class MarketViewType(val value: Int) {
    OUTCOME_TWO(value = 1),
    OUTCOME_THREE(value = 2),
    OUTCOME_FOUR(value = 3),
    OUTCOME_TWO_HANDICAP(value = 4),
    OUTCOME_THREE_HANDICAP(value = 5),
    OUTCOME_SIX(value = 6),
    OUTCOME_ONE(value = 7),
    TWO_OUTCOME_DOUBLE_LINE(value = 8),
    THREE_OUTCOME_DOUBLE_LINE(value = 9);

    companion object {
        fun from(findValue: Int): MarketViewType =
            values().firstOrNull { it.value == findValue } ?: OUTCOME_THREE
    }
}