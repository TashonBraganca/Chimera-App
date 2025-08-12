package com.chimera.repository;

import com.chimera.model.AssetRanking;
import com.chimera.model.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRankingRepository extends JpaRepository<AssetRanking, Long> {
    
    // Find cached rankings by request parameters
    @Query("SELECT ar FROM AssetRanking ar WHERE " +
           "ar.requestAmount = :amount AND " +
           "ar.requestHorizonDays = :horizonDays AND " +
           "ar.requestRiskPreference = :riskPreference AND " +
           "ar.createdAt > :since " +
           "ORDER BY ar.rank ASC")
    List<AssetRanking> findCachedRankings(@Param("amount") Double amount,
                                         @Param("horizonDays") Integer horizonDays,
                                         @Param("riskPreference") String riskPreference,
                                         @Param("since") LocalDateTime since);
    
    // Find rankings by asset type
    List<AssetRanking> findByAssetTypeOrderByRankAsc(AssetType assetType);
    
    // Find top N rankings
    @Query("SELECT ar FROM AssetRanking ar WHERE ar.createdAt > :since ORDER BY ar.rank ASC")
    List<AssetRanking> findTopRankings(@Param("since") LocalDateTime since);
    
    // Find by symbol
    Optional<AssetRanking> findTopBySymbolOrderByCreatedAtDesc(String symbol);
    
    // Find recent rankings for a specific request profile
    @Query("SELECT ar FROM AssetRanking ar WHERE " +
           "ar.requestAmount = :amount AND " +
           "ar.requestHorizonDays = :horizonDays AND " +
           "ar.requestRiskPreference = :riskPreference AND " +
           "ar.assetType = :assetType AND " +
           "ar.createdAt > :since " +
           "ORDER BY ar.rank ASC " +
           "LIMIT :maxResults")
    List<AssetRanking> findRecentRankings(@Param("amount") Double amount,
                                         @Param("horizonDays") Integer horizonDays,
                                         @Param("riskPreference") String riskPreference,
                                         @Param("assetType") AssetType assetType,
                                         @Param("since") LocalDateTime since,
                                         @Param("maxResults") Integer maxResults);
    
    // Delete old rankings (cleanup)
    @Query("DELETE FROM AssetRanking ar WHERE ar.createdAt < :cutoff")
    void deleteOldRankings(@Param("cutoff") LocalDateTime cutoff);
    
    // Count rankings by confidence level
    @Query("SELECT COUNT(ar) FROM AssetRanking ar WHERE ar.confidence >= :minConfidence AND ar.createdAt > :since")
    Long countHighConfidenceRankings(@Param("minConfidence") Integer minConfidence, 
                                    @Param("since") LocalDateTime since);
    
    // Get average confidence for recent rankings
    @Query("SELECT AVG(ar.confidence) FROM AssetRanking ar WHERE ar.createdAt > :since")
    Double getAverageConfidence(@Param("since") LocalDateTime since);
    
    // Find rankings by score range
    @Query("SELECT ar FROM AssetRanking ar WHERE " +
           "ar.score BETWEEN :minScore AND :maxScore AND " +
           "ar.createdAt > :since " +
           "ORDER BY ar.score DESC")
    List<AssetRanking> findByScoreRange(@Param("minScore") Double minScore,
                                       @Param("maxScore") Double maxScore,
                                       @Param("since") LocalDateTime since);
    
    // Check if cache exists for request
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM AssetRanking ar WHERE " +
           "ar.requestAmount = :amount AND " +
           "ar.requestHorizonDays = :horizonDays AND " +
           "ar.requestRiskPreference = :riskPreference AND " +
           "ar.createdAt > :since")
    boolean existsCachedRankings(@Param("amount") Double amount,
                                @Param("horizonDays") Integer horizonDays,
                                @Param("riskPreference") String riskPreference,
                                @Param("since") LocalDateTime since);
}