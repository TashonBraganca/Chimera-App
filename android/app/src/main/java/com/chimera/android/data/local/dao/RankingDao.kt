package com.chimera.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.android.data.local.entity.CachedRanking
import kotlinx.coroutines.flow.Flow

@Dao
interface RankingDao {
    
    @Query("""
        SELECT * FROM cached_rankings 
        WHERE amountInr = :amountInr 
        AND horizonDays = :horizonDays 
        AND assetType = :assetType 
        AND riskPreference = :riskPreference
        ORDER BY rank ASC
    """)
    suspend fun getRankingsForRequest(
        amountInr: Long,
        horizonDays: Int,
        assetType: String,
        riskPreference: String
    ): List<CachedRanking>
    
    @Query("SELECT * FROM cached_rankings ORDER BY cachedTimestamp DESC")
    fun observeAllRankings(): Flow<List<CachedRanking>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRankings(rankings: List<CachedRanking>)
    
    @Query("""
        DELETE FROM cached_rankings 
        WHERE amountInr = :amountInr 
        AND horizonDays = :horizonDays 
        AND assetType = :assetType 
        AND riskPreference = :riskPreference
    """)
    suspend fun deleteRankingsForRequest(
        amountInr: Long,
        horizonDays: Int,
        assetType: String,
        riskPreference: String
    )
    
    @Query("DELETE FROM cached_rankings WHERE cachedTimestamp < :timestamp")
    suspend fun deleteOldRankings(timestamp: Long)
    
    @Query("DELETE FROM cached_rankings")
    suspend fun clearAllRankings()
}