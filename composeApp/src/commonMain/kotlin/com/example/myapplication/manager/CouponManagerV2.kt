package com.example.myapplication.manager


import com.example.model.BetItems
import com.example.model.BetslipEvent
import com.example.model.CouponItem
import com.example.model.CouponItemData
import com.example.model.CouponSettings
import com.example.model.CreateBetOrderRequestModel
import com.example.model.MarketByIdRequestBody
import com.example.model.SportsBookUpdateInfo
import com.example.model.SystemCalculationItem
import com.example.model.ignoreNull
import com.example.myapplication.moneyformatter.formatMoney
import com.example.myapplication.moneyformatter.toKmpBigDecimal
import com.mgmbk.iddaa.manager.EventStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min


class CouponManagerV2 {
    companion object {
        //TODO : HashMap must be ConcurrentHashMap
        val eventItems = MutableStateFlow<HashMap<Int, CouponItem>>(hashMapOf())
        val columnCount = MutableStateFlow<Long>(0)

        val selectedSystems = mutableListOf<Int>()
        val selectedSystemsToApply = mutableListOf<Int>()

        var couponCount = 1
        var multiplier = 50
        var updatedItemCount = MutableStateFlow<Int>(0)

        var lastCalculation: CouponCalculation? = null

        private fun getSystemOptions(eventCount: Int, bankCount: Int): IntArray {
            val maxColumns = 12_500_000
            val options = IntArray(eventCount){ it + 1 }
            return options.filter { combinations(eventCount - bankCount, it) <= maxColumns }.toIntArray()
        }

        private fun combinations(n: Int, k: Int): Long {
            if (k > n) return 0
            return factorial(n) / (factorial(k) * factorial(n - k))
        }


        private fun factorial(num: Int): Long {
            var fact = 1L
            if (num > 1) {
                for (i in 1..num)
                    fact *= i
            }
            return fact
        }

        private fun calculateCombination(n: Int, r: Int): Long {
            if (n < r)
                return 0
            return factorial(n) / ((factorial(r) * factorial(n - r)))
        }

        fun updateColumnCount() {
            var columns = 0L
            selectedSystemsToApply.forEach { system ->
                val bankCount = eventItems.value?.filter { it.value.isBank }?.size ?: 0
                val notBankCount = eventItems.value?.filter { !it.value.isBank }?.size ?: 0
                columns += calculateCombination(notBankCount, system - bankCount)
            }
            columnCount.value = columns
        }

        fun resetBetslip(removeEvents: Boolean = true) {
            couponCount = 1
            multiplier = RemoteConfigs.minCouponPrice
            selectedSystems.clear()
            if (removeEvents) {
                resetCouponEvents()
            }
        }

        fun getSystemOptionsList(): List<Int> {
            eventItems.value?.let { items ->
                //val maxMbs = items.maxOf { it.value.market.mbc ?: 1 }
                val maxMbs = items.maxOf { it.value.itemData?.couponItemData?.second?.mbc ?: 1 }
                if(items.isNotEmpty()) {
                    //val list = systemOptions[items.size-1].toMutableList()
                    val list = getSystemOptions(items.size, (items.filter { it.value.isBank }).size).toMutableList()
                    list.removeAll { it < max(maxMbs, (items.filter { it.value.isBank }).size) }
                    return list
                }
            }
            return listOf()
        }

        fun getSystemsString(analytics: Boolean = false): String {
            var sysStr = ""
            val system = mutableListOf<Int>()
            system.addAll(selectedSystems)

            system.sorted().forEach { sysStr += "$it," }
            if (sysStr.isNotEmpty() && sysStr.last() == ',')
                return sysStr.dropLast(1)
            return if (analytics) "" else "Seçiniz"
        }

        fun getSystemsStringFromList(system: List<Int>): String {
            var sysStr = ""
            system.sorted().forEach { sysStr += "$it," }
            return if(sysStr.isNotEmpty() && sysStr.last() == ',')
                sysStr.dropLast(1)
            else ""
        }
        fun addOrRemoveItem(couponItem: CouponItem) {
            eventItems.value?.let { couponItems ->
                val eventId = couponItem.eventId

                val couponItemData = couponItem.itemData?.couponItemData
                val event = couponItemData?.first
                val market = couponItemData?.second
                val outcome = couponItemData?.third

                couponItems[eventId]?.let {
                    couponItems.remove(eventId)
                    if (it.outcomeNo != couponItem.outcomeNo || it.marketId != couponItem.marketId) {
                        couponItems[eventId] = couponItem
                    }

                } ?: run {
                    couponItems[eventId] = couponItem
                }

                removeInproperSystems()
                if (couponItems.isEmpty()) {
                    resetBetslip()
                }

                eventItems.tryEmit(couponItems)

                val updateInfo = SportsBookUpdateInfo(1, arrayListOf(couponItem.eventId))
                EventStoreManager.setSocketUpdated(updateInfo)
            }
        }

        fun removeItem(eventId: Int) {
            eventItems.value?.let { couponItems ->
                couponItems.remove(eventId)
                eventItems.tryEmit(couponItems)
            }
        }

        fun eventsRemoved(eventIds: List<Int>) {
            eventItems.value?.let { couponItems ->
                eventIds.forEach { eventId ->
                    couponItems[eventId]?.let {
                        it.isRemoved = true
                    }
                }
                eventItems.value?.let { eventItems.tryEmit(it) }
            }
        }

        fun triggerRefreshCoupon() {
            eventItems.value?.let { eventItems.tryEmit(it) }
        }

        fun removeInproperSystems() {
            selectedSystems.removeAll { it > (eventItems.value?.size ?: 20) }
            val minSystem = getCouponAsList().filter { it.isBank }.size
            selectedSystems.removeAll { it < minSystem }
            triggerRefreshCoupon()
        }

        fun resetCouponEvents() {
            eventItems.value = HashMap()
        }

        fun updateSocketEvents(ids: List<Int>) {
            eventItems.value?.let { couponItems ->
                val updatedItems: MutableMap<Int, CouponItem> = mutableMapOf()

                couponItems.forEach {
                    val itemData = it.value.getCouponItemData()
                    val isUpdated =
                        ids.contains(it.value.eventId) && itemData.third?.prevOddIsDifferent() == true && itemData.third?.odd != it.value.oddPrev

                    it.value.oddPrev = itemData.third?.odd ?: 0.0

                    if (isUpdated) {
                        it.value.itemData = CouponItemData(itemData)
                        updatedItems[it.key] = it.value
                    }
                }

                updatedItemCount.value = updatedItems.size
                eventItems.tryEmit(couponItems)
            }
        }

        fun getCouponAsList(): List<CouponItem> {
            val list = mutableListOf<CouponItem>()
            eventItems.value?.let { couponItems ->
                couponItems.forEach { couponItem ->
                    list.add(couponItem.value)
                }
            }
            return list.sortedBy { it.editDate }
        }

        private fun getCouponAsListCopy(): List<CouponItem> {
            val list = mutableListOf<CouponItem>()
            eventItems.value?.let { couponItems ->
                couponItems.forEach { couponItem ->
                    list.add(couponItem.value.copy())
                }
            }
            return list.sortedBy { it.editDate }
        }

        private fun getCouponMaxOddAndWood(systemSubCoupon: List<CouponItem>? = null): MaxCouponValues {
            val couponList = systemSubCoupon ?: getCouponAsList()
            var rateTotal = 1.0
            var rateWood = 1.0
            couponList.forEach {
                val outcome = it.itemData?.couponItemData?.third
                rateTotal *= (outcome?.odd ?: 1.0)
                rateWood *= (outcome?.webOdd ?: outcome?.odd ?: 1.0)
            }
            return MaxCouponValues(maxOdd = rateTotal, maxWodd = rateWood)
        }

        fun getRateButtonData(): Pair<Int, Double> {
            var totalOdd = getCouponMaxOddAndWood().maxOdd
            if(totalOdd == 1.0)
                totalOdd = 0.0
            totalOdd = min(totalOdd, (RemoteConfigs.maxCouponAmount.toDouble()))
            return Pair(eventItems.value?.size.ignoreNull(), totalOdd)
        }

        fun getCreateBetOrderRequest(settings: CouponSettings): CreateBetOrderRequestModel? {
            lastCalculation?.let { couponCalculation ->
                val events = mutableListOf<BetslipEvent>()
                eventItems.value?.let { items ->
                    items.forEach { item ->
                        events.add(toBetslipEvent(item.value))
                    }
                }

                return CreateBetOrderRequestModel(
                    acceptLowers = settings.acceptLower,
                    acceptHighers = settings.acceptHigher,
                    useBonus = settings.bonus,
                    selectedSystems = selectedSystems,
                    multiplier = multiplier,
                    couponCount = couponCount,
                    maxOdd = couponCalculation.totalOdd.formatMoney(),
                    amount = couponCalculation.getTotalAmount().toDouble().formatMoney(),
                    maxWinning = (couponCalculation.maxWinning / couponCount).formatMoney(),
                    maxKingWinning = (couponCalculation.maxWinningWodd / couponCount).formatMoney(),
                    events = events
                )
            } ?: run { return null }
        }

        private fun toBetslipEvent(couponItem: CouponItem): BetslipEvent {
            val data = couponItem.itemData?.couponItemData
            return BetslipEvent(
                eventId = data?.first?.eventId ?: -1,
                eventVersion = data?.first?.eventVersion?.toInt() ?: -1,
                marketId = data?.second?.marketId?.toInt() ?: -1,
                marketVersion = data?.second?.version?.toInt() ?: -1,
                outComeNo = data?.third?.outcomeNo ?: -1,
                odd = data?.third?.odd ?: 1.0,
                isBanker = couponItem.isBank,
                marketName = couponItem.getMarketName(false),
                competitionName = CouponItem.getCompetitionName(data?.first?.competitionId ?: -1)
            )
        }

        fun isInCoupon(eventId: Int, marketId: Long, outcomeNo: Int): Boolean {
            eventItems.value?.let { items ->
                items[eventId]?.let {
                    return (it.marketId == marketId.toInt() && it.outcomeNo == outcomeNo)
                }
            }
            return false
        }

        fun isInCoupon(eventId: Int): Boolean {
            eventItems.value?.let { items ->
                return items.any { it.value.eventId == eventId }
            }
            return false
        }

        private fun calculateSystemsList(): SystemCalculationItem {
            val systemCalculationItem = SystemCalculationItem()
            val allItems = getCouponAsListCopy()
            selectedSystems.forEach {
                calculateSystem(it, systemCalculationItem, allItems)
            }
            return systemCalculationItem
        }

        private fun calculateSystem(
            system: Int,
            systemCalculationItem: SystemCalculationItem,
            items: List<CouponItem>
        ) {
            val bankItems = items.filter { it.isBank }
            val systemItems = items.filter { !it.isBank }

            combinationsHelper(
                systemItems,
                bankItems,
                system - bankItems.size,
                0,
                mutableListOf(),
                systemCalculationItem
            )
        }

        private fun combinationsHelper(
            items: List<CouponItem>,
            bankItems: List<CouponItem>,
            size: Int,
            start: Int,
            current: MutableList<CouponItem>,
            systemCalculationItem: SystemCalculationItem
        ) {
            if (size == 0) {
                current.addAll(bankItems)
                val totalData = getCouponMaxOddAndWood(current)
                systemCalculationItem.systemTotalOdd += totalData.maxOdd
                systemCalculationItem.systemTotalWodd += totalData.maxWodd
                systemCalculationItem.systemTotalColumn += 1
                current.removeAll(bankItems)
                return
            }

            for (i in start until items.size) {
                current.add(items[i])
                combinationsHelper(
                    items,
                    bankItems,
                    size - 1,
                    i + 1,
                    current,
                    systemCalculationItem
                )
                current.removeAt(current.size - 1)
            }
        }

        fun calculateCoupon(): CouponCalculation {
            var totalOdd = 0.0
            var totalWodd = 0.0
            var totalColumns = 1L
            val totalEvents = eventItems.value?.size.ignoreNull()

            if (selectedSystems.isNotEmpty()) {
                val systemCalculation = calculateSystemsList()
                totalOdd = systemCalculation.systemTotalOdd
                totalWodd = systemCalculation.systemTotalWodd
                totalColumns = systemCalculation.systemTotalColumn
            }
            else {
                val totalData = getCouponMaxOddAndWood()
                totalOdd = totalData.maxOdd
                totalWodd = totalData.maxWodd
            }

            val maxCouponAmount = RemoteConfigs.maxCouponAmount.toDouble()
            if (totalOdd > maxCouponAmount) totalOdd = maxCouponAmount
            if (totalWodd > maxCouponAmount) totalWodd = maxCouponAmount

            var maxWinning = totalOdd * multiplier
            if (maxWinning > RemoteConfigs.maxCouponAmount.toDouble()) maxWinning =
                RemoteConfigs.maxCouponAmount.toDouble()
            var maxWinningWodd = totalWodd * multiplier
            if (maxWinningWodd > RemoteConfigs.maxCouponAmount.toDouble()) maxWinningWodd =
                RemoteConfigs.maxCouponAmount.toDouble()
            val kingWinning = maxWinning - maxWinningWodd

            val maxMbs = eventItems.value?.maxOfOrNull { it.value.itemData?.couponItemData?.second?.mbc ?: 1 } ?: 1
            val eventCount = totalEvents

            val calculation = CouponCalculation(
                totalOdd = totalOdd,
                totalWodd = totalWodd,
                totalColumns = totalColumns,
                couponCount = couponCount,
                totalEvents = totalEvents,
                multiplier = multiplier,
                maxWinning = maxWinning * couponCount,
                maxWinningWodd = maxWinningWodd * couponCount,
                kingWinning = kingWinning,
                mbsDiff = maxMbs - eventCount,
            )
            lastCalculation = calculation

            return calculation
        }

        fun toBetItems(couponItem: CouponItem): BetItems {
            val data = couponItem.itemData?.couponItemData
            val event = data?.first
            val market = data?.second
            val outcome = data?.third

            return BetItems(
                (event?.bettingPhase ?: -999) + 1,
                outcome?.outcomeNo,
                outcome?.name,
                couponItem.eventId,
                event?.eventVersion?.toInt(),
                event?.getName(),
                CouponItem.getCompetitionName(event?.competitionId ?: -1), // lig adı
                event?.sportId,
                event?.eventDate,
                outcome?.odd?.toKmpBigDecimal(),
                outcome?.webOdd?.toKmpBigDecimal(),
                market?.mbc,
                couponItem.marketId,
                market?.version?.toInt(),
                couponItem.getMarketName(false),
                couponItem.isBank,
                couponItem.marketId.toString(),
                outcome?.odd.toString(),
                couponItem.outcomeNo.toString(),
                event?.isLive,
                null,
                "",
                "",
                "${couponItem.marketId}_${couponItem.outcomeNo}",
                "",
                iskbet = event?.kingOdds,
                kbodd = event?.kingOdds,
                kblive = event?.isKingLive,
                kbmbs = event?.kingMbc,
                skbet = 1,
                bid = event?.betRadarId?.toInt()
            )
        }


        fun toMarketByIdRequestBody(couponItem: CouponItem): MarketByIdRequestBody {
            return MarketByIdRequestBody(
                eventId = couponItem.eventId,
                marketId = couponItem.marketId,
                outComeNo = couponItem.outcomeNo
            )
        }

        fun getMarketByIdRequest(): ArrayList<MarketByIdRequestBody> {
            val items = getCouponAsList()
            val marketByIdItems = arrayListOf<MarketByIdRequestBody>()
            items.forEach {
                marketByIdItems.add(toMarketByIdRequestBody(it))
            }
            return marketByIdItems
        }

        fun hasBankEvent(): Boolean {
            eventItems.value?.let { couponItem ->
                return couponItem.any { it.value.isBank }
            }
            return false
        }

    }
}

class RemoteConfigs {
    companion object {
        val minCouponPrice = 50
        val maxCouponAmount = 20

    }
}

@Serializable
data class MaxCouponValues(
    val maxOdd : Double,
    val maxWodd : Double
)
