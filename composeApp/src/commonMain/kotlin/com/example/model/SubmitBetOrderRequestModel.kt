package com.example.model

import com.example.myapplication.moneyformatter.Decimal
import com.example.myapplication.moneyformatter.DecimalAsStringSerializer
import kotlinx.serialization.Serializable

@Serializable
data class SubmitBetOrderRequestModel(
    val betSlip: SubmitBetOrder?,
    val isConfirmed: Boolean?,
    val useBonus : Boolean?
)

@Serializable
data class SubmitBetOrder(
    val bet: BetSlip?,
    val betSlipDetail: BetSlipDetail?
)

@Serializable
data class BetSlip(
    val betItems: ArrayList<BetItems>?,
    var selectedSystems: List<Int>?,
    val multiplier: String? = 3.toString()
)

@Serializable
data class BetSlipDetail(
    val isPlayable: Boolean?,
    val stake: String?,
    val maxOdd: String?,
    val maxWinning: String?,
    val maxWodd:String?,
    val maxWoddWinning: String,
    val maxKingWinning : String?,
    var systems: List<Int>? = listOf(),
    val numberOfCoupons: Int,
    val message: String?,
    val currency: String? = "TL"
)

@Serializable
data class BetItems(
    val pt: Int?,
    val ono: Int?,
    val ona: String?,
    val eid: Int?,
    val ev: Int?,
    var en: String?,
    val cn: String?,
    val sid: Int?,
    val ed: Long?,
    @Serializable(with = DecimalAsStringSerializer::class)
    val odd: Decimal?, // fixedOdds (king bet)
    @Serializable(with = DecimalAsStringSerializer::class)
    val wodd: Decimal?, // fixedOddsWeb
    var mbs: Int?,
    val mid: Int?,
    val mv: Int?,
    val mn: String?,
    var b: Boolean?,
    var mno: String?,
    var sodd: String?,
    var sono: String?,
    var live: Boolean?,
    var m: Unit?,
    var cs: String?,
    var oodd: String?,
    var ou: String?,
    var sona: String?,
    var iskbet: Boolean?,
    var kbodd: Boolean?,
    var kblive: Boolean?,
    var kbmbs: Boolean?,
    var skbet: Int?,
    var edh: String? = null,
    var ede: String? = null,
    var bid: Int?
) {
    override fun equals(other: Any?): Boolean {
        return this.eid == (other as BetItems).eid
    }
}

@Serializable
data class BetSlipResponse(
    val isSuccess: Boolean,
    val message: String?,
    val data: BetSlipData?,
    val error: ErrorResponse?,
    val info: InfoResponse?,
    val date: String? = null
)

@Serializable
data class BetSlipData(
    val bet: BetSlip?,
    val betSlipDetail: BetSlipDetail?
)


@Serializable
data class InfoResponse(
    var code: Int,
    var name: String,
    var message: String
)

@Serializable
data class ErrorResponse(var code: Int?, var name: String?, var message: String?)
