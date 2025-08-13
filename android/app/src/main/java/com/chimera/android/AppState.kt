package com.chimera.android

import androidx.compose.runtime.Stable
import com.chimera.android.data.model.RankingRequest
import kotlinx.serialization.Serializable

/**
 * Global app state holder for sharing data between screens
 */
@Stable
@Serializable
data class AppState(
    val isInitialized: Boolean = false,
    val isOnline: Boolean = true,
    val lastRankingRequest: RankingRequest? = null,
    val disclaimerAcknowledged: Boolean = false,
    val isDarkTheme: Boolean = false,
    val isHighContrastMode: Boolean = false,
    val textScaleFactor: Float = 1.0f,
    val dataFreshness: String = "Unknown",
    val lastUpdateTime: String = "",
    val hasSeenIntro: Boolean = false
)