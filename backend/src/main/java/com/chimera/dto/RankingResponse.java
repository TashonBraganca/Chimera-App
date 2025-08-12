package com.chimera.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class RankingResponse {
    
    @JsonProperty("status")
    private String status = "success";
    
    @JsonProperty("rankings")
    private List<AssetRankingDto> rankings;
    
    @JsonProperty("metadata")
    private RankingMetadata metadata;
    
    // Constructors
    public RankingResponse() {}
    
    public RankingResponse(List<AssetRankingDto> rankings, RankingMetadata metadata) {
        this.rankings = rankings;
        this.metadata = metadata;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<AssetRankingDto> getRankings() { return rankings; }
    public void setRankings(List<AssetRankingDto> rankings) { this.rankings = rankings; }
    
    public RankingMetadata getMetadata() { return metadata; }
    public void setMetadata(RankingMetadata metadata) { this.metadata = metadata; }
    
    // Inner classes
    public static class AssetRankingDto {
        @JsonProperty("symbol")
        private String symbol;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("score")
        private Double score;
        
        @JsonProperty("confidence")
        private Integer confidence;
        
        @JsonProperty("rank")
        private Integer rank;
        
        @JsonProperty("recommendation")
        private String recommendation;
        
        @JsonProperty("lastPrice")
        private Double lastPrice;
        
        @JsonProperty("change")
        private String change;
        
        // Constructors
        public AssetRankingDto() {}
        
        public AssetRankingDto(String symbol, String name, Double score, Integer confidence,
                              Integer rank, String recommendation, Double lastPrice, String change) {
            this.symbol = symbol;
            this.name = name;
            this.score = score;
            this.confidence = confidence;
            this.rank = rank;
            this.recommendation = recommendation;
            this.lastPrice = lastPrice;
            this.change = change;
        }
        
        // Getters and Setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public Integer getConfidence() { return confidence; }
        public void setConfidence(Integer confidence) { this.confidence = confidence; }
        
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        
        public Double getLastPrice() { return lastPrice; }
        public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }
        
        public String getChange() { return change; }
        public void setChange(String change) { this.change = change; }
    }
    
    public static class RankingMetadata {
        @JsonProperty("totalAssets")
        private Integer totalAssets;
        
        @JsonProperty("displayedAssets")
        private Integer displayedAssets;
        
        @JsonProperty("lastUpdated")
        private String lastUpdated;
        
        @JsonProperty("dataSource")
        private String dataSource;
        
        @JsonProperty("disclaimer")
        private String disclaimer;
        
        @JsonProperty("processingTimeMs")
        private Long processingTimeMs;
        
        @JsonProperty("cacheHit")
        private Boolean cacheHit = false;
        
        // Constructors
        public RankingMetadata() {
            this.lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " IST";
            this.disclaimer = "This analysis is for educational purposes only and should not be considered as investment advice. " +
                            "Please consult with a financial advisor before making investment decisions.";
        }
        
        public RankingMetadata(Integer totalAssets, Integer displayedAssets, String dataSource) {
            this();
            this.totalAssets = totalAssets;
            this.displayedAssets = displayedAssets;
            this.dataSource = dataSource;
        }
        
        // Getters and Setters
        public Integer getTotalAssets() { return totalAssets; }
        public void setTotalAssets(Integer totalAssets) { this.totalAssets = totalAssets; }
        
        public Integer getDisplayedAssets() { return displayedAssets; }
        public void setDisplayedAssets(Integer displayedAssets) { this.displayedAssets = displayedAssets; }
        
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
        
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        
        public String getDisclaimer() { return disclaimer; }
        public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
        
        public Long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
        
        public Boolean getCacheHit() { return cacheHit; }
        public void setCacheHit(Boolean cacheHit) { this.cacheHit = cacheHit; }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankingResponse that = (RankingResponse) o;
        return Objects.equals(status, that.status) &&
               Objects.equals(rankings, that.rankings);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status, rankings);
    }
    
    @Override
    public String toString() {
        return "RankingResponse{" +
                "status='" + status + '\'' +
                ", rankings=" + (rankings != null ? rankings.size() + " items" : "null") +
                ", metadata=" + metadata +
                '}';
    }
}