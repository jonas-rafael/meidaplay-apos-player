package com.example.mediaplay.ui

import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.adsbase.StartAppAd
import com.example.mediaplay.MainActivity

@Composable
actual fun AdBanner(modifier: Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            Banner(context).apply {
                // Configurações opcionais aqui
            }
        }
    )
}

actual fun showInterstitialAd() {
    MainActivity.activityRef?.get()?.let { activity ->
        StartAppAd.showAd(activity)
    }
}
