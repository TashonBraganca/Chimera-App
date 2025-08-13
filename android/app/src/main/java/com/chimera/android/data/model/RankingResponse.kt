package com.chimera.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RankingResponse(
    val status: String,
    val rankings: List<BackendAssetRanking>,
    val metadata: RankingMetadata
)

@Serializable
data class BackendAssetRanking(
    val symbol: String,
    val name: String,
    val score: Double,
    val confidence: Int,
    val rank: Int,
    val recommendation: String,
    val lastPrice: Double,
    val change: String
)

@Serializable
data class RankingMetadata(
    val totalAssets: Int,
    val displayedAssets: Int,
    val lastUpdated: String,
    val dataSource: String,
    val disclaimer: String
)