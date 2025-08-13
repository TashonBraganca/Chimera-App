package com.chimera.android.data.repository

import com.chimera.android.data.local.dao.RankingDao
import com.chimera.android.data.model.RankingRequest
import com.chimera.android.data.model.RankingResponse
import com.chimera.android.data.model.AssetRanking
import com.chimera.android.data.remote.api.AnalyticsApiService
import com.chimera.android.data.local.entity.CachedRanking
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepository @Inject constructor(
    private val apiService: AnalyticsApiService,
    private val rankingDao: RankingDao
) {
    
    /**
     * Get rankings with offline-first approach
     */
    suspend fun getRankings(request: RankingRequest): RankingResponse {
        return try {
            // Try to fetch from API first
            val response = apiService.getRankings(request)
            
            Timber.d("Rankings fetched from API: ${response.rankings.size} items")
            response
            
        } catch (e: Exception) {
            Timber.w(e, "API failed, falling back to mock data")
            
            // Return mock data if API fails
            createMockResponse()
        }
    }
    
    /**
     * Observe cached rankings for offline use
     */
    fun observeCachedRankings(): Flow<List<CachedRanking>> {
        return rankingDao.observeAllRankings()
    }
    
    private fun createMockResponse(): RankingResponse {
        val mockRankings = listOf(
            com.chimera.android.data.model.BackendAssetRanking(
                symbol = "RELIANCE",
                name = "Reliance Industries Ltd.",
                score = 0.87,
                confidence = 92,
                rank = 1,
                recommendation = "BUY",
                lastPrice = 2850.50,
                change = "+2.3%"
            ),
            com.chimera.android.data.model.BackendAssetRanking(
                symbol = "TCS",
                name = "Tata Consultancy Services Ltd.",
                score = 0.84,
                confidence = 89,
                rank = 2,
                recommendation = "BUY",
                lastPrice = 4125.75,
                change = "+1.8%"
            )
        )
        
        return RankingResponse(
            status = "success",
            rankings = mockRankings,
            metadata = com.chimera.android.data.model.RankingMetadata(
                totalAssets = 50,
                displayedAssets = 2,
                lastUpdated = "2024-01-10 15:30:00 IST",
                dataSource = "Mock Data - Offline",
                disclaimer = "This is mock data for testing purposes only."
            )
        )
    }
}