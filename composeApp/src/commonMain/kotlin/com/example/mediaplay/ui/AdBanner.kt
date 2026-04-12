package com.example.mediaplay.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AdBanner(modifier: Modifier = Modifier)

/**
 * Exibe um anúncio de tela cheia (Intersticial).
 */
expect fun showInterstitialAd()
