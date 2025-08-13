package com.chimera.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RankingRequest(
    val amountInr: Long,
    val horizonDays: Int,
    val assetType: AssetType,
    val riskPreference: String, // "conservative", "balanced", "aggressive"
    val topK: Int = 20,
    val sectorFilter: String? = null,
    val marketCapFilter: String? = null
)