package com.chimera.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.Objects;

public class RankingRequest {
    
    @JsonProperty("amountInr")
    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum investment amount is ₹1,000")
    @DecimalMax(value = "100000000.0", message = "Maximum investment amount is ₹10 crores")
    private Double amountInr;
    
    @JsonProperty("horizonDays")
    @NotNull(message = "Investment horizon is required")
    @Min(value = 1, message = "Minimum horizon is 1 day")
    @Max(value = 3650, message = "Maximum horizon is 10 years")
    private Integer horizonDays;
    
    @JsonProperty("riskPreference")
    @NotBlank(message = "Risk preference is required")
    @Pattern(regexp = "CONSERVATIVE|MODERATE|AGGRESSIVE", 
             message = "Risk preference must be CONSERVATIVE, MODERATE, or AGGRESSIVE")
    private String riskPreference = "MODERATE";
    
    @JsonProperty("assetType")
    @Pattern(regexp = "EQUITY|MUTUAL_FUND|ETF|ALL", 
             message = "Asset type must be EQUITY, MUTUAL_FUND, ETF, or ALL")
    private String assetType = "EQUITY";
    
    @JsonProperty("maxResults")
    @Min(value = 1, message = "Minimum results is 1")
    @Max(value = 50, message = "Maximum results is 50")
    private Integer maxResults = 10;
    
    // Constructors
    public RankingRequest() {}
    
    public RankingRequest(Double amountInr, Integer horizonDays, String riskPreference) {
        this.amountInr = amountInr;
        this.horizonDays = horizonDays;
        this.riskPreference = riskPreference;
    }
    
    // Getters and Setters
    public Double getAmountInr() { return amountInr; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }
    
    public Integer getHorizonDays() { return horizonDays; }
    public void setHorizonDays(Integer horizonDays) { this.horizonDays = horizonDays; }
    
    public String getRiskPreference() { return riskPreference; }
    public void setRiskPreference(String riskPreference) { this.riskPreference = riskPreference; }
    
    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    
    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
    
    // Helper methods
    public String getCacheKey() {
        return String.format("ranking_%s_%d_%s_%s_%d", 
            amountInr.toString(), horizonDays, riskPreference, assetType, maxResults);
    }
    
    public boolean isShortTerm() { return horizonDays <= 90; }
    public boolean isMediumTerm() { return horizonDays > 90 && horizonDays <= 540; }
    public boolean isLongTerm() { return horizonDays > 540; }
    
    public boolean isConservative() { return "CONSERVATIVE".equals(riskPreference); }
    public boolean isModerate() { return "MODERATE".equals(riskPreference); }
    public boolean isAggressive() { return "AGGRESSIVE".equals(riskPreference); }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankingRequest that = (RankingRequest) o;
        return Objects.equals(amountInr, that.amountInr) &&
               Objects.equals(horizonDays, that.horizonDays) &&
               Objects.equals(riskPreference, that.riskPreference) &&
               Objects.equals(assetType, that.assetType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amountInr, horizonDays, riskPreference, assetType);
    }
    
    @Override
    public String toString() {
        return "RankingRequest{" +
                "amountInr=" + amountInr +
                ", horizonDays=" + horizonDays +
                ", riskPreference='" + riskPreference + '\'' +
                ", assetType='" + assetType + '\'' +
                ", maxResults=" + maxResults +
                '}';
    }
}