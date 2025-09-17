package com.example.myapplication.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.model.EventDataInfo
import com.example.model.EventsResponse
import com.example.model.MarketConfigResponse
import com.example.model.MarketResponse
import com.example.myapplication.manager.MarketConfig
import com.example.myapplication.manager.socket.mainJsonParser
import com.mgmbk.iddaa.manager.EventStoreManager
import com.mgmbk.iddaa.manager.SportsBookItemDTO
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
import kotlinx.coroutines.launch


class SportsbookViewmodel : BaseViewmodel() {
    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter

    private val _events = MutableStateFlow<List<SportsBookItemDTO>>(emptyList())
    val events: StateFlow<List<SportsBookItemDTO>> = _events

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                //_counter.emit(counter.value + 1)
            }
        }
    }


    suspend fun getEvents(
        sportType: Int = selectedSportId,
        programType: Int = selectedProgramType,
        version: Long = 0L,
    ) {
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

    suspend fun filterChanged() {
        val events = sortEventsAndSetEventList()
        _events.emit(events)
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
}


