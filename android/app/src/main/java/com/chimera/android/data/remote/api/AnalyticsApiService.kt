package com.chimera.android.data.remote.api

import com.chimera.android.data.model.QARequest
import com.chimera.android.data.model.QAResponse
import com.chimera.android.data.model.RankingRequest
import com.chimera.android.data.model.RankingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AnalyticsApiService {
    
    @POST("api/rank")
    suspend fun getRankings(@Body request: RankingRequest): RankingResponse
    
    @POST("api/chat") 
    suspend fun askQuestion(@Body request: QARequest): QAResponse
    
    @GET("api/freshness")
    suspend fun getDataFreshness(): FreshnessResponse
}

// Temporary data class for freshness - matches backend DTO
data class FreshnessResponse(
    val overallStatus: String,
    val lastChecked: String,
    val disclaimer: String
)