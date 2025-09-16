package com.example.myapplication.manager

import com.example.model.Competition
import com.example.model.DetailMarketGroup
import com.example.model.DetailMarketSubGroup
import com.example.model.MarketGroup
import com.example.model.MarketLookup
import com.example.model.MarketResponse
import com.example.model.ProgramTypeEnum
import com.example.model.SportInfo
import com.example.model.SportTypeEnum
import com.example.model.ignoreNull
import com.example.model.toArrayList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object MarketConfig{
    var marketConfig : MarketResponse = MarketResponse(mapOf(), null,null,null,null,null)

    private val _sportsBookInfo = MutableStateFlow<List<SportInfo>>(listOf())
    val sportsBookInfo : StateFlow<List<SportInfo>> get() = _sportsBookInfo.asStateFlow()


    fun visiblePreSports() = sportsBookInfo.value?.filter { it.totalEventCount.ignoreNull() > 0 }?.map {
        it.sportId
    }

    fun visibleLiveSports() = marketConfig.sportType?.toList()?.filter {
        visiblePreSports()?.contains(it.second.id.ignoreNull()) == true
    }?.sortedBy {
        it.second.priority
    }

    fun getSelectableSportInfo(programType: Int, sportId: Int): Int {

        val info = sportsBookInfo.value?.firstOrNull { it.sportId == sportId }

        val hasEventsForSelected = if (programType == ProgramTypeEnum.LongTerm.value) {
            info?.outrightEventCount.ignoreNull() > 0
        } else {
            (info?.liveCount.ignoreNull() + info?.upcomingCount.ignoreNull()) > 0
        }

        if (hasEventsForSelected) return sportId


        // if event count is 0 will select another sport type
        val visibleSportsInfo =
            if (programType == ProgramTypeEnum.Live.value) {
                sportsBookInfo.value?.filter { (it.liveCount.ignoreNull() + it.upcomingCount.ignoreNull()) > 0 }
            } else {
                sportsBookInfo.value?.filter { it.outrightEventCount.ignoreNull() > 0 }
            }
        val sorted = visibleSportsInfo?.sortedBy {
            marketConfig.sportType?.get(it.sportId.toString())?.priority ?: 9999
        }
        return sorted?.firstOrNull()?.sportId.ignoreNull(SportTypeEnum.SOCCER.sportId)
    }

        fun updateMarketConfigs(response: MarketResponse){
            marketConfig = response
        }
        fun updateSportsBookInfo(response: List<SportInfo>){
            _sportsBookInfo.tryEmit(response)
        }

        fun showOrHideLiveType () = sportsBookInfo.value?.any {
            ((it.liveCount ?: 0) > 0) || ((it.upcomingCount ?: 0) > 0 )
        }

        fun getMarketLookup(key:String): MarketLookup?{
            if(key.isNullOrEmpty())
                return null
            return marketConfig.marketLookup[key]
        }

        fun getOutcomeLookup(key:String, no:Int, sov:Double): String?{
            val marketLookup = getMarketLookup(key)
            val strSov = if(sov > 0) sov.toString() else ""
            marketLookup?.let {
                return it.getOutcomeName(no, strSov)
            }
            return null
        }

        fun getMarketGroups(sportId:Int, isLive:Boolean):List<MarketGroup>{
            val list = marketConfig.marketGroup?.values?.filter { group ->
                group.isMobil == true &&
                group.sportType == sportId &&
                !marketConfig.marketSubGroup?.values?.filter { sub ->
                    sub.marketGroup == group.id &&
                    sub.isLive == isLive
                }.isNullOrEmpty()
            }.toArrayList()

            return list.sortedWith(compareBy({ it.priority }, { it.marketGroupName }))
        }


        fun getDetailMarketGroups(sportId:Int):ArrayList<DetailMarketGroup>{
            var list:ArrayList<DetailMarketGroup> = ArrayList()

            marketConfig.detailMarketGroup?.let { groups ->
                list.addAll(
                   ArrayList(groups.values.filter { it.sportType == sportId })
                )
            }
            list = ArrayList(list.sortedWith(compareBy({ it.priority }, { it.marketGroupName })))
            return list
        }

        fun getDetailMarketSubGroups(sportId: Int, groupId:Int? = -1):ArrayList<DetailMarketSubGroup>{

            var list:ArrayList<DetailMarketSubGroup> = ArrayList()

            marketConfig.detailMarketSubGroup?.let { subGroups ->
                list.addAll(
                    ArrayList(subGroups.values.filter {it.marketGroup == groupId  })
                )
            }
            list = ArrayList(list.sortedWith(compareBy({ it.priority }, { it.marketSubGroupName })))
            return list
        }


        private fun createMarketDetailSubgroupMap(){
            marketDetailSubGroupsMap.clear()
            marketConfig.detailMarketSubGroup?.values?.forEach(){ subGroup ->
                subGroup.markets?.forEach(){ key ->
                    if(marketDetailSubGroupsMap[key] == null)
                        marketDetailSubGroupsMap[key] = arrayListOf()

                    marketDetailSubGroupsMap[key]?.add(subGroup.id)
                }
            }
        }

        fun getDetailGroup(id:Int):DetailMarketGroup{
            var group = marketConfig.detailMarketGroup?.get(id.toString())
            if(group == null)
                group = getOtherDetailGroup()

            return group
        }

        fun getDetailSubGroup(id:Int):DetailMarketSubGroup{
            var subGroup = marketConfig.detailMarketSubGroup?.get(id.toString())
            if(subGroup == null)
                subGroup = getOtherDetailSubGroup()

            return subGroup
        }

        var marketDetailSubGroupsMap: HashMap<String, ArrayList<Int>> = hashMapOf()
        fun getMarketDetailSubGroupMap():HashMap<String, ArrayList<Int>> {
                if(marketDetailSubGroupsMap.isEmpty())
                    createMarketDetailSubgroupMap()
            return marketDetailSubGroupsMap
        }



        fun getMarketDetailSubGroup(groupId: Int, marketKey:String):DetailMarketSubGroup{
            val subGroupIds = getMarketDetailSubGroupMap()[marketKey]
            if(subGroupIds.isNullOrEmpty()) {
                return getOtherDetailSubGroup()
            }

            subGroupIds.forEach {
                if(getDetailSubGroup(it).marketGroup == groupId)
                    return getDetailSubGroup(it)
            }

            return getDetailSubGroup(subGroupIds.get(0))
        }

        private var _otherDetailSubGroup:DetailMarketSubGroup? = null
        fun getOtherDetailSubGroup(): DetailMarketSubGroup {
            if(_otherDetailSubGroup == null) {
                _otherDetailSubGroup = DetailMarketSubGroup(
                    -999,
                    "Diğer",
                    999,
                    -999,
                    null,
                    false,
                    false)

            }
            return _otherDetailSubGroup as DetailMarketSubGroup
        }


        private var _otherDetailGroup:DetailMarketGroup? = null
        fun getOtherDetailGroup(): DetailMarketGroup {
            if (_otherDetailGroup == null) {
                _otherDetailGroup = DetailMarketGroup(
                    -999,
                    "Diğer",
                    999,
                    -1,
                    null,
                    false
                )

            }
            return _otherDetailGroup as DetailMarketGroup
        }


        /**** competition ****/
        var competitions : List<Competition> = listOf()
        var competitionsMap :Map<Int, Competition>? = null
        fun updateCompetitions(competition: List<Competition>) {
            competitions = competition

            competitionsMap = competition.map { it.id to it }.toMap()
        }

        fun getCompetition(id:Int):Competition{
            competitionsMap?.let {
                it[id]?.let { competition ->
                    return competition
                }
            }
            return getOtherCompetition()
        }

        private var _otherCompetition:Competition? = null
        fun getOtherCompetition(): Competition {
            if (_otherCompetition == null) {
                _otherCompetition = Competition(
                    -999,
                    "DĞR",
                    "Diğer",
                    999
                )

            }
            return _otherCompetition as Competition
        }
    }

fun String.getFlag(): String {
    val flagPrefix = "https://static.iddaa.com/images/country-flags/{flag}.png"
    val flagPrefixWithIds = "https://static.iddaa.com/images/country-flags-with-ids/{flag}.png"

    val isNumber = this.toIntOrNull() != null
    val selectedPrefix = if (isNumber) flagPrefixWithIds else flagPrefix

    return selectedPrefix.replace("{flag}", this)
}


