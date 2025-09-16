package com.example.model

enum class ScoreType(val value: String) {
    REGULAR("r"),
    CURRENT("c"),
    HALF_TIME("ht"),
    EXTRA_TIME("et"),
    PENALTIES("pe"),
    QUARTER("qs"),
    SETS("ss"),
    CORNER("co"),
    YELLOW_CARD("yc"),
    RED_CARD("rc"),
    INNING("is"),
    EXTRA_INNING("ei"),
    SHOOTOUT("s"),
    CANCELLED("cc"),
    SET_GAME_SCORE("gs");

    companion object {
        fun from(findValue: String): ScoreType =
            values().firstOrNull { it.value == findValue } ?: REGULAR
    }
}

