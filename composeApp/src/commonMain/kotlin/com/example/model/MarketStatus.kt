package com.example.model

enum class MarketStatus(val value: Int) {
    Unknown(-10),
    Suspended(-2),
    Paused(-1),
    Closed(0),
    Open(1),
    Resulted_NotConfirmed(2),
    Resulted_Confirmed(3),
    Settled(4);

    companion object {
        fun isViewable(value: Int): Boolean =
            value == Open.value || value == Paused.value || value == Suspended.value
    }
}
