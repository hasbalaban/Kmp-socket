package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CouponSettings(
    var acceptHigher: Boolean,
    var acceptLower: Boolean,
    var bonus: Boolean
)