package com.chimera.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chimera.android.data.model.AssetRanking

@Entity(tableName = "cached_rankings")
data class CachedRanking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: String,
    val assetName: String,
    val rank: Int,
    val totalScore: Double,
    val confidencePercent: Double,
    val returnScore: Double?,
    val volatilityScore: Double?,
    val momentumScore: Double?,
    val liquidityScore: Double?,
    val riskScore: Double?,
    val sentimentScore: Double?,
    val amountInr: Long,
    val horizonDays: Int,
    val assetType: String,
    val riskPreference: String,
    val createdAt: String,
    val cachedTimestamp: Long = System.currentTimeMillis()
) {
    fun toAssetRanking(): AssetRanking {
        return AssetRanking(
            assetId = assetId,
            assetName = assetName,
            rank = rank,
            totalScore = totalScore,
            confidencePercent = confidencePercent,
            returnScore = returnScore,
            volatilityScore = volatilityScore,
            momentumScore = momentumScore,
            liquidityScore = liquidityScore,
            riskScore = riskScore,
            sentimentScore = sentimentScore,
            displayFlag = true,
            lastUpdated = createdAt
        )
    }
}