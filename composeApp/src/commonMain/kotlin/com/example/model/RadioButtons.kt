package com.example.model

import kotlinx.serialization.Serializable


@Serializable
data class DateSelectionFilterItem(
    var isChecked: Boolean = false,
    val dateStamp: Long,
    val formattedDate: String,
    val size : Int
) : RadioButtonItem()

@Serializable
data class MarketGroupsAdapterItem(
    val groupId: Int,
    val name: String,
) : RadioButtonItem()

@Serializable
data class SportsBookFilterItem(
    val sortBy: SportsBookSortBy,
    val sortByText : String,
) : RadioButtonItem()

enum class SportsBookSortBy{
    Date, Competition, Percent
}

@Serializable
open class RadioButtonItem{
    var isSelected: Boolean = false
}

@Serializable
data class InformationSelectedItem(
    val category: String
):RadioButtonItem()

@Serializable
data class  DialogChangeSelectedItem (
    val name: String,
    var pageType : InformationPageType,
    var isSelected: Boolean
)

enum class InformationPageType{
    PASSWORD, PERSONAL_INFORMATION
}

