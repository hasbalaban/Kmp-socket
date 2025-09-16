package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class EventDataInfo(
    val isdiff : Boolean,
    val version : Long = 0,
    val events : List<Events>,
    @SerialName("rmi")
    val removedEvents:ArrayList<Int>?,
    @SerialName("sc")
    val eventScores : Map<String, EventScore>? = null,
)

@Serializable
data class Events(
    @SerialName("i")
    val eventId : Int,

    @SerialName("mpi")
    val mappedId: Int?,

    @SerialName("bri")
    val betRadarId : Long? = null,

    @SerialName("cref")
    val cref : Long? = null,

    @SerialName("v")
    var eventVersion : Long,

    @SerialName("n")
    val eventName : String?,

    @SerialName("sid")
    val sportId : Int,

    @SerialName("d")
    val eventDate : Long,

    // 0:closed, 1, open, -1, paused, -2:suspended
    @SerialName("s")
    var status : Int,

    // 0:pre, 1:live
    @SerialName("bp")
    var bettingPhase : Int = 0,

    @SerialName("il")
    val isLive : Boolean = false,

    @SerialName("mbc")
    var minimumBetCount : Int = 1,

    @SerialName("kOdd")
    val kingOdds : Boolean = false,

    @SerialName("kMbc")
    val kingMbc : Boolean = false,

    @SerialName("kLive")
    val isKingLive : Boolean = false,

    @SerialName("hduel")
    val hasDuel : Boolean? = false,

    @SerialName("hr")
    val hasRapid : Boolean? = null,

    @SerialName("m")
    val markets : ArrayList<Market>? = null,

    @SerialName("ci")
    val competitionId : Int,

    @SerialName("rhei")
    val realHomeEventId : Int? = null,

    @SerialName("raei")
    val realAwayEventId : Int? = null,

    //Canlı maçlar için maçın skor bilgisi döner
    @SerialName("sc")
    var score : EventScore? = null,

    @SerialName("oc")
    var oddCount : Int?,

    @SerialName("hn")
    val homeTeamName : String?,

    @SerialName("an")
    val awayTeamName : String?,

    @SerialName("hc")
    val hasComments: Boolean? = false,

    @SerialName("hs")
    val hasStream: Boolean? = false,

    var sliderMarkets: ArrayList<Market>? = null,
){

    var isFavorite = false
    var isSelected = false

    fun getName():String{
        if(!eventName.isNullOrEmpty())
            return eventName.ignoreNull("-")

        return "$homeTeamName - $awayTeamName"
    }

    fun getNameBanner():String{
        return "$homeTeamName\n$awayTeamName"
    }

    fun getMarkets(muks : List<String>): List<Market>? {
        return markets?.filter {
            muks.contains(it.key())
        }
    }

    fun getMarket(muk : String, sov:String? = null): Market? {
        try {
            return markets?.filter {
                muk == it.key() &&
                        (sov == null || sov.toDoubleIgnoreNull() == it.sov() )
            }?.firstOrNull()
        }catch (e:Exception){
            return null
        }
    }

    fun update(socketEvent : SocketEvent) : Boolean{
        var updated = false
        socketEvent.eventVersion?.let { socketVersion->
            if (socketVersion > eventVersion){
                updated = true
                eventVersion = socketVersion

                socketEvent.bettingPhase?.let { bp ->
                    bettingPhase = bp
                }

                socketEvent.eventStatus?.let { es ->
                    status = es
                }

                socketEvent.oddCount?.let { oc ->
                    oddCount = oc
                }

                socketEvent.minimumBetCount?.let { mbc ->
                    minimumBetCount = mbc
                }
            }
        }

        socketEvent.markets?.let {socketMarkets->
            socketMarkets.forEach {socketMarket ->
                val currentMarket =  markets?.firstOrNull { it.marketId == socketMarket.id }
                var _updated = false
                currentMarket?.let {
                    _updated = currentMarket.update(socketMarket)
                } ?: run {
                    _updated = addMarket(socketMarket)
                }
                updated = updated || _updated
            }
        }

        return updated
    }

    fun addMarket(socketMarket: SocketEventMarket) :Boolean{
        val types = socketMarket.muk?.split("_")
        types?.let {
            if (it.size<2)
                return false
            val type = types[0].toInt()
            val subtype = types[1].toInt()

            val outcomes = socketMarket.outcomes?.map {
                OutComes(it.no.ignoreNull(), it.odd, null, it.webOdd, it.name.ignoreNull())
            }.toArrayList()

            var newMarket = Market(
                marketId = socketMarket.id,
                type = type,
                subtype= subtype,
                version =socketMarket.version.ignoreNull(),
                status = socketMarket.status.ignoreNull(),
                mbc = socketMarket.mbc.ignoreNull(1),
                outComes = outcomes,
                specialOddValue = socketMarket.speacialOddValue
            )

            markets?.add(newMarket)
            return true
        }
        return false
    }

}

@Serializable
data class Market (

    @SerialName("i")
    val marketId : Long ,

    @SerialName("t")
    val type : Int? = null,

    @SerialName("st")
    val subtype : Int? = null,

    @SerialName("v")
    var version : Long = 0,

    @SerialName("s")
    var status : Int? = null,

    @SerialName("mbc")
    var mbc : Int?,

    @SerialName("o")
    var outComes : ArrayList<OutComes>? = null,

    @SerialName("sov")
    val specialOddValue : String? ,

) {
    fun key()= type.toString() + "_" + subtype

    private var specialOdd:Double?= null
    fun sov():Double{
        if(specialOdd != null)
            return specialOdd.ignoreNull()
        val sovs:List<String> = specialOddValue.ignoreNull().split("|")

        specialOdd =  if (sovs.isNotEmpty()){
            sovs[sovs.size-1].toDoubleIgnoreNull()
        }else{
            0.0
        }
        return specialOdd.ignoreNull()
    }

    fun update(socketMarket: SocketEventMarket) : Boolean {
        var updated = false
        socketMarket.version?.let { socketVersion ->
            if (socketVersion > version.ignoreNull()){
                updated = true
                version = socketVersion
            }
            socketMarket.status?.let { newStatus ->
                status = newStatus
            }
        }

        socketMarket.outcomes?.forEach {socketOutcome->
            val currentOutCome =  outComes?.firstOrNull { it.outcomeNo == socketOutcome.no }
            currentOutCome?.let {
                val _updated = it.update(socketOutcome)
                updated = updated || _updated

            } ?: run {
                val outcome = OutComes(
                    outcomeNo = socketOutcome.no.ignoreNull(),
                odd = socketOutcome.odd,
                webOdd = socketOutcome.webOdd,
                name = socketOutcome.name.ignoreNull())
                outComes?.add(outcome)
            }
        }

        return updated
    }

    fun isViewable():Boolean {
        return  MarketStatus.isViewable(status.ignoreNull())
    }

}

@Serializable
data class OutComes(

    @SerialName("no")
    val outcomeNo : Int,

    @SerialName("odd")
    var odd : Double? = null,

    @SerialName("podd")
    var previousOdd : Double? = null,

    @SerialName("wodd")
    var webOdd : Double? = null,

    @SerialName("n")
    val name : String = "",

){

    var oddChangeStatus : OddChangeStatus? = OddChangeStatus.NONE

    fun update(outComes: SocketEventOutcome) : Boolean {
        var updated = false
        outComes.odd?.let { currentOdd ->
            updated = true
            if(currentOdd != odd)
                previousOdd = odd
            odd = currentOdd

            val pO = previousOdd.ignoreNull(1.0)
            val cO = odd.ignoreNull(1.0)
            oddChangeStatus =
                if (odd.ignoreNull() <= 1.0) null
                else if (cO > pO ) OddChangeStatus.UP
                else if (pO > cO) OddChangeStatus.DOWN
                else OddChangeStatus.NONE

        }

        outComes.webOdd?.let {
            webOdd = it
        }

        return updated
    }

    fun prevOddIsDifferent(): Boolean {
        return (odd != null && odd != previousOdd)
    }

}

enum class OddChangeStatus (val value : String?) {
    UP("u"),
    DOWN("d"),
    NONE(null)
}


fun <T> List<T>?.toArrayList(): ArrayList<T> = this?.let { ArrayList(it) } ?: kotlin.run { arrayListOf() }
fun Long?.ignoreNull(defaultValue: Long = 0L): Long = this ?: defaultValue
fun Double?.ignoreNull(defaultValue: Double = 0.0): Double = this ?: defaultValue
fun Int?.ignoreNull(defaultValue: Int = 0): Int = this ?: defaultValue
fun Boolean?.ignoreNull(defaultValue: Boolean = false): Boolean = this ?: defaultValue
fun String?.ignoreNull(defaultValue: String = ""): String = this ?: defaultValue
fun Double?.isInt(): Boolean = this.ignoreNull() % 1 == 0.0


fun String?.toDoubleIgnoreNull(defaultValue: Double = 0.0): Double {
    this ?: return defaultValue
    return try {
        this.replace(",",".")
        this.toDouble()
    } catch (_: Exception) {
        defaultValue
    }
}

