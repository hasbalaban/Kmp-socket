package com.example.myapplication.manager


import com.example.model.Events
import com.example.model.LeagueSelectionFilterItem
import com.example.model.ignoreNull
import com.example.myapplication.listextensions.SynchronizedMutableMap

class SportsBookFilterManager {

    companion object {
        var selectedFilter = SelectedFilter()
        var availableLeagues = HashMap<Int, LeagueSelectionFilterItem>()

        fun getAvailableCompetitions(): List<LeagueSelectionFilterItem> =
            availableLeagues.values.sortedBy {
                it.competitionPriority
            }.onEach {
                it.competitionIcon = MarketConfig.getCompetition(it.competitionId).getFlagSuffix().getFlag()
                it.isChecked = selectedFilter.leagueFilter[it.competitionId]?.isChecked.ignoreNull()
            }

    }


        fun getSelectedFilterCount(): Int {
            return selectedFilter.let {
                it.singleMatch.isSelected.value() +
                        it.isFavoriteSelected.value() +
                        it.iskingSelected.value() +
                        it.leagueFilter.isNotEmpty().value() +
                        it.search.searchedText.isNotEmpty().value()
            }
        }

        fun shouldShowHighlightEvents(): Boolean {
            val showHighlight = selectedFilter.let {
                it.singleMatch.isSelected ||
                        it.groupByLeague.isClicked ||
                        it.groupByPercent.isClicked ||
                        it.shouldShowOddPlayingPercent ||
                        it.search.searchedText.isNotEmpty() ||
                        it.isFavoriteSelected ||
                        it.isDuelSelected ||
                        it.iskingSelected ||
                        it.leagueFilter.isNotEmpty()
            }.not()
            return showHighlight
        }

        suspend fun clearFilter() = run {
            availableLeagues.clear()
            selectedFilter = SelectedFilter()
        }

        fun filterChanged(bulletinFilterChoice: BulletinFilterChoice) {
            when (bulletinFilterChoice) {
                is BulletinFilterChoice.SingleMatch -> selectedFilter.singleMatch =
                    bulletinFilterChoice

                is BulletinFilterChoice.GroupByLeague -> {
                    selectedFilter.groupByDate = false
                    selectedFilter.groupByLeague = bulletinFilterChoice
                    selectedFilter.groupByPercent = BulletinFilterChoice.GroupByPercent(false)
                }

                is BulletinFilterChoice.GroupByDate -> {
                    selectedFilter.groupByDate = bulletinFilterChoice.isClicked
                    selectedFilter.groupByLeague = BulletinFilterChoice.GroupByLeague(false)
                    selectedFilter.groupByPercent = BulletinFilterChoice.GroupByPercent(false)
                }

                is BulletinFilterChoice.GroupByPercent -> {
                    selectedFilter.groupByDate = false
                    selectedFilter.groupByLeague = BulletinFilterChoice.GroupByLeague(false)
                    selectedFilter.groupByPercent = bulletinFilterChoice
                }

                is BulletinFilterChoice.ShouldShowOddPlayingPercent -> {
                    selectedFilter.shouldShowOddPlayingPercent = bulletinFilterChoice.isSelected
                }

                is BulletinFilterChoice.Search -> {
                    selectedFilter.search = bulletinFilterChoice
                }

                is BulletinFilterChoice.DuelFilter -> selectedFilter.isDuelSelected =
                    bulletinFilterChoice.isSelected

                is BulletinFilterChoice.FavoriteFilter -> selectedFilter.isFavoriteSelected =
                    bulletinFilterChoice.isSelected

                is BulletinFilterChoice.LeagueFilter -> addOrRemoveLeagueFilter(
                    bulletinFilterChoice.leagueFilter,
                    bulletinFilterChoice.isChecked
                )

                is BulletinFilterChoice.SelectedGroupFilter -> selectedFilter.selectedGroupKey =
                    bulletinFilterChoice.selectedKeys

                is BulletinFilterChoice.ProgramType -> selectedFilter.programType =
                    bulletinFilterChoice.value

                is BulletinFilterChoice.SportId -> selectedFilter.sportId =
                    bulletinFilterChoice.value

                is BulletinFilterChoice.KingFilter -> {
                    selectedFilter.iskingSelected = bulletinFilterChoice.isSelected
                }


            }
        }

        suspend fun updateDateAndLeagueAvailableList(
            event: Events
        ) {
            val competition = MarketConfig.getCompetition(event.competitionId)
            val leagueItem = LeagueSelectionFilterItem(
                competitionId = competition.id,
                competitionIcon = null,
                competitionPriority = competition.priority,
                leagueName = competition.competitionName.ignoreNull(),
                size = availableLeagues[competition.id]?.size.ignoreNull(0) + 1
            )
            availableLeagues[leagueItem.competitionId] = leagueItem
        }

        fun filterEvents(events: List<Events>): List<Events> {
            val searchedText =
                selectedFilter.search.searchedText.transliterateTurkishToEnglish().lowercase()
            var strSearch = ""
            val filtered = events.filter { event ->

                if (searchedText.isNotEmpty()) {
                    strSearch =
                        event.getName() + " - " + MarketConfig.getCompetition(event.competitionId).competitionName.ignoreNull()
                    strSearch = strSearch.transliterateTurkishToEnglish().lowercase()
                }

                ((!selectedFilter.singleMatch.isSelected || (selectedFilter.singleMatch.isSelected && event.minimumBetCount == 1))
                        && (!selectedFilter.iskingSelected || event.kingMbc)
                        && (!selectedFilter.isDuelSelected || event.hasDuel == true)

                        //&& (!selectedFilter.groupByPercent.isClicked || availableEventPercent[event.sportId]?.data?.get(event.eventId.toString()) != null)

                        && (!selectedFilter.isFavoriteSelected
                        && (selectedFilter.leagueFilter.isEmpty() || selectedFilter.leagueFilter[event.competitionId] != null))
                        && (searchedText.isEmpty() || (strSearch.contains(searchedText, ignoreCase = true)))
                        )
            }
            return filtered
        }

    /*
        fun saveOrRemoveFavorite(eventInfo: String, shared: SharedPreferences): Boolean {
            val editor = shared.edit()

            val isContains = favoriteEventIds.contains(eventInfo)
            if (isContains) {
                favoriteEventIds.remove(eventInfo)

                val stringSet = favoriteEventIds.toSet()
                editor.putStringSet(PrefsConstant.FAVORITE_ITEMS, stringSet)
                editor.apply()

                return false
            }

            favoriteEventIds.add(eventInfo)
            val stringSet = favoriteEventIds.toSet()
            editor.putStringSet(PrefsConstant.FAVORITE_ITEMS, stringSet)
            editor.apply()

            return true
        }

        fun removeFavorite(sportId: Int, programType: Int, shared: SharedPreferences) {
            val editor = shared.edit()

            favoriteEventIds = favoriteEventIds.filter {
                it.contains(sportId.toString() + "_" + programType.toString()).not()
            }.toArrayList()

            val stringSet = favoriteEventIds.toSet()
            editor.putStringSet(PrefsConstant.FAVORITE_ITEMS, stringSet)
            editor.apply()
        }

        fun checkFavoriteEvent(eventInfo: String): Boolean {
            return favoriteEventIds.contains(eventInfo)
        }

        fun checkFavoriteFilterToActive(sportId: Int, programType: Int): Boolean {
            return favoriteEventIds.any {
                it.contains(sportId.toString() + "_" + programType.toString())
            }
        }

     */

        private fun addOrRemoveLeagueFilter(
            leagueFilter: LeagueSelectionFilterItem,
            isChecked: Boolean
        ) {

            selectedFilter.leagueFilter.apply {
                if (isChecked) this[leagueFilter.competitionId] = leagueFilter
                else this.remove(leagueFilter.competitionId)
            }
        }


}
        sealed class BulletinFilterChoice() {
            data class SingleMatch(val isSelected: Boolean) : BulletinFilterChoice()
            data class GroupByLeague(val isClicked: Boolean) : BulletinFilterChoice()
            data class GroupByDate(val isClicked: Boolean) : BulletinFilterChoice()
            data class GroupByPercent(val isClicked: Boolean) : BulletinFilterChoice()
            data class ShouldShowOddPlayingPercent(val isSelected: Boolean) : BulletinFilterChoice()
            data class Search(val searchedText: String) : BulletinFilterChoice()
            data class KingFilter(var isSelected: Boolean) : BulletinFilterChoice()

            data class LeagueFilter(
                var leagueFilter: LeagueSelectionFilterItem,
                val isChecked: Boolean
            ) : BulletinFilterChoice()

            data class SelectedGroupFilter(var selectedKeys: SelectedGroupInfo) :
                BulletinFilterChoice()

            data class ProgramType(var value: Int) : BulletinFilterChoice()
            data class SportId(var value: Int) : BulletinFilterChoice()
            data class DuelFilter(var isSelected: Boolean) : BulletinFilterChoice()
            data class FavoriteFilter(var isSelected: Boolean) : BulletinFilterChoice()
        }


        data class SelectedGroupInfo(
            val markets: List<String> = listOf(),
            val groupId: Int = -1,
            val subGroupId: Int = -1,
            val specialOddValue: Double = 0.0
        )

        data class SelectedFilter(
            var singleMatch: BulletinFilterChoice.SingleMatch = BulletinFilterChoice.SingleMatch(
                false
            ),
            var groupByLeague: BulletinFilterChoice.GroupByLeague = BulletinFilterChoice.GroupByLeague(
                false
            ),
            var groupByDate: Boolean = true,
            var groupByPercent: BulletinFilterChoice.GroupByPercent = BulletinFilterChoice.GroupByPercent(
                false
            ),
            var shouldShowOddPlayingPercent: Boolean = false,
            var search: BulletinFilterChoice.Search = BulletinFilterChoice.Search(""),
            var leagueFilter: HashMap<Int, LeagueSelectionFilterItem> = hashMapOf(),
            var iskingSelected: Boolean = false,
            var selectedGroupKey: SelectedGroupInfo = SelectedGroupInfo(),
            var programType: Int = 1,
            var sportId: Int = 1,
            var isDuelSelected: Boolean = false,
            var isFavoriteSelected: Boolean = false
        )


        fun Boolean.value(): Int {
            return if (this) 1 else 0
        }

        data class EventPercentStore<T>(
            val expiredTime: Long,
            val data: HashMap<String, T>
        )



fun String.transliterateTurkishToEnglish(): String {
    val turkishChars = hashMapOf(
        'ç' to 'c', 'Ç' to 'C',
        'ğ' to 'g', 'Ğ' to 'G',
        'ı' to 'i', 'İ' to 'I',
        'ö' to 'o', 'Ö' to 'O',
        'ş' to 's', 'Ş' to 'S',
        'ü' to 'u', 'Ü' to 'U'
    )

    val result = StringBuilder(length)
    forEach { char ->
        val replacement = turkishChars[char]
        if (replacement != null) {
            result.append(replacement)
        } else {
            result.append(char)
        }
    }
    return result.toString()
}