package com.example.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs


@Serializable
data class MarketConfigResponse(
    @SerialName("isSuccess")
    val isSuccess: Boolean?,

    @SerialName("data")
    val data: MarketResponse?,

    @SerialName("message")
    val message: String?,

    @SerialName("error")
    val error: String?,

    @SerialName("info")
    val info: String?,

    @SerialName("dateTime")
    val dateTime: String?
)

@Serializable
data class MarketResponse(

    @SerialName("m")
    val marketLookup: ImmutableMap<String, MarketLookup>,

    @SerialName("s")
    val sportType: ImmutableMap<String, SportType>?,

    @SerialName("mg")
    val marketGroup: ImmutableMap<String, MarketGroup>?,

    @SerialName("msg")
    val marketSubGroup: ImmutableMap<String, MarketSubGroup>?,

    @SerialName("dmg")
    val detailMarketGroup: ImmutableMap<String, DetailMarketGroup>?,

    @SerialName("dmsg")
    val detailMarketSubGroup: ImmutableMap<String, DetailMarketSubGroup>?,
)

@Serializable
data class MarketLookup(
    @SerialName("i")
    val id: Int?,

    @SerialName("d")
    val description: String?,

    @SerialName("il")
    val isLive: Boolean?,

    @SerialName("mt")
    val marketType: Int?,

    @SerialName("mmdv")
    val mobileMarketDetailView: Int?,

    @SerialName("mmlv")
    val mobilMarketListView: Int = 1,

    @SerialName("p")
    val priority: Int,

    @SerialName("st")
    val sportTpe: Int?,

    @SerialName("mst")
    val marketSubType: Int?,

    @SerialName("mdv")
    val marketDetailView: Int?,


    @SerialName("n")
    val marketName: String,

    @SerialName("imm")
    val isMarketMain: Boolean?,

    @SerialName("mg")
    val marketSubGroups: ImmutableList<Int>?,

    @SerialName("mdg")
    val detailMarketSubGroups: ImmutableList<Int>?,

    @SerialName("ir")
    val isRapidMarket: Boolean?,

    @SerialName("in")
    val isNewMarket: Boolean = false,

    @SerialName("o")
    val outcomeLookup: ImmutableMap<String, String>?

){
    private var name: String? = null
    val pureName: String
        get() {
            if(name != null)
                return name.ignoreNull()

            var _name = marketName.replace("{0}.", "").trim { it <= ' ' }.ignoreNull()
            _name = _name.replace("{1}", "").replace("{1}.", "").trim { it <= ' ' }
            _name = _name.replace("{2}", "").replace("{2}.", "").trim { it <= ' ' }
            _name = _name.replace("{3}", "").replace("{3}.", "").trim { it <= ' ' }
            _name = _name.replace("{h}", "").replace("{h}.", "").trim { it <= ' ' }
            name = _name
            return _name.ignoreNull()
        }

    fun getName(specialOdd: String): String {
        if(specialOdd.isNullOrEmpty())
            return pureName
        var _name = marketName.ignoreNull()
        if (marketName.contains("{h}"))
            _name = getNameHandicap(specialOdd)

        var sovs:ImmutableList<String> = specialOdd.split("|").toImmutableList()

        _name = if (sovs.isNotEmpty()) {
            _name.replace("{0}", sovs[0]).trim { it <= ' ' }
        } else {
            _name.replace("{0}", "").trim { it <= ' ' }
        }

        _name = if (sovs.size >  1) {
            _name.replace("{1}", sovs[1]).trim { it <= ' ' }
        } else {
            _name.replace("{1}", "").trim { it <= ' ' }
        }

        _name = if (sovs.size >  2) {
            _name.replace("{2}", sovs[2]).trim { it <= ' ' }
        } else {
            _name.replace("{2}", "").trim { it <= ' ' }
        }

        return _name
    }

    private fun getNameHandicap(specialOdd: String): String {
        val _name = marketName.ignoreNull()
        val _handicap = getHandicap(specialOdd)

        return _name.replace("{h}", _handicap).trim { it <= ' ' }
    }

    fun getHandicap(specialOdd: String): String {
        val sovs:ImmutableList<String> = specialOdd.split("|").toImmutableList()

        val handicap = if (sovs.isNotEmpty() && sovs.get(sovs.size-1).isNotEmpty()) {
            var homeHandicap = "0"
            var awayHandicap = "0"
            val sov = sovs.get(sovs.size-1).toDoubleIgnoreNull().ignoreNull()

            val handicap = if(sov.isInt())
                abs(sov).toInt().toString()
            else
                abs(sov).toString()

            if (sov > 0)
                homeHandicap = handicap
            else if(sov < 0)
                awayHandicap = handicap

            "($homeHandicap:$awayHandicap)"
        } else {
            ""
        }
        return handicap
    }
    fun getOutcomeName(outcomeNo:Int, sov:String?):String?{
        outcomeLookup?.let { lookup ->
            val strNo = outcomeNo.toString()
            return lookup[strNo]?.replace("{0}",sov.ignoreNull())
        }
        return null
    }
}

fun MarketLookup?.getSportsBookOutcomeSize() : Int{
    return when(this?.mobilMarketListView){
        MarketViewType.OUTCOME_ONE.value -> 1
        MarketViewType.OUTCOME_FOUR.value -> 4
        MarketViewType.OUTCOME_TWO.value,
        MarketViewType.OUTCOME_TWO_HANDICAP.value -> 2
        else -> 3
    }
}

fun MarketLookup?.getDetailOutcomeSize() : Int{
    return when(this?.mobileMarketDetailView) {
        MarketViewType.OUTCOME_ONE.value -> 1
        MarketViewType.OUTCOME_TWO.value,
        MarketViewType.OUTCOME_FOUR.value,
        MarketViewType.OUTCOME_TWO_HANDICAP.value,
        MarketViewType.TWO_OUTCOME_DOUBLE_LINE.value -> 2
        else -> 3
    }
}
@Serializable
data class SportType(
    @SerialName("i")
    val id: Int?,

    @SerialName("n")
    val sportTypeName: String?,

    @SerialName("p")
    val priority: Int?,

    @SerialName("ics")
    val sportTypeIconSelected: String?,

    @SerialName("ic")
    val sportTypeIcon: String?,

    @SerialName("icc")
    val sportTypeIconForCoupon: String?,

    @SerialName("sl")
    val slug: String?
)
@Serializable
data class MarketGroup(
    @SerialName("i")
    val id: Int?,

    @SerialName("n")
    val marketGroupName: String?,

    @SerialName("p")
    val priority: Int?,

    @SerialName("st")
    val sportType: Int?,

    @SerialName("sg")
    val subGroups: ImmutableList<Int>?,

    @SerialName("im")
    val isMobil: Boolean?,

    @SerialName("iv")
    val isVisible: Boolean?
){
    // Used From Market Groups
    var isSelected : Boolean = false
}

@Serializable
data class MarketSubGroup(
    @SerialName("i")
    val id: Int?,

    @SerialName("n")
    val marketSubGroupName: String?,

    @SerialName("p")
    val priority: Int?,

    @SerialName("m")
    val markets: ImmutableList<String>?, // market unique key - list

    @SerialName("im")
    val isMobil: Boolean?,

    @SerialName("iv")
    val isVisible: Boolean?,

    @SerialName("mg")
    val marketGroup: Int?,

    @SerialName("il")
    val isLive: Boolean?

)

@Serializable
data class DetailMarketGroup(
    @SerialName("i")
    val id: Int?,

    @SerialName("n")
    val marketGroupName: String?,

    @SerialName("p")
    val priority: Int?,

    @SerialName("st")
    val sportType: Int?,

    @SerialName("sg")
    val subGroups: ImmutableList<Int>?,

    @SerialName("iv")
    val isVisible: Boolean?
)

@Serializable
data class DetailMarketSubGroup(
    @SerialName("i")
    val id: Int,

    @SerialName("n")
    val marketSubGroupName: String,

    @SerialName("p")
    val priority: Int = 999,

    @SerialName("mg")
    val marketGroup: Int = -999,

    @SerialName("m")
    val markets: ImmutableList<String>?, // market unique key - list

    @SerialName("iv")
    val isVisible: Boolean?,

    @SerialName("il")
    val IsLive: Boolean?
)
