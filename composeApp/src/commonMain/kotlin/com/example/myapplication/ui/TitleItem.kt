package com.example.myapplication.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ignoreNull
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.rememberImagePainter

@Composable
fun TitleItem(imageUrl: String?, title: String?) {
    Row(
        modifier = Modifier.background(Color.DarkGray).fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp).height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = rememberImagePainter(
            ImageRequest {
                data(imageUrl)
            }
        )
        imageUrl?.let {
            Image(
                painter = painter,
                modifier = Modifier.size(12.dp),
                contentDescription = "Network Image",
                contentScale = ContentScale.Crop,
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title.ignoreNull(),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}