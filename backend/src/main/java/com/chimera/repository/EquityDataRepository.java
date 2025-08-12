package com.chimera.repository;

import com.chimera.model.EquityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquityDataRepository extends JpaRepository<EquityData, Long> {
    
    // Find latest price for a symbol
    Optional<EquityData> findTopBySymbolOrderByTradeDateDesc(String symbol);
    
    // Find all data for a symbol within date range
    @Query("SELECT ed FROM EquityData ed WHERE ed.symbol = :symbol AND " +
           "ed.tradeDate BETWEEN :startDate AND :endDate ORDER BY ed.tradeDate DESC")
    List<EquityData> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    
    // Find all symbols for a specific date
    List<EquityData> findByTradeDateOrderBySymbolAsc(LocalDate tradeDate);
    
    // Find latest trading date
    @Query("SELECT MAX(ed.tradeDate) FROM EquityData ed")
    LocalDate findLatestTradingDate();
    
    // Find symbols with data in the last N days
    @Query("SELECT DISTINCT ed.symbol FROM EquityData ed WHERE ed.tradeDate >= :since ORDER BY ed.symbol")
    List<String> findActiveSymbolsSince(@Param("since") LocalDate since);
    
    // Find price history for multiple symbols
    @Query("SELECT ed FROM EquityData ed WHERE ed.symbol IN :symbols AND " +
           "ed.tradeDate BETWEEN :startDate AND :endDate ORDER BY ed.symbol, ed.tradeDate DESC")
    List<EquityData> findBySymbolsAndDateRange(@Param("symbols") List<String> symbols,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    // Get top performing stocks by daily return
    @Query("SELECT ed FROM EquityData ed WHERE ed.tradeDate = :date AND " +
           "ed.prevClose IS NOT NULL AND ed.prevClose > 0 AND " +
           "((ed.closePrice - ed.prevClose) / ed.prevClose) > 0 " +
           "ORDER BY ((ed.closePrice - ed.prevClose) / ed.prevClose) DESC")
    List<EquityData> findTopGainersByDate(@Param("date") LocalDate date);
    
    // Get worst performing stocks by daily return
    @Query("SELECT ed FROM EquityData ed WHERE ed.tradeDate = :date AND " +
           "ed.prevClose IS NOT NULL AND ed.prevClose > 0 AND " +
           "((ed.closePrice - ed.prevClose) / ed.prevClose) < 0 " +
           "ORDER BY ((ed.closePrice - ed.prevClose) / ed.prevClose) ASC")
    List<EquityData> findTopLosersByDate(@Param("date") LocalDate date);
    
    // Find high volume stocks
    @Query("SELECT ed FROM EquityData ed WHERE ed.tradeDate = :date AND " +
           "ed.totalTradedQuantity IS NOT NULL " +
           "ORDER BY ed.totalTradedQuantity DESC")
    List<EquityData> findHighVolumeStocks(@Param("date") LocalDate date);
    
    // Get market statistics for a date
    @Query("SELECT COUNT(ed), AVG(ed.closePrice), AVG(ed.totalTradedQuantity) " +
           "FROM EquityData ed WHERE ed.tradeDate = :date")
    Object[] getMarketStatistics(@Param("date") LocalDate date);
    
    // Check if data exists for a date
    boolean existsByTradeDate(LocalDate date);
    
    // Count unique symbols
    @Query("SELECT COUNT(DISTINCT ed.symbol) FROM EquityData ed WHERE ed.tradeDate >= :since")
    Long countUniqueSymbols(@Param("since") LocalDate since);
    
    // Find by sector
    List<EquityData> findBySectorAndTradeDateOrderByClosePrice(String sector, LocalDate tradeDate);
    
    // Get price range for symbol
    @Query("SELECT MIN(ed.closePrice), MAX(ed.closePrice) FROM EquityData ed WHERE " +
           "ed.symbol = :symbol AND ed.tradeDate BETWEEN :startDate AND :endDate")
    Object[] getPriceRange(@Param("symbol") String symbol,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);
}