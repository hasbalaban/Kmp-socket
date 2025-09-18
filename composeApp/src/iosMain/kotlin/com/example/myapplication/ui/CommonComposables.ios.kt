package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import com.example.myapplication.MbsImage
import com.example.myapplication.getMbsImage
import myapplication.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import myapplication.composeapp.generated.resources.ic_mbs_1_live
import myapplication.composeapp.generated.resources.ic_mbs_2_live
import myapplication.composeapp.generated.resources.ic_mbs_3_live
import myapplication.composeapp.generated.resources.king_icon


@Composable
actual fun MbsIcon(mbs: Int, modifier: Modifier) {
    // 1. Paylaşılan iş mantığını çağır
    val mbsImage: MbsImage? = getMbsImage(mbs = mbs)

    // 2. Soyut kimliği, platformdan bağımsız, tip güvenli bir DrawableResource'a çevir
    //    Artık String değil, doğrudan Res nesnesini kullanıyoruz.
    val drawableRes: DrawableResource? = when (mbsImage) {
        MbsImage.MBS_1 -> Res.drawable.ic_mbs_1_live
        MbsImage.MBS_2 -> Res.drawable.ic_mbs_2_live
        MbsImage.MBS_3 -> Res.drawable.ic_mbs_3_live
        null -> null
    }

    // 3. Eğer geçerli bir kaynak bulunduysa, Image Composable'ını çiz
    if (drawableRes != null) {
        Image(
            // painterResource artık doğru tip olan DrawableResource'u alıyor
            painter = painterResource(resource = drawableRes),
            contentDescription = "MBS $mbs",
            modifier = modifier
        )
    }

}

@Composable
actual fun KingIcon(modifier: Modifier) {
    Image(
        // painterResource artık doğru tip olan DrawableResource'u alıyor
        painter = painterResource(resource = Res.drawable.king_icon),
        contentDescription = "king icon",
        modifier = modifier
    )
}