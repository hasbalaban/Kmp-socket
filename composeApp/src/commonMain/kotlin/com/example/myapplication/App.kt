package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.viewmodel.SportsbookViewmodel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    viewModel: SportsbookViewmodel = koinViewModel()
) {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var rotation by remember { mutableStateOf(0f) }
        val rotationn = animateFloatAsState(rotation)
        val counter by viewModel.counter.collectAsState()


        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.compose_multiplatform), null,
                        modifier = Modifier
                            .rotate(rotationn.value)
                            .clickable {
                            rotation = (rotation + 90)
                        },
                    )
                    Text("Compose: $greeting")
                }
            }

            Text(text = counter.toString(), color = Color.Red, fontSize = 24.sp)
            EventSection()
        }
    }
}


@Composable
private fun EventSection(
    viewModel: SportsbookViewmodel = koinViewModel()
){
    val events by viewModel.events.collectAsState()

    LaunchedEffect(Unit){
        viewModel.getEvents()
        viewModel.getMarketConfig()
    }





    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)){
        items(events){
            Text(it.event.homeTeamName.toString() + "-" + it.event.awayTeamName.toString(), color = Color.Black, fontSize = 16.sp)
        }
    }
}