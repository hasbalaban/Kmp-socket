package com.mgmbk.iddaa.manager


import com.example.mapper.toEventItem
import com.example.mapper.toEventScoreItem
import com.example.mapper.toMarketItem
import com.example.model.BettingPhase
import com.example.model.EventDataInfo
import com.example.model.EventItem
import com.example.model.EventScoreDTO
import com.example.model.EventScoreItem
import com.example.model.EventTeamScoreDTO
import com.example.model.EventsDTO
import com.example.model.MarketItem
import com.example.model.MarketLookup
import com.example.model.ProgramTypeEnum
import com.example.model.SocketEvent
import com.example.model.SportsBookUpdateInfo
import com.example.model.ignoreNull
import com.example.model.toArrayList
import com.example.myapplication.manager.CouponManagerV2
import com.example.myapplication.manager.MarketConfig
import com.example.myapplication.manager.SportsBookFilterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.get

data class EventInfo(
    var version: Long = 0,
    var data: List<EventsDTO> = ArrayList()
)

class EventStoreManager {
    companion object {

        var liveEventData: EventData = EventData()
        var preEvents: EventData = EventData()
        var specialEvents: EventData = EventData()
        val eventScores: HashMap<SportId, ScoreData> = HashMap()

        private val _socketUpdated = MutableStateFlow<SportsBookUpdateInfo?>(null)
        val socketUpdated: StateFlow<SportsBookUpdateInfo?> = _socketUpdated.asStateFlow()

        fun findEvent(eventId: Int, sportId: Int, phase: Int): EventsDTO? {
            if (phase == BettingPhase.LIVE_EVENT.value) {
                return liveEventData.data[sportId]?.events?.get(eventId)
            }

            return preEvents.data[sportId]?.events?.get(eventId)
                ?: specialEvents.data[sportId]?.events?.get(eventId)
        }

        fun findEvent(eventId: Int): EventsDTO? {
            var foundEvent: EventsDTO?
            val liveEvents = liveEventData.data.flatMap { it.value.events.values }
            foundEvent = liveEvents.firstOrNull { it.eventId == eventId }

            if (foundEvent != null) {
                return foundEvent
            }

            val preEvents = preEvents.data.flatMap { it.value.events.values }
            foundEvent = preEvents.firstOrNull { it.eventId == eventId }

            if (foundEvent != null) {
                return foundEvent
            }

            val specialEvents = specialEvents.data.flatMap { it.value.events.values }
            foundEvent = specialEvents.firstOrNull { it.eventId == eventId }

            return foundEvent
        }

        fun getScore(sportId: Int, eventId: Int): EventScoreDTO? {
            return eventScores.get(sportId)?.data?.get(eventId.toString())
        }

        fun setScore(sportId: Int, eventScore: EventScoreDTO) {
            val eventId = eventScore.getEventIdGreaterThanZero()
            if (eventScores[sportId] == null) {
                val newScore = ScoreData()
                newScore.data = HashMap()
                eventScores[sportId] = newScore
            }
            eventScores.get(sportId)?.data?.set(eventId.toString(), eventScore)
        }

        private fun addOrUpdateEventData(
            newEvent: EventsDTO,
            updateAllData: Boolean
        ): EventsDTO? {

            val eventData =  if (newEvent.bettingPhase == BettingPhase.LIVE_EVENT.value) liveEventData
            else preEvents

            if (eventData.data[newEvent.sportId] == null) eventData.data.put(newEvent.sportId, EventInfoMap())

            if (updateAllData) {
                eventData.data[newEvent.sportId]?.events?.put(newEvent.eventId, newEvent)
                if (eventScores[newEvent.sportId] == null) {
                    eventScores[newEvent.sportId] = ScoreData(HashMap())
                }
                newEvent.score?.let {
                    eventScores[newEvent.sportId]?.data?.put(newEvent.eventId.toString(), it)
                }

            } else {

                val oldEvent = eventData.data[newEvent.sportId]?.events?.get(newEvent.eventId)
                if (oldEvent == null) {
                    eventData.data[newEvent.sportId]?.events?.put(newEvent.eventId, newEvent)
                    return eventData.data[newEvent.sportId]?.events?.get(newEvent.eventId)
                }

                val newMarketMap = newEvent.markets?.associateBy { it.marketId }
                oldEvent.markets?.forEach { oldMarket ->
                    val newMarket = newMarketMap?.get(oldMarket.marketId)
                    if (newMarket != null) {
                        if (oldMarket.marketId == newMarket.marketId) {
                            oldMarket.mbc = newMarket.mbc
                            oldMarket.version = newMarket.version
                            oldMarket.status = newMarket.status
                            oldMarket.outComes = newMarket.outComes
                        }
                    }
                }


                val oldMarketIds = oldEvent.markets?.map { it.marketId }.orEmpty().toSet()
                val notExistMarkets = newEvent.markets?.filter { newMarket ->
                    newMarket.marketId !in oldMarketIds
                }
                if (!notExistMarkets.isNullOrEmpty()) {
                    oldEvent.markets?.addAll(notExistMarkets)
                }

                eventData.data[oldEvent.sportId]?.events?.put(oldEvent.eventId, oldEvent)
                oldEvent.score?.let { score ->
                    eventScores[oldEvent.sportId]?.data?.put(oldEvent.eventId.toString(), score)
                }
                EventStoreManager().addToUpdatedList(oldEvent.eventId)
            }

            return eventData.data[newEvent.sportId]?.events?.get(newEvent.eventId)
        }

        fun setSocketUpdated(isUpdated: SportsBookUpdateInfo) {
            _socketUpdated.value = isUpdated
        }
    }

    fun provideRequestVersion(sportId: Int, programType: Int): Long {
        return when (programType) {
            ProgramTypeEnum.PreEvents.value -> preEvents.data[sportId]?.version.ignoreNull()
            ProgramTypeEnum.Live.value -> liveEventData.data[sportId]?.version.ignoreNull()
            else -> specialEvents.data[sportId]?.version.ignoreNull()
        }
    }

    fun removeEvents(programType: Int, sportId: Int, events: ArrayList<Int>) {
        events.forEach { eventId ->
            when (programType) {
                ProgramTypeEnum.PreEvents.value -> preEvents.data[sportId]?.events?.remove(eventId)
                ProgramTypeEnum.Live.value -> liveEventData.data[sportId]?.events?.remove(eventId)
                else -> specialEvents.data[sportId]?.events?.remove(eventId)
            }
        }
    }

    fun addOrUpdateSportEvents(
        programType: Int,
        sportId: Int,
        eventDataInfo: EventDataInfo,
    ) {
        eventDataInfo.removedEvents?.let {
            removeEvents(programType, sportId, it)
            val removedEventIds = mutableListOf<Int>()
            it.forEach { eventId ->
                removedEventIds.add(eventId)
            }
            CouponManagerV2.eventsRemoved(removedEventIds)
        }


        if (programType == ProgramTypeEnum.PreEvents.value) {
            if (!eventDataInfo.isdiff) {
                preEvents.data[sportId]?.events?.clear()
            }
            if (preEvents.data[sportId] == null) {
                preEvents.data += hashMapOf(sportId to EventInfoMap())
            }

            preEvents.data[sportId]?.let { it ->
                it.version = eventDataInfo.version
                eventDataInfo.events.forEach { event ->
                    it.events[event.eventId] = event
                }
            }
            return
        }


        if (programType == ProgramTypeEnum.Live.value) {
            if (!eventDataInfo.isdiff) {
                liveEventData.data[sportId]?.events?.clear()
                eventScores[sportId]?.data?.clear()

            }
            if (liveEventData.data[sportId] == null) {
                liveEventData.data += hashMapOf(sportId to EventInfoMap())
            }

            liveEventData.data[sportId]?.let { it ->
                it.version = eventDataInfo.version

                eventDataInfo.eventScores?.forEach { eScore ->
                    if (eventScores[sportId] == null) {
                        eventScores[sportId] = ScoreData(HashMap())
                    }
                    eventScores[sportId]?.let {
                        it.data[eScore.value.id.toString()] = eScore.value
                    }
                }

                eventDataInfo.events.forEach { event ->
                    eventScores.get(event.sportId)?.data?.get(event.eventId.toString())?.let {
                        if (event.score == null) {
                            event.score = it
                        }else{
                            event.score?.update(it)
                        }
                    }
                    it.events[event.eventId] = event
                }
            }
            return
        }

        if (programType == ProgramTypeEnum.LongTerm.value) {
            if (!eventDataInfo.isdiff) {
                specialEvents.data[sportId]?.events?.clear()
            }

            if (specialEvents.data[sportId] == null) {
                specialEvents.data += hashMapOf(sportId to EventInfoMap())
            }

            specialEvents.data[sportId]?.let { it ->
                it.version = eventDataInfo.version
                eventDataInfo.events.forEach { event ->
                    it.events[event.eventId] = event
                }
            }
            return
        }

    }

    fun getSportEvents(programType: Int, sportId: Int): EventInfo {
        return when (programType) {
            ProgramTypeEnum.PreEvents.value -> {
                EventInfo(
                    version = preEvents.data[sportId]?.version.ignoreNull(),
                    data = preEvents.data[sportId]?.events?.map { it.value } ?: listOf()
                )

            }

            ProgramTypeEnum.Live.value -> {
                EventInfo(
                    version = liveEventData.data[sportId]?.version.ignoreNull(),
                    data = liveEventData.data[sportId]?.events?.map { it.value } ?: listOf()
                )

            }

            else -> {
                EventInfo(
                    version = specialEvents.data[sportId]?.version.ignoreNull(),
                    data = specialEvents.data[sportId]?.events?.map { it.value } ?: listOf()
                )
            }
        }
    }

    fun updateEventFromSocket(socketEvent: SocketEvent) {
        var event: EventsDTO? =
            liveEventData.data[socketEvent.sportId]?.events?.get(socketEvent.eventId)
        event?.let {
            if (socketEvent.action == "rp" && it.bettingPhase == BettingPhase.LIVE_EVENT.value) {
                removeLiveEvent(socketEvent)
                return
            }
            val isUpdated = it.update(socketEvent)
            if (isUpdated) {
                addToUpdatedList(it.eventId)
            }
        }
        if (socketEvent.action == "rp") {
            event = preEvents.data[socketEvent.sportId]?.events?.get(socketEvent.eventId)
            event?.let {
                removePreEvent(socketEvent)
            }
            return
        }

        if (event == null) {
            event = preEvents.data[socketEvent.sportId]?.events?.get(socketEvent.eventId)
            event?.let {
                val isUpdated = it.update(socketEvent)
                if (isUpdated) {
                    addToUpdatedList(it.eventId)
                }
            }
            return
        }

    }

    private fun removeLiveEvent(socketEvent: SocketEvent) {
        liveEventData.data[socketEvent.sportId]?.events?.remove(socketEvent.eventId)
        addToUpdatedList(socketEvent.eventId)
    }

    private fun removePreEvent(socketEvent: SocketEvent) {
        specialEvents.data[socketEvent.sportId]?.events?.remove(socketEvent.eventId)
        preEvents.data[socketEvent.sportId]?.events?.remove(socketEvent.eventId)
        addToUpdatedList(socketEvent.eventId)
    }

    private val updatedEvents: HashMap<Int, Int> = HashMap()


    private fun addToUpdatedList(eventId: Int) {
        updatedEvents[eventId] = eventId
        // Post delayed daha önce çalıştırıldı mı?
        if (updatedEvents.size == 1) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(400)

                CouponManagerV2.updateSocketEvents(updatedEvents.values.toList())

                val updateInfo =
                    SportsBookUpdateInfo(1, updatedEvents.values.toList().toArrayList())
                _socketUpdated.value = updateInfo

                updatedEvents.clear()

            }
        }
    }

    fun updateScoreFromSocket(socketScore: EventScoreDTO) {

        var score: EventScoreDTO? = null
        var sportId: Int = 1

        eventScores.forEach {
            val item = it.value.data[socketScore.id.toString()]
            if (item != null) {
                score = item
                sportId = it.key
                return@forEach
            }
        }


        score?.let {
            it.update(socketScore)
            findEvent(socketScore.eventId)?.score?.update(it)
            addToUpdatedList(socketScore.id)
        } ?: run {
            score = socketScore
            score?.let {
                val eventId = socketScore.getEventIdGreaterThanZero()

                it.eventId = eventId
                it.status = socketScore.status ?: 1

                if (it.homeTeam == null)
                    it.homeTeam = EventTeamScoreDTO(null, null, null, null, null, null, null, null, null, null, servingPlayer = null, null, null)

                if (it.awayTeam == null)
                    it.awayTeam = EventTeamScoreDTO(null, null, null, null, null, null, null, null, null, null, servingPlayer = null, null, null)

                it.update(socketScore)

                addScoreToList(it, sportId)

                addToUpdatedList(socketScore.id)
            }


            val relevantEvent = liveEventData.data[sportId]?.events?.get(socketScore.eventId)
            relevantEvent?.let { relevant ->
                relevant.score = socketScore
                liveEventData.data[sportId]?.events?.set(relevant.eventId, relevantEvent)
            }
        }
    }

    fun addScoreToList(score: EventScoreDTO, sportId: Int) {
        if (eventScores[sportId] == null) {
            val newScore = ScoreData()
            newScore.data = HashMap()
            eventScores[sportId] = newScore
        }
        eventScores[sportId]?.data?.put(score.getEventIdGreaterThanZero().toString(), score)
    }
}

data class EventData(
    var data: HashMap<SportId, EventInfoMap> = HashMap()
)

data class ScoreData(
    var data: HashMap<String, EventScoreDTO> = HashMap()
)

data class EventInfoMap(
    var version: Long = 0,
    var events: HashMap<EventId, EventsDTO> = HashMap()
)

private typealias SportId = Int
private typealias EventId = Int

const val selectedProgramType = 1
const val selectedSportId = 137

fun sortEventsAndSetEventList(): List<SportsBookItem> {
    val events = EventStoreManager().getSportEvents(selectedProgramType, selectedSportId).data.sortedBy {
        it.score == null
    }

    val muks = SportsBookFilterManager.selectedFilter.selectedGroupKey.markets
    val mukSize = muks.size.coerceAtLeast(0)
    val key1: String? = if (mukSize > 0) muks[0] else null
    val key2: String? = if (mukSize > 1) muks[1] else null

    var muk1: String? = null
    var muk2: String? = null
    var sov1: String? = null
    var sov2: String? = null
    key1?.let {
        val muklist1 = key1.split("_")
        muk1 = muklist1.getOrNull(0).ignoreNull("1") + "_" +
                muklist1.getOrNull(1).ignoreNull("1")
        sov1 = muklist1.getOrNull(2)
    }
    key2?.let {
        val muklist2 = key2.split("_")
        muk2 = muklist2.getOrNull(0).ignoreNull("1") + "_" +
                muklist2.getOrNull(1).ignoreNull("1")
        sov2 = muklist2.getOrNull(2)
    }

    val marketLookup1 = MarketConfig.getMarketLookup(muk1.ignoreNull("-"))
    var marketLookup2: MarketLookup? = null
    muk2?.let {
        marketLookup2 = MarketConfig.getMarketLookup(muk2.ignoreNull("-"))
    }


    val marketsLookups: Pair<MarketLookup?, MarketLookup?> =
        Pair(marketLookup1, marketLookup2)

    val coupons = CouponManagerV2.getCouponAsList()

    return events.mapIndexed {index, event->

        val isSelected = coupons.any { it.eventId == event.eventId }

        var market1: MarketItem? = null
        var market2: MarketItem? = null
        market1 = event.markets?.firstOrNull()?.toMarketItem()
        market2 = event.markets?.firstOrNull()?.toMarketItem()
        muk1?.let {
            market1 = event.markets?.firstOrNull()?.toMarketItem()
        }
        muk2?.let {
            market2 = event.markets?.firstOrNull()?.toMarketItem()
            //market2 = event.getMarket(it, sov2)?.toMarketItem()
        }


        val eventItem = event.toEventItem(isSelected = isSelected)
        val markets: Pair<MarketItem?, MarketItem?> = Pair(market1, market2)
        val eventScore = EventStoreManager.eventScores[event.sportId]?.data?.get(event.eventId.toString())?.toEventScoreItem()


        SportsBookItem(
            event = eventItem,
            markets = markets,
            marketLookups = marketsLookups,
            score = eventScore
        )

    }
}

data class SportsBookItem(
    val event: EventItem,
    val markets: Pair<MarketItem?, MarketItem?>,
    val marketLookups: Pair<MarketLookup?, MarketLookup?>,
    val score: EventScoreItem?
)
