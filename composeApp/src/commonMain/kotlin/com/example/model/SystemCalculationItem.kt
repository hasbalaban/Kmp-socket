package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SystemCalculationItem(
    var systemTotalOdd: Double = 0.0,
    var systemTotalWodd: Double = 0.0,
    var systemTotalColumn: Long = 0L,
)