package com.example.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventScoreDTO(
    @SerialName("eid") var eventId: Int = 0,
    @SerialName("id") var id: Int = 0,
    @SerialName("t") var updateDate: Long?,
    @SerialName("s") var status: Int = 0,
    @SerialName("ht") var homeTeam: EventTeamScoreDTO?,
    @SerialName("at") var awayTeam: EventTeamScoreDTO?,
    @SerialName("min") var minute: String?,
    @SerialName("sec") var second: String?,
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

    fun getTeamSetScore(teamScore: EventTeamScoreDTO?, set: Int, type: ScoreType): Int? {
        if(teamScore != null) {
            val partScore: ArrayList<SetScore>? = if(type == ScoreType.SETS) {
                teamScore.setScores
            }
            else {
                teamScore.quarterScores
            }

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

    fun update(score:EventScoreDTO){
        eventId = score.getEventIdGreaterThanZero()
        score.updateDate?.let { updateDate = it }
         status = if(score.status > 0) score.status else status
        score.minute?.let { minute = it }
        score.second?.let { second = it }
        score.homeTeam?.let { homeScore ->
            if (score.awayTeam?.servingPlayer == true){
                homeTeam?.servingPlayer = false
            }

            homeTeam?.update(homeScore)
        }
        score.awayTeam?.let { awayScore ->
            if (score.homeTeam?.servingPlayer == true){
                awayTeam?.servingPlayer = false
            }

            awayTeam?.update(awayScore)
        }
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
data class EventTeamScoreDTO(
    @SerialName("r") var regularScore: Int?,
    @SerialName("c") var currentScore: Int?,
    @SerialName("qs") var quarterScores: ArrayList<SetScore>?,
    @SerialName("et") var extraTimeScore: Int?,
    @SerialName("pe") var penaltiesScore: Int?,
    @SerialName("co") var corner: Int?,
    @SerialName("hco") var halfCorner: Int?,
    @SerialName("yc") var yellowCard: Int?,
    @SerialName("rc") var redCard: Int?,
    @SerialName("gs") var gameScore: Int?,
    @SerialName("sp") var servingPlayer: Boolean?,
    @SerialName("ss") var setScores: ArrayList<SetScore>?,
    @SerialName("ht") var halfScore: Int?
) {
    fun update(newTeamScore: EventTeamScoreDTO){
        newTeamScore.regularScore?.let { regularScore = it }
        newTeamScore.currentScore?.let { currentScore = it }
        newTeamScore.extraTimeScore?.let { extraTimeScore = it }
        newTeamScore.penaltiesScore?.let { penaltiesScore = it }
        newTeamScore.corner?.let { corner = it }
        newTeamScore.halfCorner?.let { halfCorner = it }
        newTeamScore.yellowCard?.let { yellowCard = it }
        newTeamScore.redCard?.let { redCard = it }
        newTeamScore.gameScore?.let { gameScore = it }
        newTeamScore.halfScore?.let { halfScore = it }
        newTeamScore.servingPlayer?.let { servingPlayer = it }

        newTeamScore.setScores?.let { partScores ->
            partScores.forEach { newSetScoreItem ->
                var setScore = setScores?.firstOrNull { it.number == newSetScoreItem.number }
                if(setScore == null) {
                    setScore = SetScore(
                        newSetScoreItem.score,
                        newSetScoreItem.number,
                        newSetScoreItem.tieBreakScore
                    )
                    setScores?.add(setScore)
                }else{
                    setScore.update(newSetScoreItem)
                }
            }
        }

        newTeamScore.quarterScores?.let {
            it.forEach { newQuarterScoreItem ->
                var quarterScore = quarterScores?.filter { it.number == newQuarterScoreItem.number }?.firstOrNull()
                if(quarterScore == null) {
                    quarterScore = SetScore(
                        newQuarterScoreItem.score,
                        newQuarterScoreItem.number,
                        newQuarterScoreItem.tieBreakScore
                    )
                    quarterScores?.add(quarterScore)
                }else{
                    quarterScore.update(newQuarterScoreItem)
                }
            }
        }

    }

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
data class SetScore(
    @SerialName("s") var score: Int?,
    @SerialName("n") val number: Int,
    @SerialName("tb") var tieBreakScore: Int = -1
){
    fun update(_newSetScore:SetScore){
        _newSetScore.score?.let {
            score = it
        }
        if(_newSetScore.tieBreakScore >-1){
            tieBreakScore = _newSetScore.tieBreakScore
        }
    }
}
