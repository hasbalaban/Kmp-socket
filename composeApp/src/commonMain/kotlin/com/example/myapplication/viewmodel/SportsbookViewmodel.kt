package com.example.myapplication.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.model.EventDataInfo
import com.example.model.EventsResponse
import com.example.model.MarketConfigResponse
import com.example.model.MarketResponse
import com.example.model.SportInfo
import com.example.model.SportInfoResponse
import com.example.model.SportsBookUpdateInfo
import com.example.myapplication.manager.MarketConfig
import com.example.myapplication.manager.socket.mainJsonParser
import com.mgmbk.iddaa.manager.EventStoreManager
import com.mgmbk.iddaa.manager.SportsBookItem
import com.mgmbk.iddaa.manager.selectedProgramType
import com.mgmbk.iddaa.manager.selectedSportId
import com.mgmbk.iddaa.manager.sortEventsAndSetEventList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun shouldUpdateScreen(socketUpdateInfo: SportsBookUpdateInfo?): Boolean {
    val sportsId = selectedSportId
    val programType = selectedProgramType

    return socketUpdateInfo?.events?.any {
        EventStoreManager.findEvent(it, sportsId, programType) != null
    } == true
}

sealed interface ListItem {
    data class Event(val sportsBookItem: SportsBookItem) : ListItem
    object Divider : ListItem
}


class SportsbookViewmodel : BaseViewmodel() {

    private val _events = MutableStateFlow<List<ListItem>>(emptyList())
    val events: StateFlow<List<ListItem>> = _events.asStateFlow()


    init {
        listenToSocketUpdates()
    }


    private fun prepareListForUi(events: List<SportsBookItem>): List<ListItem> {
        val listWithDividers = mutableListOf<ListItem>()
        events.forEachIndexed { index, event ->
            listWithDividers.add(ListItem.Event(event))
            if (index < events.lastIndex) {
                listWithDividers.add(ListItem.Divider)
            }
        }
        return listWithDividers
    }

    private fun listenToSocketUpdates() {
        viewModelScope.launch {
            EventStoreManager.socketUpdated
                .filterNotNull() // Başlangıçtaki null değeri atla
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
        programType: Int = selectedProgramType,
        version: Long = 0L,
    ) {
        selectedSportId = sportType
        filterChanged()

        val service = ApiService()
        val eventDataInfo = service.getSportsbookEvents(
            sportType = selectedSportId,
            programType = selectedProgramType,
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
            is NetworkResponse.Error -> {
                println(response.message)
                println(response.code)
                println(response.code)
                println(response.code)
            }

            is NetworkResponse.Loading -> {

            }

            is NetworkResponse.Success -> {
                response.data?.data?.let {
                    MarketConfig.updateSportsBookInfo(it)
                }
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
        val events = sortEventsAndSetEventList()
        val uiList = prepareListForUi(events)

        _events.update {
            uiList
        }
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
}


