package com.chimera.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "asset_rankings")
public class AssetRanking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double score;
    
    @Column(nullable = false)
    private Integer confidence;
    
    @Column(nullable = false)
    private Integer rank;
    
    @Column(nullable = false)
    private String recommendation;
    
    @Column(name = "last_price")
    private Double lastPrice;
    
    @Column(name = "price_change")
    private String change;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type")
    private AssetType assetType = AssetType.EQUITY;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Request context for caching
    @Column(name = "request_amount")
    private Double requestAmount;
    
    @Column(name = "request_horizon_days")
    private Integer requestHorizonDays;
    
    @Column(name = "request_risk_preference")
    private String requestRiskPreference;
    
    // Constructors
    public AssetRanking() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AssetRanking(String symbol, String name, Double score, Integer confidence, 
                       Integer rank, String recommendation) {
        this();
        this.symbol = symbol;
        this.name = name;
        this.score = score;
        this.confidence = confidence;
        this.rank = rank;
        this.recommendation = recommendation;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Double getRequestAmount() { return requestAmount; }
    public void setRequestAmount(Double requestAmount) { this.requestAmount = requestAmount; }
    
    public Integer getRequestHorizonDays() { return requestHorizonDays; }
    public void setRequestHorizonDays(Integer requestHorizonDays) { this.requestHorizonDays = requestHorizonDays; }
    
    public String getRequestRiskPreference() { return requestRiskPreference; }
    public void setRequestRiskPreference(String requestRiskPreference) { this.requestRiskPreference = requestRiskPreference; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetRanking that = (AssetRanking) o;
        return Objects.equals(symbol, that.symbol) && 
               Objects.equals(createdAt.toLocalDate(), that.createdAt.toLocalDate());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbol, createdAt.toLocalDate());
    }
    
    @Override
    public String toString() {
        return "AssetRanking{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", rank=" + rank +
                ", score=" + score +
                ", confidence=" + confidence +
                ", recommendation='" + recommendation + '\'' +
                '}';
    }
}