package com.chimera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Health and monitoring endpoints for operational visibility.
 * 
 * Implements the health check specifications from docs/health_observability_plan.md
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    
    private final Instant startTime = Instant.now();
    
    /**
     * Readiness probe - determines if service is ready to accept traffic
     * Checks critical dependencies: database, cache, data freshness
     */
@GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", ZonedDateTime.now(ZoneId.of("UTC")).toString());
        
        Map<String, Object> checks = new HashMap<>();
        
        // Database check (placeholder - will implement in M2)
        Map<String, Object> dbCheck = new HashMap<>();
        dbCheck.put("status", "UP");
        dbCheck.put("responseTime", "15ms");
        checks.put("database", dbCheck);
        
        // Redis check (placeholder - will implement in M2)  
        Map<String, Object> cacheCheck = new HashMap<>();
        cacheCheck.put("status", "UP");
        cacheCheck.put("responseTime", "5ms");
        checks.put("redis", cacheCheck);
        
        // Data freshness check (placeholder - will implement in M2)
        Map<String, Object> freshnessCheck = new HashMap<>();
        freshnessCheck.put("status", "UP");
        freshnessCheck.put("lastUpdate", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        checks.put("dataFreshness", freshnessCheck);
        
        // Memory usage check
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        Map<String, Object> memoryCheck = new HashMap<>();
        memoryCheck.put("status", memoryUsagePercent < 80 ? "UP" : "DOWN");
        memoryCheck.put("usage", String.format("%.0f%%", memoryUsagePercent));
        checks.put("memory", memoryCheck);
        
        response.put("checks", checks);
        
        // Determine overall status
        boolean allHealthy = checks.values().stream()
            .allMatch(check -> "UP".equals(((Map<?, ?>) check).get("status")));
        
        response.put("status", allHealthy ? "UP" : "DOWN");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Liveness probe - determines if service is running (for container restarts)
     * Minimal check, no external dependencies
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", ZonedDateTime.now(ZoneId.of("UTC")).toString());
        
        // Calculate uptime
        long uptimeSeconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        response.put("uptime", String.format("PT%dM%dS", uptimeSeconds / 60, uptimeSeconds % 60));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Data freshness summary - monitor data pipeline health
     * Will be fully implemented in M2 with actual data sources
     */
    @GetMapping("/freshness")
    public ResponseEntity<Map<String, Object>> freshness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "HEALTHY");
        response.put("timestamp", ZonedDateTime.now(ZoneId.of("UTC")).toString());
        
        // Placeholder data sources (will implement actual checks in M2)
        Map<String, Object> sources = new HashMap<>();
        
        // NSE Bhavcopy placeholder
        Map<String, Object> nseSource = new HashMap<>();
        nseSource.put("lastSuccess", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        nseSource.put("ageMinutes", 35);
        nseSource.put("status", "FRESH");
        nseSource.put("recordCount", 0); // Will be populated in M2
        nseSource.put("anomalies", 0);
        sources.put("nse_bhavcopy", nseSource);
        
        // AMFI NAV placeholder
        Map<String, Object> amfiSource = new HashMap<>();
        amfiSource.put("lastSuccess", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        amfiSource.put("ageMinutes", 75);
        amfiSource.put("status", "FRESH");
        amfiSource.put("recordCount", 0); // Will be populated in M2
        amfiSource.put("anomalies", 0);
        sources.put("amfi_nav", amfiSource);
        
        response.put("sources", sources);
        
        // Aggregate health summary
        Map<String, Object> aggregateHealth = new HashMap<>();
        aggregateHealth.put("freshSources", 2);
        aggregateHealth.put("staleSources", 0);
        aggregateHealth.put("failingSources", 0);
        aggregateHealth.put("totalAnomalies", 0);
        response.put("aggregateHealth", aggregateHealth);
        
        return ResponseEntity.ok(response);
    }
    
    // Spring Boot Actuator provides built-in health indicators
    // Custom health logic is available via /health/ready and /health/live endpoints
}