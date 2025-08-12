package com.chimera.model;

public enum AssetType {
    EQUITY("Equity"),
    MUTUAL_FUND("Mutual Fund"),
    ETF("ETF"),
    BOND("Bond"),
    COMMODITY("Commodity");
    
    private final String displayName;
    
    AssetType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}