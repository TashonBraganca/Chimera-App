package com.chimera.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "equity_data", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "trade_date"}))
public class EquityData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;
    
    @Column(name = "open_price")
    private Double openPrice;
    
    @Column(name = "high_price")
    private Double highPrice;
    
    @Column(name = "low_price")
    private Double lowPrice;
    
    @Column(name = "close_price", nullable = false)
    private Double closePrice;
    
    @Column(name = "last_price")
    private Double lastPrice;
    
    @Column(name = "prev_close")
    private Double prevClose;
    
    @Column(name = "total_traded_quantity")
    private Long totalTradedQuantity;
    
    @Column(name = "total_traded_value")
    private Double totalTradedValue;
    
    @Column(name = "market_cap")
    private Double marketCap;
    
    @Column(length = 50)
    private String series = "EQ";
    
    @Column(length = 50)
    private String sector;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "data_source", length = 50)
    private String dataSource = "NSE";
    
    // Constructors
    public EquityData() {
        this.createdAt = LocalDateTime.now();
    }
    
    public EquityData(String symbol, String name, LocalDate tradeDate, Double closePrice) {
        this();
        this.symbol = symbol;
        this.name = name;
        this.tradeDate = tradeDate;
        this.closePrice = closePrice;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    
    public Double getOpenPrice() { return openPrice; }
    public void setOpenPrice(Double openPrice) { this.openPrice = openPrice; }
    
    public Double getHighPrice() { return highPrice; }
    public void setHighPrice(Double highPrice) { this.highPrice = highPrice; }
    
    public Double getLowPrice() { return lowPrice; }
    public void setLowPrice(Double lowPrice) { this.lowPrice = lowPrice; }
    
    public Double getClosePrice() { return closePrice; }
    public void setClosePrice(Double closePrice) { this.closePrice = closePrice; }
    
    public Double getLastPrice() { return lastPrice; }
    public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }
    
    public Double getPrevClose() { return prevClose; }
    public void setPrevClose(Double prevClose) { this.prevClose = prevClose; }
    
    public Long getTotalTradedQuantity() { return totalTradedQuantity; }
    public void setTotalTradedQuantity(Long totalTradedQuantity) { this.totalTradedQuantity = totalTradedQuantity; }
    
    public Double getTotalTradedValue() { return totalTradedValue; }
    public void setTotalTradedValue(Double totalTradedValue) { this.totalTradedValue = totalTradedValue; }
    
    public Double getMarketCap() { return marketCap; }
    public void setMarketCap(Double marketCap) { this.marketCap = marketCap; }
    
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }
    
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    // Helper methods
    public Double getDailyReturn() {
        if (prevClose != null && prevClose != 0) {
            return (closePrice - prevClose) / prevClose;
        }
        return 0.0;
    }
    
    public String getChangePercent() {
        if (prevClose != null && prevClose != 0) {
            double change = ((closePrice - prevClose) / prevClose) * 100;
            return String.format("%+.2f%%", change);
        }
        return "0.00%";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquityData that = (EquityData) o;
        return Objects.equals(symbol, that.symbol) && 
               Objects.equals(tradeDate, that.tradeDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbol, tradeDate);
    }
    
    @Override
    public String toString() {
        return "EquityData{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", tradeDate=" + tradeDate +
                ", closePrice=" + closePrice +
                ", change=" + getChangePercent() +
                '}';
    }
}