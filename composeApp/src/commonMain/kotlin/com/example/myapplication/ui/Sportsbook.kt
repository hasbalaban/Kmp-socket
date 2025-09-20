package com.example.myapplication.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CouponItem
import com.example.model.EventScoreItem
import com.example.model.MarketLookup
import com.example.model.OutComesItem
import com.example.model.ScoreType
import com.example.model.ignoreNull
import com.example.myapplication.manager.CouponManagerV2
import com.example.myapplication.manager.MarketConfig
import com.example.myapplication.viewmodel.ListItem
import com.example.myapplication.viewmodel.SportsbookViewmodel
import com.example.myapplication.manager.SportsBookFilterManager
import kotlinx.collections.immutable.ImmutableList
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.ic_mbs_1_live
import myapplication.composeapp.generated.resources.ic_mbs_2_live
import myapplication.composeapp.generated.resources.ic_mbs_3_live
import myapplication.composeapp.generated.resources.king_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SportsbookScreen(viewModel: SportsbookViewmodel = koinViewModel()) {

    val eventList by viewModel.events.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMarketConfig()
        viewModel.getCompetitions()
        viewModel.getSportInfo()
    }

    Column{
        SportNameTabs()

        LazyColumn(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
        itemsIndexed(
            items = eventList,
            key = { index, item ->
                when (item) {
                    is ListItem.PreEvent -> "PreEvent_${item.item.event.eventId}"
                    is ListItem.LiveEvent -> "LiveEvent_$index"
                    is ListItem.SpecialEventGroup -> "SpecialEventGroup_$index"
                    is ListItem.SpecialEventOutcome -> "SpecialEventOutcome_$index"
                    is ListItem.SpecialEventTitle -> "SpecialEventTitle_$index"
                    is ListItem.SportsbookTitle -> "SportsbookTitle_$index"
                    is ListItem.Title -> "Title_$index"
                    is ListItem.UpComingEvent -> "UpComingEvent_$index"
                }
            },
            contentType = { _, item ->
                when (item) {
                    is ListItem.PreEvent -> "event"
                    is ListItem.LiveEvent -> "LiveEvent"
                    is ListItem.SpecialEventGroup -> "SpecialEventGroup"
                    is ListItem.SpecialEventOutcome ->"SpecialEventOutcome"
                    is ListItem.SpecialEventTitle -> "SpecialEventTitle"
                    is ListItem.SportsbookTitle -> "SportsbookTitle"
                    is ListItem.Title -> "Title"
                    is ListItem.UpComingEvent -> "UpComingEvent"
                }
            }
        ) { index, item -> // 'itemContent' de artık hem 'index' hem de 'item' alır
            when (item) {

                is ListItem.LiveEvent -> {

                    val onclickOutcome = remember(item.item.event.eventId) onclickOutcome@{
                        { market: MarketLookup?, outcome: OutComesItem ->
                            val market = item.item.event.markets?.firstOrNull()
                            market?.let {
                                it
                                val couponItem = CouponItem(
                                    eventId = item.item.event.eventId,
                                    marketId = market.marketId.toInt(),
                                    outcomeNo = outcome.outcomeNo,
                                    oddPrev = outcome.odd ?: 0.0,
                                    sportId = item.item.event.sportId,
                                    bettingPhase = item.item.event.bettingPhase,
                                    eventName = item.item.event.getName(),
                                    marketName = CouponItem.getMarketName(market, outcome),
                                    eventDate = item.item.event.eventDate
                                )
                                CouponManagerV2.addOrRemoveItem(couponItem)
                                viewModel.filterChanged()
                            }
                            Unit
                        }
                    }

                    Column(
                        modifier = Modifier.height(100.dp).fillMaxWidth(),
                    ) {


                        SportsbookItemHeader(
                            mbsIconResource = item.item.event.mbsIconResource,
                            homeTeamName = item.item.event.homeTeamName,
                            awayTeamName = item.item.event.awayTeamName,
                            score = item.item.event.score,
                            kingCount = item.item.event.kingCount,
                            minute = item.item.score?.minute
                        )
                        SportsbookMarketList(
                            item.item.markets.first?.outComes,
                            item.item.marketLookups.first,
                            item.item.event.oddCount.toString(),
                            onclickOutcome = onclickOutcome
                        )

                    }
                }
                is ListItem.PreEvent -> {}
                is ListItem.SpecialEventGroup -> {}
                is ListItem.SpecialEventOutcome -> {}
                is ListItem.SpecialEventTitle -> {}
                is ListItem.SportsbookTitle -> {
                    TitleItem(imageUrl = item.item.leagueCountryImage, title = item.item.cn)
                }
                is ListItem.Title -> {
                    TitleItem(imageUrl = "" , title = item.item.title)
                }
                is ListItem.UpComingEvent -> {}
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
            viewModel.getEvents(it, SportsBookFilterManager.selectedFilter.programType)
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
    mbsIconResource: DrawableResource?,
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
        val alpha by animateFloatAsState(
            targetValue = if (kingCount > 0) 1f else 0f,
            label = "subtitle_alpha"
        )

        Image(painter = painterResource(mbsIconResource ?: Res.drawable.king_icon), modifier = Modifier.size(24.dp).alpha(alpha), contentDescription = null)
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

            Column(modifier = Modifier.width(IntrinsicSize.Min).height(52.dp), verticalArrangement = Arrangement.Bottom) {
                Row(modifier = Modifier.sizeIn(minWidth =  50.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)){
                    val alpha by animateFloatAsState(
                        targetValue = if (kingCount > 0) 1f else 0f,
                        label = "subtitle_alpha"
                    )
                    Image(
                        modifier = Modifier.alpha(alpha).size(20.dp),
                        painter = painterResource(Res.drawable.king_icon),
                        contentDescription = ""
                    )
                    Image(
                        modifier = Modifier.alpha(alpha).size(20.dp),
                        painter = painterResource(Res.drawable.king_icon),
                        contentDescription = ""
                    )
                    Image(
                        modifier = Modifier.alpha(alpha).size(20.dp),
                        painter = painterResource(Res.drawable.king_icon),
                        contentDescription = ""
                    )

                }
                val minute = if (minute.isNullOrBlank()) "-" else minute + "'"
                Text(
                    minute,
                    color = Color(0xff008641),
                    fontSize = 12.sp,
                    modifier = Modifier.sizeIn(minWidth = 50.dp).fillMaxWidth().paddingFromBaseline(0.dp, 0.dp).padding(vertical = 4.dp).height(20.dp).clip(RoundedCornerShape(20)).background(Color(0xffd5e1e5))
                        .padding(horizontal = 20.dp).padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

@Composable
private fun SportsbookMarketList(
    outComes: ImmutableList<OutComesItem>?,
    marketLookup: MarketLookup?,
    oddCount: String,
    onclickOutcome: (MarketLookup?, OutComesItem) -> Unit
) {
    val firstOutcome = outComes?.getOrNull(0)
    val secondOutcome = outComes?.getOrNull(1)
    val thirdOutcome = outComes?.getOrNull(2)
    Row(
        modifier = Modifier.fillMaxWidth().height(50.dp).padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SportsbookMarketItem(outComes?.getOrNull(0), marketLookup, modifier = Modifier.clickable {
            firstOutcome?.let {
                onclickOutcome.invoke(marketLookup, it)
            }
        }.weight(1f))
        SportsbookMarketItem(outComes?.getOrNull(1), marketLookup, modifier = Modifier.clickable {
            secondOutcome?.let {
                onclickOutcome.invoke(marketLookup,it)
            }
        }.weight(1f))
        SportsbookMarketItem(outComes?.getOrNull(2), marketLookup, modifier = Modifier.clickable {
            thirdOutcome?.let {
                onclickOutcome.invoke(marketLookup, it)
            }
        }.weight(1f))
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
            modifier = Modifier.fillMaxWidth().background(if (outComesItem?.isSelected == true) Color.Yellow else Color.White),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

    }
}




@Preview
@Composable
private fun SportsbookMarketItem(){
    MaterialTheme {
        Text("hasan")
    }
}


