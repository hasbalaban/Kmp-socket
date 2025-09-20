package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.manager.CouponManagerV2

@Composable
fun BottomBar() {
    val couponItems by CouponManagerV2.eventItems.collectAsState()

    val couponData = remember(couponItems) {
        CouponManagerV2.getRateButtonData()
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White),
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            shape = RoundedCornerShape(20),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally){
                Text(couponData.first.toString() + " Maç", color = Color.Black.copy(alpha = 0.6f))
                Text(text = couponData.second.toString(), color = Color.Black, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
            }
        }
    }
}