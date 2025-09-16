package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompetitionsResponse(
    val isSuccess: Boolean?,
    val data: List<Competition>?,
    val message: String?,
    val error: String?,
    val info: String?,
    val dateTime: String?
)

@Serializable
data class Competition(
    @SerialName("i") val id: Int,
    @SerialName("cid") val countryId: String?,
    @SerialName("n") val competitionName: String?,
    @SerialName("p") val priority: Int
) {
    fun getFlagSuffix():String{
        return countryId?.lowercase().toString()
    }
}



