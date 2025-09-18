package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.EventScoreItem
import com.example.model.MarketLookup
import com.example.model.OutComesItem
import com.example.model.ScoreType
import com.example.model.SportTypeEnum
import com.example.model.SportsBookUpdateInfo
import com.example.model.ignoreNull
import com.example.myapplication.manager.MarketConfig
import com.example.myapplication.viewmodel.ListItem
import com.example.myapplication.viewmodel.SportsbookViewmodel
import com.mgmbk.iddaa.manager.EventStoreManager
import com.mgmbk.iddaa.manager.selectedProgramType
import com.mgmbk.iddaa.manager.selectedSportId
import org.koin.compose.viewmodel.koinViewModel


private fun shouldUpdateScreen(socketUpdateInfo: SportsBookUpdateInfo?): Boolean {
    val sportsId = selectedSportId
    val programType = selectedProgramType

    return socketUpdateInfo?.events?.any {
        EventStoreManager.findEvent(it, sportsId, programType) != null
    } == true
}

@Composable
fun SportsbookScreen(viewModel: SportsbookViewmodel = koinViewModel()) {

    val eventList by viewModel.events.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMarketConfig()
        viewModel.getSportInfo()
    }

    Column{
        SportNameTabs()

        LazyColumn(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        itemsIndexed(
            items = eventList,
            key = { index, item ->
                when (item) {
                    is ListItem.Event -> "event_${item.sportsBookItem.event.eventId}"
                    is ListItem.Divider -> "divider_$index"
                }
            },
            contentType = { _, item ->
                when (item) {
                    is ListItem.Event -> "event"
                    is ListItem.Divider -> "divider"
                }
            }
        ) { index, item -> // 'itemContent' de artık hem 'index' hem de 'item' alır
            when (item) {
                is ListItem.Event -> {
                    Column(
                        modifier = Modifier.height(100.dp).fillMaxWidth(),
                    ) {
                        SportsbookItemHeader(
                            mbs = item.sportsBookItem.event.minimumBetCount,
                            homeTeamName = item.sportsBookItem.event.homeTeamName,
                            awayTeamName = item.sportsBookItem.event.awayTeamName,
                            score = item.sportsBookItem.event.score,
                            kingCount = item.sportsBookItem.event.kingCount,
                            minute = item.sportsBookItem.score?.minute
                        )
                        SportsbookMarketList(
                            item.sportsBookItem.markets.first?.outComes,
                            item.sportsBookItem.marketLookups.first,
                            item.sportsBookItem.event.oddCount.toString(),
                        )
                    }
                }
                is ListItem.Divider -> HorizontalDivider()
            }
        }
    }

    }

}


@Composable
fun SportNameTabs(
    viewModel: SportsbookViewmodel = koinViewModel()
) {
    val sportList by MarketConfig.sportsBookInfo.collectAsState()
    var selectedSportId: Int? by remember {
        mutableStateOf(null)
    }
    val selectedTabIndex = remember(selectedSportId) {
        sportList.indexOfFirst { it.sportId == selectedSportId }
    }

    LaunchedEffect(sportList) {
        if (sportList.isNotEmpty()) {
            selectedSportId = sportList.firstOrNull()?.sportId
        }
    }

    LaunchedEffect(selectedSportId) {
        selectedSportId?.let {
            viewModel.getEvents(it)
        }
    }

    selectedSportId?.let {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            divider = {
                HorizontalDivider(color = Color.Gray)
            },
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = Color.Yellow
                )
            },
            containerColor = Color(0xff008641),
            contentColor = Color.White
        ) {
            // 2. Sekmeleri bir döngü ile oluştur
            sportList.forEachIndexed { index, sportInfo ->
                Tab(
                    // 3. Bu sekmenin seçili olup olmadığını belirle
                    selected = sportInfo.sportId == selectedSportId,
                    // 4. Sekmeye tıklandığında ne olacağını belirle
                    onClick = {
                        selectedSportId = sportInfo.sportId
                    },
                    // 5. Sekmenin içeriği
                    text = {
                        val sportTypeName =
                            MarketConfig.marketConfig.sportType?.get(sportInfo.sportId.toString())?.sportTypeName?.ignoreNull("-")

                        Text(text = sportTypeName.ignoreNull(""), fontSize = 20.sp, color = Color.White)
                    },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.LightGray
                )
            }
        }
    }


}

@Composable
private fun SportsbookItemHeader(
    mbs: Int,
    homeTeamName: String?,
    awayTeamName: String?,
    score: EventScoreItem?,
    kingCount : Int,
    minute : String?,
) {

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).background(Color(50, 100,100).copy(alpha = 0.1f)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MbsIcon(mbs, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.height(52.dp).weight(1f)) {
            Text(
                homeTeamName.toString(),
                color = Color.Black,
                fontSize = 16.sp
            )
            Text(
                awayTeamName.toString(),
                color = Color.Black,
                fontSize = 16.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            Column(modifier = Modifier.fillMaxHeight()) {
                val homeScore = score?.getStrScoreByType(true,ScoreType.CURRENT)
                val awayScore = score?.getStrScoreByType(false,ScoreType.CURRENT)
                Text(
                    homeScore.ignoreNull("-"),
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    awayScore.ignoreNull("-"),
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }

            VerticalDivider(thickness = 1.dp)

            Column(modifier = Modifier.height(52.dp), verticalArrangement = Arrangement.Bottom) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)){
                    if (kingCount > 0){
                        KingIcon(modifier = Modifier.size(20.dp))
                    }
                    if (kingCount > 0){
                        KingIcon(modifier = Modifier.size(20.dp))
                    }
                    if (kingCount > 0){
                        KingIcon(modifier = Modifier.size(20.dp))
                    }
                }
                val minute = if (minute.isNullOrBlank()) "-" else minute + "'"
                Text(
                    minute,
                    color = Color(0xff008641),
                    fontSize = 12.sp,
                    modifier = Modifier.paddingFromBaseline(0.dp, 0.dp).padding(vertical = 4.dp).height(20.dp).clip(RoundedCornerShape(20)).background(Color(0xffd5e1e5))
                        .padding(horizontal = 20.dp).padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

@Composable
private fun SportsbookMarketList(
    outComes: List<OutComesItem>?,
    marketLookup: MarketLookup?,
    oddCount: String
) {
    Row(modifier = Modifier.fillMaxWidth().height(50.dp).padding(start = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SportsbookMarketItem(outComes?.getOrNull(0), marketLookup, modifier = Modifier.weight(1f))
        SportsbookMarketItem(outComes?.getOrNull(1), marketLookup, modifier = Modifier.weight(1f))
        SportsbookMarketItem(outComes?.getOrNull(2), marketLookup, modifier = Modifier.weight(1f))
        Text(text = "+$oddCount", color = Color.White, fontSize = 14.sp, modifier = Modifier.fillMaxHeight().background(color = Color(0xff171717)).padding(14.dp))
    }
}

@Composable
fun SportsbookMarketItem(
    outComesItem: OutComesItem?,
    marketLookup: MarketLookup?,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().height(50.dp).padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            outComesItem?.name.ignoreNull("-"),
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().height(20.dp).background(color = Color.DarkGray),
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier)

        Text(
            if (outComesItem?.odd.ignoreNull(-1.0) >= 0) outComesItem?.odd.toString() else "-",
            color = Color.Black,
            fontSize = 12.sp,
            modifier = Modifier,
        )

    }
}





