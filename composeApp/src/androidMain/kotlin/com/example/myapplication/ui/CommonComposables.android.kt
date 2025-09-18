package com.example.myapplication.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.myapplication.MbsImage
import com.example.myapplication.R
import com.example.myapplication.getMbsImage

@Composable
actual fun MbsIcon(mbs: Int, modifier: Modifier) {
    val mbsImage: MbsImage? = getMbsImage(mbs = mbs)

    val drawableResId: Int? = when (mbsImage) {
        MbsImage.MBS_1 -> R.drawable.ic_mbs_1_live
        MbsImage.MBS_2 -> R.drawable.ic_mbs_2_live
        MbsImage.MBS_3 -> R.drawable.ic_mbs_3_live
        null -> null
    }

    // 3. Eğer geçerli bir drawable ID'si bulunduysa, Image Composable'ını çiz
    if (drawableResId != null) {
        Image(
            painter = painterResource(id = drawableResId),
            contentDescription = "MBS $mbs",
            modifier = modifier
        )
    }
}
@Composable
actual fun KingIcon(modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.king_icon),
        contentDescription = "king icon",
        modifier = modifier
    )
}