package com.example.myapplication.manager

import kotlinx.serialization.Serializable

@Serializable
data class CouponCalculation(
    val totalOdd: Double,
    val totalWodd: Double,
    val totalColumns: Long,
    val couponCount: Int,
    val totalEvents: Int,
    val multiplier: Int,
    val maxWinning: Double,
    val maxWinningWodd: Double,
    val kingWinning: Double,
    val mbsDiff: Int,
) {
    fun isCouponValid(): Boolean {
        if(CouponManagerV2.hasBankEvent() && CouponManagerV2.selectedSystems.isEmpty())
            return false
        if(mbsDiff > 0 && couponCount > 0)
            return false
        if(multiplier * totalColumns < 50)
            return false
        if(couponCount < 1)
            return false
        /*if(getTotalAmount() > RemoteConfigs.maxCouponAmount)
            return false
        if(totalEvents > RemoteConfigs.maxCouponEventsCount)
            return false
        if(CouponManagerV2.selectedSystems.isEmpty() && CouponManagerV2.hasBankEvent())
            return false*/
        return true
    }

    fun getTotalSuccessAmountPerItem(successCount: Int = 0): Long {
        return  (getTotalAmount()/totalEvents) * (successCount)
    }
    fun getTotalAmount(): Long {
        return totalColumns * multiplier
    }
}
