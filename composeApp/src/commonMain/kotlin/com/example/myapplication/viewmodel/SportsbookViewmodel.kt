package com.example.myapplication.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.model.CompetitionsResponse
import com.example.model.EventDataInfo
import com.example.model.EventsResponse
import com.example.model.MarketConfigResponse
import com.example.model.MarketResponse
import com.example.model.SportInfoResponse
import com.example.model.SportsBookUpdateInfo
import com.example.myapplication.manager.MarketConfig
import com.example.myapplication.manager.socket.mainJsonParser
import com.example.myapplication.manager.EventStoreManager
import com.example.myapplication.manager.LiveSportsBookItem
import com.example.myapplication.manager.SpecialEventGroupItem
import com.example.myapplication.manager.PreSportsBookItem
import com.example.myapplication.manager.SpecialEventOutComesItem
import com.example.myapplication.manager.SpecialEventTitleItem
import com.example.myapplication.manager.SportsBookFilterManager
import com.example.myapplication.manager.SportsBookUiHandler
import com.example.myapplication.manager.SportsbookTitleItem
import com.example.myapplication.manager.TitleItem
import com.example.myapplication.manager.UpComingEventItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun shouldUpdateScreen(socketUpdateInfo: SportsBookUpdateInfo?): Boolean {
    val sportsId = SportsBookFilterManager.selectedFilter.sportId
    val programType = SportsBookFilterManager.selectedFilter.programType

    return socketUpdateInfo?.events?.any {
        EventStoreManager.findEvent(it, sportsId, programType) != null
    } == true
}

sealed interface ListItem {
    data class Title(val item: TitleItem) : ListItem
    data class PreEvent(val item: PreSportsBookItem) : ListItem
    data class LiveEvent(val item: LiveSportsBookItem) : ListItem
    data class UpComingEvent(val item: UpComingEventItem) : ListItem
    data class SportsbookTitle(val item: SportsbookTitleItem) : ListItem
    data class SpecialEventGroup(val item: SpecialEventGroupItem) : ListItem
    data class SpecialEventTitle(val item: SpecialEventTitleItem) : ListItem
    data class SpecialEventOutcome(val item: SpecialEventOutComesItem) : ListItem
}


class SportsbookViewmodel : BaseViewmodel() {

    private val _events = MutableStateFlow<List<ListItem>>(emptyList())
    val events: StateFlow<List<ListItem>> = _events.asStateFlow()


    init {
        listenToSocketUpdates()
    }


    private fun listenToSocketUpdates() {
        viewModelScope.launch {
            EventStoreManager.socketUpdated
                .filterNotNull()
                .collect { updateInfo ->
                    // shouldUpdateScreen mantığı artık burada
                    if (shouldUpdateScreen(updateInfo) && updateInfo.events.isNotEmpty()) {
                        println("Socket güncellendi (DEBOUNCED), filterChanged() tetikleniyor.")
                        filterChanged()
                    }
                }
        }

    }


    suspend fun getEvents(
        sportType: Int,
        programType: Int,
        version: Long = 0L,
    ) {
        SportsBookFilterManager.selectedFilter.sportId = sportType
        SportsBookFilterManager.selectedFilter.programType = programType

        filterChanged()

        val service = ApiService()
        val eventDataInfo = service.getSportsbookEvents(
            sportType = sportType,
            programType = programType,
            version = version
        )

        when (eventDataInfo) {
            is NetworkResponse.Error -> {
                println(eventDataInfo.message)
                println(eventDataInfo.code)
                println(eventDataInfo.code)
                println(eventDataInfo.code)
            }

            is NetworkResponse.Loading -> {

            }

            is NetworkResponse.Success -> {
                eventDataInfo.data?.let {
                    saveOrUpdateEventData(
                        programType = programType,
                        sportType = sportType,
                        eventDataInfo = it
                    )
                }

                filterChanged()
            }
        }


    }

    suspend fun getMarketConfig() {
        val service = ApiService()
        val response = service.getMarketConfig()

        when (response) {
            is NetworkResponse.Error -> {
                println(response.message)
                println(response.code)
                println(response.code)
                println(response.code)
            }

            is NetworkResponse.Loading -> {

            }

            is NetworkResponse.Success -> {
                response.data?.let {
                    MarketConfig.updateMarketConfigs(it)
                }
                filterChanged()
            }
        }


    }

    suspend fun getSportInfo() {
        val service = ApiService()
        val response = service.getSportInfo()

        when (response) {
            is NetworkResponse.Error -> {}
            is NetworkResponse.Loading -> {}
            is NetworkResponse.Success -> {
                response.data?.data?.let {
                    MarketConfig.updateSportsBookInfo(it)
                }
            }
        }


    }

    suspend fun getCompetitions() {
        val service = ApiService()
        val response = service.getCompetitions()

        when (response) {
            is NetworkResponse.Error -> {}
            is NetworkResponse.Loading -> {}
            is NetworkResponse.Success -> {
                response.data?.data?.let { MarketConfig.updateCompetitions(it) }
            }
        }


    }

    private fun saveOrUpdateEventData(
        programType: Int,
        sportType: Int,
        eventDataInfo: EventDataInfo
    ) {
        val eventStoreManager = EventStoreManager()

        eventStoreManager.addOrUpdateSportEvents(
            programType = programType,
            sportId = sportType,
            eventDataInfo = eventDataInfo
        )
    }

    fun filterChanged() {
        val uiList = SportsBookUiHandler().getSportsbookUiList()
        _events.value = uiList
    }


}


sealed class NetworkResponse<out T> {
    data class Success<T>(val data: T) : NetworkResponse<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResponse<Nothing>()
    data class Loading (val isLoading : Boolean): NetworkResponse<Nothing>()

}



class ApiService() {

    val client = HttpClient {
        // 1. ContentNegotiation: JSON'u otomatik olarak veri sınıflarımıza (User) çevirir
        install(ContentNegotiation) {
            json(mainJsonParser)
        }
    }


    private val baseUrl = "https://sportsbookV2.iddaa.com"

    suspend fun getSportsbookEvents(
        sportType: Int? = null,
        programType: Int,
        version: Long
    ): NetworkResponse<EventDataInfo?> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/sportsbook/events") {
                // @Query anotasyonlarının karşılığı budur.
                // Ktor, bu parametreleri URL'ye otomatik olarak ekler:
                // .../events?type=...&version=...

                parameter("type", programType)
                parameter("version", version)

                // Parametre null değilse ekle
                sportType?.let {
                    parameter("st", it)
                }
            }

            NetworkResponse.Success(data = (response.body() as? EventsResponse)?.data)

        } catch (e: Exception) {
            // Hata durumunda NetworkResponse.Error olarak sarmala
            println("Ağ hatası: ${e.message}")
            NetworkResponse.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }

    suspend fun getMarketConfig(): NetworkResponse<MarketResponse?> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/sportsbook/get_market_config")
            NetworkResponse.Success(data = (response.body() as? MarketConfigResponse)?.data)

        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }
    suspend fun getSportInfo(): NetworkResponse<SportInfoResponse?> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/sportsbook/info")
            NetworkResponse.Success(data = (response.body() as? SportInfoResponse))

        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }
    suspend fun getCompetitions(): NetworkResponse<CompetitionsResponse?> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/sportsbook/competitions")
            NetworkResponse.Success(data = (response.body() as? CompetitionsResponse))

        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }

}


