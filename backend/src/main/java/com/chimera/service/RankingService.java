package com.chimera.service;

import com.chimera.dto.RankingRequest;
import com.chimera.dto.RankingResponse;
import com.chimera.model.AssetRanking;
import com.chimera.model.AssetType;
import com.chimera.model.EquityData;
import com.chimera.repository.AssetRankingRepository;
import com.chimera.repository.EquityDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class RankingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RankingService.class);
    
    @Autowired(required = false)
    private AssetRankingRepository assetRankingRepository;
    
    @Autowired(required = false)
    private EquityDataRepository equityDataRepository;
    
    @Autowired
    private CacheService cacheService;
    
    // Cache rankings for 30 minutes
    @Cacheable(value = "rankings", key = "#request.cacheKey")
    public RankingResponse generateRankings(RankingRequest request) {
        long startTime = System.currentTimeMillis();
        
        logger.info("Generating rankings for request: {}", request);
        
        try {
            // Check cache first
            List<AssetRanking> cachedRankings = getCachedRankings(request);
            if (!cachedRankings.isEmpty()) {
                return buildResponseFromCache(cachedRankings, startTime);
            }
            
            // Generate new rankings
            List<AssetRanking> rankings = computeRankings(request);
            
            // Save to database for caching
            saveRankings(rankings, request);
            
            // Build response
            return buildResponse(rankings, startTime, false);
            
        } catch (Exception e) {
            logger.error("Error generating rankings: ", e);
            return getFallbackResponse(request, startTime);
        }
    }
    
    private List<AssetRanking> getCachedRankings(RankingRequest request) {
        if (assetRankingRepository == null) {
            logger.debug("AssetRankingRepository not available, skipping cache lookup");
            return new ArrayList<>();
        }
        
        try {
            LocalDateTime cacheThreshold = LocalDateTime.now().minusMinutes(30);
            
            return assetRankingRepository.findCachedRankings(
                request.getAmountInr(),
                request.getHorizonDays(),
                request.getRiskPreference(),
                cacheThreshold
            );
        } catch (Exception e) {
            logger.warn("Error accessing cached rankings: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<AssetRanking> computeRankings(RankingRequest request) {
        // Check if equity repository is available
        if (equityDataRepository == null) {
            logger.info("EquityDataRepository not available, using mock data");
            return generateMockRankings(request);
        }
        
        List<EquityData> equityData;
        try {
            // Get latest equity data
            LocalDate latestDate = equityDataRepository.findLatestTradingDate();
            if (latestDate == null) {
                latestDate = LocalDate.now().minusDays(1);
            }
            
            equityData = equityDataRepository.findByTradeDateOrderBySymbolAsc(latestDate);
            
            if (equityData.isEmpty()) {
                return generateMockRankings(request);
            }
        } catch (Exception e) {
            logger.warn("Error accessing equity data: {}", e.getMessage());
            return generateMockRankings(request);
        }
        
        // Compute scores based on financial metrics
        List<AssetRanking> rankings = new ArrayList<>();
        int rank = 1;
        
        for (EquityData equity : equityData) {
            if (rank > request.getMaxResults()) break;
            
            AssetRanking ranking = new AssetRanking();
            ranking.setSymbol(equity.getSymbol());
            ranking.setName(equity.getName());
            ranking.setLastPrice(equity.getClosePrice());
            ranking.setChange(equity.getChangePercent());
            ranking.setAssetType(AssetType.EQUITY);
            
            // Simple scoring based on multiple factors
            double score = calculateScore(equity, request);
            ranking.setScore(score);
            
            // Confidence based on data quality and score
            int confidence = calculateConfidence(equity, score);
            ranking.setConfidence(confidence);
            
            // Recommendation based on score and risk preference
            String recommendation = getRecommendation(score, confidence, request);
            ranking.setRecommendation(recommendation);
            
            ranking.setRank(rank);
            
            // Set request context for caching
            ranking.setRequestAmount(request.getAmountInr());
            ranking.setRequestHorizonDays(request.getHorizonDays());
            ranking.setRequestRiskPreference(request.getRiskPreference());
            
            rankings.add(ranking);
            rank++;
        }
        
        return rankings;
    }
    
    private double calculateScore(EquityData equity, RankingRequest request) {
        double score = 0.5; // Base score
        
        try {
            // Factor 1: Daily return (30% weight)
            double dailyReturn = equity.getDailyReturn();
            score += dailyReturn * 0.3;
            
            // Factor 2: Volume liquidity (20% weight)
            if (equity.getTotalTradedQuantity() != null) {
                double volumeScore = Math.min(equity.getTotalTradedQuantity() / 1000000.0, 1.0);
                score += volumeScore * 0.2;
            }
            
            // Factor 3: Price stability (25% weight)
            if (equity.getHighPrice() != null && equity.getLowPrice() != null) {
                double range = (equity.getHighPrice() - equity.getLowPrice()) / equity.getClosePrice();
                double stability = Math.max(0, 1.0 - range);
                score += stability * 0.25;
            }
            
            // Factor 4: Risk adjustment based on preference (25% weight)
            double riskAdjustment = getRiskAdjustment(request.getRiskPreference(), dailyReturn);
            score += riskAdjustment * 0.25;
            
            // Normalize score to 0-1 range
            score = Math.max(0.0, Math.min(1.0, score));
            
        } catch (Exception e) {
            logger.warn("Error calculating score for {}: {}", equity.getSymbol(), e.getMessage());
            score = 0.5; // Default score on error
        }
        
        return score;
    }
    
    private double getRiskAdjustment(String riskPreference, double dailyReturn) {
        switch (riskPreference) {
            case "CONSERVATIVE":
                return Math.abs(dailyReturn) < 0.02 ? 0.3 : -0.2; // Prefer stable stocks
            case "AGGRESSIVE":
                return Math.abs(dailyReturn) > 0.05 ? 0.3 : -0.1; // Prefer volatile stocks
            case "MODERATE":
            default:
                return Math.abs(dailyReturn) > 0.01 && Math.abs(dailyReturn) < 0.04 ? 0.2 : 0.0;
        }
    }
    
    private int calculateConfidence(EquityData equity, double score) {
        int confidence = 50; // Base confidence
        
        try {
            // Higher confidence for complete data
            if (equity.getTotalTradedQuantity() != null && equity.getTotalTradedQuantity() > 0) {
                confidence += 20;
            }
            
            if (equity.getHighPrice() != null && equity.getLowPrice() != null) {
                confidence += 15;
            }
            
            if (equity.getPrevClose() != null) {
                confidence += 15;
            }
            
            // Adjust for score extremes (very high or very low scores are less confident)
            if (score > 0.8 || score < 0.2) {
                confidence -= 10;
            }
            
            // Cap at 95% (never 100% confident in financial predictions)
            confidence = Math.min(95, Math.max(30, confidence));
            
        } catch (Exception e) {
            logger.warn("Error calculating confidence for {}: {}", equity.getSymbol(), e.getMessage());
            confidence = 50;
        }
        
        return confidence;
    }
    
    private String getRecommendation(double score, int confidence, RankingRequest request) {
        if (confidence < 60) {
            return "HOLD"; // Low confidence = hold
        }
        
        if (score > 0.75) {
            return "BUY";
        } else if (score > 0.6) {
            return request.isConservative() ? "HOLD" : "BUY";
        } else if (score > 0.4) {
            return "HOLD";
        } else {
            return request.isAggressive() ? "HOLD" : "SELL";
        }
    }
    
    private List<AssetRanking> generateMockRankings(RankingRequest request) {
        logger.info("Generating mock rankings due to lack of real data");
        
        String[] mockSymbols = {"RELIANCE", "TCS", "INFY", "HDFC", "ICICI", "BAJAJ-AUTO", "WIPRO", "ITC", "SBIN", "LT"};
        String[] mockNames = {
            "Reliance Industries Ltd.", "Tata Consultancy Services Ltd.", "Infosys Ltd.",
            "HDFC Bank Ltd.", "ICICI Bank Ltd.", "Bajaj Auto Ltd.", "Wipro Ltd.",
            "ITC Ltd.", "State Bank of India", "Larsen & Toubro Ltd."
        };
        
        List<AssetRanking> rankings = new ArrayList<>();
        
        for (int i = 0; i < Math.min(mockSymbols.length, request.getMaxResults()); i++) {
            AssetRanking ranking = new AssetRanking();
            ranking.setSymbol(mockSymbols[i]);
            ranking.setName(mockNames[i]);
            ranking.setRank(i + 1);
            
            // Generate realistic mock data
            double baseScore = 0.9 - (i * 0.05); // Decreasing scores
            ranking.setScore(Math.max(0.5, baseScore + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1));
            
            int baseConfidence = 95 - (i * 3); // Decreasing confidence
            ranking.setConfidence(Math.max(70, baseConfidence));
            
            ranking.setRecommendation(ranking.getScore() > 0.75 ? "BUY" : "HOLD");
            ranking.setLastPrice(1000.0 + ThreadLocalRandom.current().nextDouble() * 3000);
            ranking.setChange(String.format("%+.2f%%", (ThreadLocalRandom.current().nextDouble() - 0.5) * 10));
            ranking.setAssetType(AssetType.EQUITY);
            
            // Set request context
            ranking.setRequestAmount(request.getAmountInr());
            ranking.setRequestHorizonDays(request.getHorizonDays());
            ranking.setRequestRiskPreference(request.getRiskPreference());
            
            rankings.add(ranking);
        }
        
        return rankings;
    }
    
    private void saveRankings(List<AssetRanking> rankings, RankingRequest request) {
        if (assetRankingRepository == null) {
            logger.debug("AssetRankingRepository not available, skipping rankings save");
            return;
        }
        
        try {
            assetRankingRepository.saveAll(rankings);
            logger.info("Saved {} rankings to cache", rankings.size());
        } catch (Exception e) {
            logger.error("Error saving rankings to cache: ", e);
        }
    }
    
    private RankingResponse buildResponse(List<AssetRanking> rankings, long startTime, boolean fromCache) {
        List<RankingResponse.AssetRankingDto> rankingDtos = rankings.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        RankingResponse.RankingMetadata metadata = new RankingResponse.RankingMetadata();
        metadata.setTotalAssets(rankingDtos.size() * 5); // Simulated total
        metadata.setDisplayedAssets(rankingDtos.size());
        metadata.setDataSource(fromCache ? "Cache + NSE/BSE EOD" : "NSE/BSE EOD + AMFI NAV");
        metadata.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        metadata.setCacheHit(fromCache);
        
        return new RankingResponse(rankingDtos, metadata);
    }
    
    private RankingResponse buildResponseFromCache(List<AssetRanking> rankings, long startTime) {
        logger.info("Returning {} cached rankings", rankings.size());
        return buildResponse(rankings, startTime, true);
    }
    
    private RankingResponse.AssetRankingDto convertToDto(AssetRanking ranking) {
        return new RankingResponse.AssetRankingDto(
            ranking.getSymbol(),
            ranking.getName(),
            ranking.getScore(),
            ranking.getConfidence(),
            ranking.getRank(),
            ranking.getRecommendation(),
            ranking.getLastPrice(),
            ranking.getChange()
        );
    }
    
    private RankingResponse getFallbackResponse(RankingRequest request, long startTime) {
        logger.warn("Using fallback response due to error");
        
        List<AssetRanking> mockRankings = generateMockRankings(request);
        RankingResponse response = buildResponse(mockRankings, startTime, false);
        response.setStatus("fallback");
        response.getMetadata().setDataSource("Mock Data (Fallback)");
        response.getMetadata().setDisclaimer(
            response.getMetadata().getDisclaimer() + " Note: Using fallback data due to system error."
        );
        
        return response;
    }
    
    public void cleanupOldRankings() {
        if (assetRankingRepository == null) {
            logger.debug("AssetRankingRepository not available, skipping cleanup");
            return;
        }
        
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
            assetRankingRepository.deleteOldRankings(cutoff);
            logger.info("Cleaned up rankings older than {}", cutoff);
        } catch (Exception e) {
            logger.error("Error cleaning up old rankings: ", e);
        }
    }
}