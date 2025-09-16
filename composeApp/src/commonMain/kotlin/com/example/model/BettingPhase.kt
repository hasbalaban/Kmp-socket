package com.example.model

enum class BettingPhase(val value: Int) {
    LIVE_EVENT(value = 1),
    PRE_EVENT(value = 0);

    companion object {
        fun from(findValue: Int): BettingPhase =
            values().firstOrNull { it.value == findValue } ?: PRE_EVENT
    }
}