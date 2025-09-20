package com.example.myapplication.manager

import androidx.compose.material3.ListItem
import androidx.lifecycle.viewModelScope
import com.example.mapper.toEventItem
import com.example.mapper.toEventScoreItem
import com.example.mapper.toMarketItem
import com.example.model.BettingPhase
import com.example.model.DisplayItem
import com.example.model.EventItem
import com.example.model.EventsDTO
import com.example.model.MarketItem
import com.example.model.MarketLookup
import com.example.model.OutComesDTO
import com.example.model.OutComesItem
import com.example.model.ProgramTypeEnum
import com.example.model.SportTypeEnum
import com.example.model.ignoreNull
import com.example.myapplication.DateHelper
import com.example.myapplication.viewmodel.ListItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SportsBookUiHandler{

    private val filterManager by lazy { SportsBookFilterManager() }
    private val selectedProgramType get() = SportsBookFilterManager.selectedFilter.programType
    private val selectedSportId get() = SportsBookFilterManager.selectedFilter.sportId

    fun getSportsbookUiList(): List<ListItem> {
        val filterManager by lazy { SportsBookFilterManager() }
        val selectedProgramType = SportsBookFilterManager.selectedFilter.programType
        val selectedSportId = SportsBookFilterManager.selectedFilter.sportId

        val events = EventStoreManager().getSportEvents(selectedProgramType, selectedSportId)


        val filteredEvents = filterManager.filterEvents(events.data)

        val shortedList = sortAndGetEvents (
            filteredEvents,
            SportsBookFilterManager.selectedFilter.groupByLeague.isClicked || selectedProgramType == ProgramTypeEnum.Live.value
        )

        val muks = SportsBookFilterManager.selectedFilter.selectedGroupKey.markets
        val key1:String? = if(muks.size>0) muks.get(0) else null
        val key2:String? = if(muks.size>1) muks.get(1) else null

        var muk1:String? = null
        var muk2:String? = null
        var sov1:String? = null
        var sov2:String? = null
        key1?.let {
            val muklist1 = key1.split("_")
            muk1 = muklist1.getOrNull(0).ignoreNull("1") +"_"+
                    muklist1.getOrNull(1).ignoreNull("1")
            sov1 = muklist1.getOrNull(2)
        }
        key2?.let {
            val muklist2 = key2.split("_")
            muk2 = muklist2.getOrNull(0).ignoreNull("1") +"_"+
                    muklist2.getOrNull(1).ignoreNull("1")
            sov2 = muklist2.getOrNull(2)
        }

        val marketLookup1 = MarketConfig.getMarketLookup(muk1.ignoreNull("-"))
        var marketLookup2: MarketLookup? = null
        muk2?.let {
            marketLookup2 = MarketConfig.getMarketLookup(muk2.ignoreNull("-"))
        }
        val uiList = loadItemToList(marketLookup1, marketLookup2, muk1, muk2, sov1, sov2, shortedList)

        return uiList
    }

    private fun sortAndGetEvents(events: List<EventsDTO>, isLeagueSort:Boolean): List<EventsDTO> {
        val sortedEvents = events.sortedWith { event1: EventsDTO, event2: EventsDTO ->

            var res = -1 *  event1.bettingPhase.compareTo( event2.bettingPhase)

            if(res == 0 && !isLeagueSort){
                res = event1.eventDate.compareTo( event2.eventDate)
            }

            if (res == 0 && isLeagueSort && event1.bettingPhase == 0 && event2.bettingPhase == 0 && SportsBookFilterManager.selectedFilter.programType == ProgramTypeEnum.Live.value){
                res = event1.eventDate.compareTo( event2.eventDate)
            }

            if(res == 0){
                val competition1 = MarketConfig.getCompetition(event1.competitionId)
                val competition2 = MarketConfig.getCompetition(event2.competitionId)
                res = competition1.priority.compareTo( competition2.priority)

                if(res == 0){
                    res = competition1.id.compareTo( competition2.id)
                }
            }

            if (res == 0) {
                res = event1.eventDate.compareTo( event2.eventDate)
            }

            if (res == 0) {
                res = event1.getName().compareTo(event2.getName())
            }

            res
        }
        return sortedEvents
    }

    private fun loadItemToList(
        marketLookup1: MarketLookup?, marketLookup2: MarketLookup?,
        muk1: String?, muk2: String?,
        sov1: String?, sov2: String?,
        shortedList: List<EventsDTO>,
    ): List<ListItem> {
        val uiList: MutableList<ListItem> = mutableListOf()

        val selectedProgramType = SportsBookFilterManager.selectedFilter.programType
        val selectedSportId = SportsBookFilterManager.selectedFilter.sportId

        val marketsLookups: Pair<MarketLookup?, MarketLookup?> =
            Pair(marketLookup1, marketLookup2)

        var lastCompetitionId = -111
        var lastDate:Long = 0
        var upcomingTitleAdded = false

        val coupons = CouponManagerV2.getCouponAsList()

        shortedList.forEachIndexed {index, eventDto ->
            val isSelected = coupons.any{it.eventId == eventDto.eventId}
            val event = eventDto.toEventItem(isSelected = isSelected)

            if(!upcomingTitleAdded &&
                SportsBookFilterManager.selectedFilter.programType == 1
                && event.bettingPhase == 0){
                val item = ListItem.Title(TitleItem("SIRADAKİ CANLI MAÇLAR", isNextLiveMatch = true))
                uiList.add(item)
                upcomingTitleAdded = true
                lastCompetitionId = -111
                lastDate = 0
            }

            val competitionId = event.competitionId
            if (lastCompetitionId != competitionId || lastDate != event.eventDate){
                val competition = MarketConfig.getCompetition(competitionId)

                if (SportsBookFilterManager.selectedFilter.programType == ProgramTypeEnum.LongTerm.value) {
                    val item = SpecialEventGroupItem(
                        eventDate = event.eventDate,
                        competitionId = competitionId,
                        groupName = competition.competitionName.ignoreNull(),
                        leagueCountryImage = competition.getFlagSuffix().getFlag(),
                    )
                    uiList.add(ListItem.SpecialEventGroup(item))
                } else {
                    val item = SportsbookTitleItem(
                        if (event.bettingPhase == BettingPhase.LIVE_EVENT.value) "" else DateHelper.timestampToTimeBulletinScreen(
                            event.eventDate
                        ),
                        cn = competition?.competitionName,
                        isLeagueName = true,
                        isGroupByLeague = SportsBookFilterManager.selectedFilter.groupByLeague.isClicked,
                        leagueCountryImage = competition.getFlagSuffix().getFlag(),
                        sportId = event.sportId
                    )
                    uiList.add(ListItem.SportsbookTitle(item))
                }
                lastCompetitionId = competitionId
                lastDate = event.eventDate
            }

            if(SportsBookFilterManager.selectedFilter.programType == ProgramTypeEnum.Live.value &&
                event.bettingPhase == BettingPhase.PRE_EVENT.value){
                val item = UpComingEventItem(event)
                uiList.add(ListItem.UpComingEvent(item))
            }else {
                var market1: MarketItem? = null
                var market2: MarketItem? = null
                muk1?.let {
                    market1 = event.getMarket(it, sov1)
                }
                muk2?.let {
                    market2 = event.getMarket(it, sov2)
                }

                val markets: Pair<MarketItem?, MarketItem?> = Pair(market1, market2)


                if (selectedProgramType == ProgramTypeEnum.LongTerm.value || (selectedProgramType == BettingPhase.LIVE_EVENT.value && selectedSportId == SportTypeEnum.MOTO_GP.sportId)) {
                    addSpecialAndMotoGpEventsOutcomes(event)
                } else if(event.bettingPhase == BettingPhase.LIVE_EVENT.value) {
                    val eventScore = EventStoreManager.eventScores[event.sportId]?.data?.get(event.eventId.toString())?.toEventScoreItem()

                    val item = LiveSportsBookItem(
                        event = event,
                        markets = markets,
                        marketLookups = marketsLookups,
                        score = eventScore
                    )
                    uiList.add(ListItem.LiveEvent(item))
                }
                else{
                    val item = PreSportsBookItem(
                        event = event,
                        markets = markets,
                        marketLookups = marketsLookups,
                    )
                    uiList.add(ListItem.PreEvent(item))
                }
            }
        }

        return uiList
    }


    private fun addSpecialAndMotoGpEventsOutcomes(event: EventItem) {
        val uiList: MutableList<ListItem> = mutableListOf()

        val programTpe = SportsBookFilterManager.selectedFilter.programType
        var index = 0
        val markets = event.markets

        markets?.forEach{ market ->
            index = 0

            val marketLookup = MarketConfig.getMarketLookup(market.key())
            marketLookup?.let { mLookup ->

                val item = SpecialEventTitleItem(
                    minimumBetCount = market.mbc.ignoreNull(),
                    eventName = if (programTpe == ProgramTypeEnum.LongTerm.value) event.getName() else marketLookup.pureName,
                    eventTime = DateHelper.timestampToTimeBulletinScreen(event.eventDate),
                    bettingPhase = event.bettingPhase
                )
                uiList.add(ListItem.SpecialEventTitle(item))

                var line = 0
                var shouldShowTopPadding = true
                market.outComes?.let { outcomes ->

                    var j = 0
                    while (j < outcomes.size ){
                        val subList: ArrayList<OutComesItem> = ArrayList<OutComesItem>(
                            outcomes.subList(j, minOf(j + 3, outcomes.size))
                        )

                        val item = SpecialEventOutComesItem(
                            index = index,
                            marketLookup = mLookup,
                            event = event,
                            market = market,
                            outcomes = subList.toImmutableList(),
                            oddCount = subList.size,
                            shouldShowTopPadding = shouldShowTopPadding
                        )
                        uiList.add(ListItem.SpecialEventOutcome(item))
                        line++
                        j += 3
                        shouldShowTopPadding = false
                    }
                }
                index++
            }
        }
    }
}


