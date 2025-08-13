package com.chimera.android.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class AssetRanking(
    val assetId: String,
    val assetName: String,
    val rank: Int,
    val totalScore: Double,
    val confidencePercent: Double,
    val returnScore: Double? = null,
    val volatilityScore: Double? = null,
    val momentumScore: Double? = null,
    val liquidityScore: Double? = null,
    val riskScore: Double? = null,
    val sentimentScore: Double? = null,
    val displayFlag: Boolean = true,
    val lastUpdated: String
)