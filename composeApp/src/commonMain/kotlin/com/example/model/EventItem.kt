package com.example.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class EventItem(
    @SerialName("i")
    val eventId : Int,

    @SerialName("mpi")
    val mappedId: Int?,

    @SerialName("bri")
    val betRadarId : Long? = null,

    @SerialName("cref")
    val cref : Long? = null,

    @SerialName("v")
    val eventVersion : Long,

    @SerialName("n")
    val eventName : String?,

    @SerialName("sid")
    val sportId : Int,

    @SerialName("d")
    val eventDate : Long,

    // 0:closed, 1, open, -1, paused, -2:suspended
    @SerialName("s")
    val status : Int,

    // 0:pre, 1:live
    @SerialName("bp")
    val bettingPhase : Int = 0,

    @SerialName("il")
    val isLive : Boolean = false,

    @SerialName("mbc")
    val minimumBetCount : Int = 1,

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
    val markets : ImmutableList<MarketItem>? = null,

    @SerialName("ci")
    val competitionId : Int,

    @SerialName("rhei")
    val realHomeEventId : Int? = null,

    @SerialName("raei")
    val realAwayEventId : Int? = null,

    //Canlı maçlar için maçın skor bilgisi döner
    @SerialName("sc")
    val score : EventScoreItem? = null,

    @SerialName("oc")
    val oddCount : Int?,

    @SerialName("hn")
    val homeTeamName : String?,

    @SerialName("an")
    val awayTeamName : String?,

    @SerialName("hc")
    val hasComments: Boolean? = false,

    @SerialName("hs")
    val hasStream: Boolean? = false,

    val sliderMarkets: ImmutableList<MarketItem>? = null,

    val isSelected : Boolean = false,
    val kingCount : Int,
)

@Serializable
data class MarketItem (

    @SerialName("i")
    val marketId : Long,

    @SerialName("t")
    val type : Int? = null,

    @SerialName("st")
    val subtype : Int? = null,

    @SerialName("v")
    val version : Long = 0,

    @SerialName("s")
    val status : Int? = null,

    @SerialName("mbc")
    val mbc : Int?,

    @SerialName("o")
    val outComes : ImmutableList<OutComesItem>? = null,

    @SerialName("sov")
    val specialOddValue : String?,

    ) {
    fun key()= type.toString() + "_" + subtype

    private val specialOdd:Double?= null
    fun sov():Double{
        if(specialOdd != null)
            return specialOdd.ignoreNull()
        val sovs:ImmutableList<String> = specialOddValue.ignoreNull().split("|").toImmutableList()

        return if (sovs.isNotEmpty()){
            sovs[sovs.size-1].toDoubleIgnoreNull()
        }else{
            0.0
        }.ignoreNull()
    }


    fun isViewable():Boolean {
        return  MarketStatus.isViewable(status.ignoreNull())
    }

}

@Serializable
data class EventScoreItem(
    @SerialName("eid") val eventId: Int = 0,
    @SerialName("id") val id: Int = 0,
    @SerialName("t") val updateDate: Long?,
    @SerialName("s") val status: Int = 0,
    @SerialName("ht") val homeTeam: EventTeamScoreItem?,
    @SerialName("at") val awayTeam: EventTeamScoreItem?,
    @SerialName("min") val minute: String?,
    @SerialName("sec") val second: String?,
) {
    fun getEventIdGreaterThanZero(): Int {
        return if(eventId > 0) eventId else id
    }
    fun getTime(showSecond: Boolean): String? {
        if (minute == null) return null

        val stringMinute = if (minute == "45" || minute == "90") "$minute+"
        else "$minute'"

        return if (this.second != null && showSecond) {
            "$stringMinute $second''"
        } else {
            "$stringMinute"
        }
    }

    fun getScoreString(): String {
        if(homeTeam != null && awayTeam != null) {
            val homeScore = homeTeam?.currentScore.ignoreNull(0)
            val awayScore = awayTeam?.currentScore.ignoreNull(0)
            return "$homeScore - $awayScore"
        }
        return ""
    }

    fun getHalfTimeScoreString(): String {
        if(homeTeam != null && awayTeam != null) {
            val homeScore = homeTeam?.halfScore.ignoreNull(0)
            val awayScore = awayTeam?.halfScore.ignoreNull(0)
            val halfTime = "İY:"
            return "$halfTime $homeScore - $awayScore"
        }
        return "-"
    }

    fun getHalfTimeScore(): Pair<Int?, Int?> {
        val homeScore = homeTeam?.halfScore
        val awayScore = awayTeam?.halfScore
        return Pair(first = homeScore, second = awayScore)
    }

    fun getScoreByType(isHome: Boolean, scoreType: ScoreType, partID: Int = -1): Int {
        val teamScore = if(isHome) {
            homeTeam
        } else {
            awayTeam
        }

        val score = when(scoreType) {
            ScoreType.CURRENT -> teamScore?.currentScore
            ScoreType.REGULAR -> teamScore?.regularScore
            ScoreType.HALF_TIME -> {
                return teamScore?.halfScore?.let {
                    it
                } ?: teamScore?.currentScore.ignoreNull(0)
            }
            ScoreType.EXTRA_TIME -> teamScore?.extraTimeScore
            ScoreType.PENALTIES -> teamScore?.penaltiesScore
            ScoreType.CORNER -> teamScore?.corner
            ScoreType.YELLOW_CARD -> teamScore?.yellowCard
            ScoreType.RED_CARD -> teamScore?.redCard
            ScoreType.SET_GAME_SCORE -> teamScore?.gameScore
            ScoreType.QUARTER, ScoreType.SETS -> {
                return getSetScore(isHome, partID, scoreType)
            }
            else -> -1
        }

        return score.ignoreNull(0)
    }
    fun getStrScoreByType(isHome: Boolean, scoreType: ScoreType, partID: Int = -1): String {
        val teamScore = if(isHome) {
            homeTeam
        } else {
            awayTeam
        }

        val score:Int? = when(scoreType) {
            ScoreType.CURRENT -> teamScore?.currentScore.let {
                it
            }
            ScoreType.REGULAR -> teamScore?.regularScore.let {
                it
            }
            ScoreType.HALF_TIME -> {
                teamScore?.halfScore?.let {
                    it
                } ?: teamScore?.currentScore.let {
                    it
                }
            }
            ScoreType.EXTRA_TIME -> teamScore?.extraTimeScore.let {
                it
            }
            ScoreType.PENALTIES -> teamScore?.penaltiesScore.let {
                it
            }
            ScoreType.CORNER -> teamScore?.corner.let {
                it
            }
            ScoreType.YELLOW_CARD -> teamScore?.yellowCard.let {
                it
            }
            ScoreType.RED_CARD -> teamScore?.redCard.let {
                it
            }
            ScoreType.SET_GAME_SCORE -> teamScore?.gameScore.let {
                return  if(it.ignoreNull(0) > 40) "AV" else it.ignoreNull(0).toString()
            }
            ScoreType.QUARTER, ScoreType.SETS -> {
                return getStrSetScore(isHome, partID, scoreType).ignoreNull()
            }
            else -> null
        }

        return score?.let {
            score.toString()
        }?: run { "-" }
    }
    fun getCurrentPartScore(isHome: Boolean, type: ScoreType):String{

        val part = when(MatchScoreStatus.getFromValue(status)){
            MatchScoreStatus.FIRST_QUARTER,
            MatchScoreStatus.FIRST_PERIOD,
            MatchScoreStatus.FIRST_SET -> 1
            MatchScoreStatus.SECOND_PERIOD,
            MatchScoreStatus.SECOND_SET -> 2
            MatchScoreStatus.THIRD_QUARTER,
            MatchScoreStatus.THIRD_PERIOD,
            MatchScoreStatus.THIRD_SET -> 3
            MatchScoreStatus.FOURTH_QUARTER,
            MatchScoreStatus.FOURTH_PERIOD,
            MatchScoreStatus.FOURTH_SET -> 4
            MatchScoreStatus.FIFTH_PERIOD,
            MatchScoreStatus.FIFTH_SET -> 5
            MatchScoreStatus.SIXTH_PERIOD,
            MatchScoreStatus.SIXTH_SET -> 5
            else -> 0

        }

        return if(part > 0)
            getStrSetScore(isHome,part,type).ignoreNull("")
        else
            ""
    }
    fun getStrSetScore(isHome: Boolean, set: Int, type: ScoreType): String? {
        val score = if(isHome) {
            getTeamSetScore(homeTeam, set, type)
        } else {
            getTeamSetScore(awayTeam, set, type)
        }
        return score?.toString().ignoreNull()
    }
    fun getSetScore(isHome: Boolean, set: Int, type: ScoreType): Int {
        val score = if(isHome) {
            getTeamSetScore(homeTeam, set, type)
        } else {
            getTeamSetScore(awayTeam, set, type)
        }
        return score ?: 0
    }

    fun getTeamSetScore(teamScore: EventTeamScoreItem?, set: Int, type: ScoreType): Int? {
        if(teamScore != null) {
            val partScore = if(type == ScoreType.SETS)
                teamScore.setScores
            else teamScore.quarterScores

            if(!partScore.isNullOrEmpty() && partScore.size >= set) {
                partScore.forEach {
                    if(it.number == set)
                        return it.score
                }
            }
        }
        return null
    }

    fun getSetNumber(): Int {
        if(homeTeam != null && awayTeam != null) {
            return homeTeam?.setScores?.size ?: 0
        }
        return 0
    }

    fun getGameNumber(): Int {
        if(homeTeam != null && awayTeam != null) {
            val homeScore = homeTeam?.setScores?.lastOrNull()?.score ?: 0
            val awayScore = awayTeam?.setScores?.lastOrNull()?.score ?: 0
            return homeScore + awayScore + 1
        }
        return 0
    }

    fun hasTieBreak(): Boolean {
        val setScores = homeTeam?.setScores
        setScores?.let { scores ->
            scores.forEach {
                if(it.tieBreakScore > -1)
                    return true
            }
        }
        return false
    }

    fun hasScore(type: ScoreType): Boolean {
        return when(type) {
            ScoreType.CURRENT -> homeTeam?.currentScore != null
            ScoreType.REGULAR -> homeTeam?.regularScore != null
            ScoreType.HALF_TIME -> homeTeam?.currentScore != null
            ScoreType.EXTRA_TIME -> homeTeam?.extraTimeScore != null
            ScoreType.PENALTIES -> homeTeam?.penaltiesScore != null
            ScoreType.CORNER -> homeTeam?.corner != null
            ScoreType.YELLOW_CARD -> homeTeam?.yellowCard != null
            ScoreType.RED_CARD -> homeTeam?.redCard != null
            ScoreType.SET_GAME_SCORE -> homeTeam?.gameScore != null
            ScoreType.QUARTER -> {
                homeTeam?.quarterScores != null && !homeTeam?.quarterScores.isNullOrEmpty()
            }
            ScoreType.SETS ->{
                homeTeam?.setScores != null && !homeTeam?.setScores.isNullOrEmpty()
            }
            else -> false
        }
    }

    fun getFullTimeScoreString(): CharSequence? {
        if(homeTeam != null && awayTeam != null) {
            val homeScore = homeTeam?.currentScore.ignoreNull(0)
            val awayScore = awayTeam?.currentScore.ignoreNull(0)
            return "$homeScore - $awayScore"
        }
        return "-"
    }


    fun getScoreWithGameStatus(): String {
        val statusType = MatchScoreStatus.getFromValue(status)
        val isActive = statusType.isActive()
        val homeLastSet = homeTeam?.setScores?.lastOrNull()
        val awayLastSet = awayTeam?.setScores?.lastOrNull()

        if (!isActive || homeLastSet == null || awayLastSet == null) {
            return statusType.abbr
        }

        return if (homeLastSet.score == 6 && awayLastSet.score == 6) {
            "${statusType.abbr} | Tiebreak"
        } else {
            val totalGameOnCurrentSet = (homeLastSet.score ?: 0) + (awayLastSet.score ?: 0) + 1
            "${statusType.abbr} | Oyun $totalGameOnCurrentSet"
        }

    }

}

@Serializable
data class SetScore(
    @SerialName("s") val score: Int?,
    @SerialName("n") val number: Int,
    @SerialName("tb") val tieBreakScore: Int = -1
)


@Serializable
data class EventTeamScoreItem(
    @SerialName("r") val regularScore: Int?,
    @SerialName("c") val currentScore: Int?,
    @SerialName("qs") val quarterScores: ImmutableList<SetScoreItem>?,
    @SerialName("et") val extraTimeScore: Int?,
    @SerialName("pe") val penaltiesScore: Int?,
    @SerialName("co") val corner: Int?,
    @SerialName("hco") val halfCorner: Int?,
    @SerialName("yc") val yellowCard: Int?,
    @SerialName("rc") val redCard: Int?,
    @SerialName("gs") val gameScore: Int?,
    @SerialName("sp") val servingPlayer: Boolean?,
    @SerialName("ss") val setScores: ImmutableList<SetScoreItem>?,
    @SerialName("ht") val halfScore: Int?
) {

    fun map(): HashMap<String, Int> {
        val hashMap = hashMapOf(
            "r" to this.regularScore,
            "c" to this.currentScore,
            "et" to this.extraTimeScore,
            "pe" to this.penaltiesScore,
            "co" to this.corner,
            "yc" to this.yellowCard,
            "rc" to this.redCard,
            "gs" to this.gameScore,
            "ht" to this.halfScore,
        )

        this.quarterScores?.forEach {
            hashMap["q${it.number}"] = it.score
        }

        this.setScores?.forEach {
            hashMap["s${it.number}"] = it.score
        }

        return hashMap.filterValues { it != null }.mapValues { it.value!! }.toMap(HashMap())
    }
}

@Serializable
data class OutComesItem(

    @SerialName("no")
    val outcomeNo : Int,

    @SerialName("odd")
    val odd : Double? = null,

    @SerialName("podd")
    val previousOdd : Double? = null,

    @SerialName("wodd")
    val webOdd : Double? = null,

    @SerialName("n")
    val name : String = "",

    ){

    val oddChangeStatus : OddChangeStatus? = OddChangeStatus.NONE
    fun prevOddIsDifferent(): Boolean {
        return (odd != null && odd != previousOdd)
    }
}

@Serializable
data class SetScoreItem(
    @SerialName("s") val score: Int?,
    @SerialName("n") val number: Int,
    @SerialName("tb") val tieBreakScore: Int = -1
)