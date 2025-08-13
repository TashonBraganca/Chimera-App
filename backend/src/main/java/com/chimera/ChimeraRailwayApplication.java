package com.chimera;

import com.chimera.dto.RankingRequest;
import com.chimera.dto.RankingResponse;
import com.chimera.service.RankingService;
import com.chimera.service.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Railway-specific application entry point.
 * Disables database auto-configuration and excludes conflicting application classes.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    RedisAutoConfiguration.class
})
@ComponentScan(basePackages = "com.chimera", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        ChimeraMinimalApplication.class,
        ChimeraStandaloneApplication.class
    })
})
@RestController
public class ChimeraRailwayApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(ChimeraRailwayApplication.class);
    
    @Autowired(required = false)
    private RankingService rankingService;
    
    @Autowired(required = false)
    private OpenAIService openAIService;

    public static void main(String[] args) {
        SpringApplication.run(ChimeraRailwayApplication.class, args);
    }
    
    // Home page
    @GetMapping("/")
    public String home() {
        return """
            <html>
            <head><title>Chimera MVP Backend - Railway</title></head>
            <body style='font-family: Arial; margin: 40px; background: #1976D2; color: white; text-align: center;'>
                <h1>🚀 Chimera MVP Backend (Railway)</h1>
                <h2>✅ RUNNING SUCCESSFULLY!</h2>
                <p><strong>Version:</strong> 0.1.0-SNAPSHOT</p>
                <p><strong>Platform:</strong> Railway Deployment</p>
                <p><strong>Phase:</strong> M7 - Deployment Complete</p>
                <p><strong>Time:</strong> %s</p>
                <hr style='margin: 20px 0;'>
                <h3>Available APIs:</h3>
                <ul style='text-align: left; display: inline-block;'>
                    <li><a href='/actuator/health' style='color: white;'>GET /actuator/health</a> - Health check</li>
                    <li><strong>POST /api/rank</strong> - Asset rankings</li>
                    <li><strong>POST /api/chat</strong> - Chat with explanations</li>
                </ul>
                <p><em>Railway deployment ready for Flutter app!</em></p>
            </body>
            </html>
            """.formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    // Ranking endpoint
    @CrossOrigin(origins = {"*"})
    @PostMapping("/api/rank")
    public ResponseEntity<RankingResponse> getRankings(@Valid @RequestBody RankingRequest request) {
        try {
            logger.info("Processing ranking request: {}", request);
            
            if (rankingService != null) {
                RankingResponse response = rankingService.generateRankings(request);
                logger.info("Successfully generated {} rankings", response.getRankings().size());
                return ResponseEntity.ok(response);
            } else {
                // Create mock response when service not available
                RankingResponse mockResponse = createMockRankingResponse();
                return ResponseEntity.ok(mockResponse);
            }
            
        } catch (Exception e) {
            logger.error("Error processing ranking request: ", e);
            RankingResponse errorResponse = createMockRankingResponse();
            errorResponse.setStatus("error");
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // Chat endpoint - integrated with OpenAI
    @CrossOrigin(origins = {"*"})
    @PostMapping("/api/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            String question = (String) request.getOrDefault("question", "");
            String assetId = (String) request.getOrDefault("assetId", "");
            
            if (question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Question is required"
                ));
            }
            
            logger.info("Processing chat request for asset: {}, question: {}", assetId, question);
            
            // Use real OpenAI service if available
            if (openAIService != null) {
                try {
                    OpenAIService.ChatResponse chatResponse = openAIService.generateExplanation(
                        assetId.isEmpty() ? "GENERAL" : assetId, 
                        question, 
                        "Financial ranking analysis context"
                    );
                    
                    Map<String, Object> response = Map.of(
                        "status", chatResponse.getStatus(),
                        "answer", chatResponse.getAnswer(),
                        "citations", List.of(chatResponse.getCitations()),
                        "confidence", chatResponse.getConfidence(),
                        "lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " IST",
                        "disclaimer", chatResponse.getDisclaimer()
                    );
                    
                    return ResponseEntity.ok(response);
                    
                } catch (Exception e) {
                    logger.error("OpenAI service error, falling back to mock response: ", e);
                    // Fall through to mock response
                }
            }
            
            // Fallback response when OpenAI service is not available or fails
            Map<String, Object> response = Map.of(
                "status", "fallback",
                "answer", "This is an educational analysis based on our ranking system. " +
                         "The ranking considers financial performance, market trends, and risk metrics. " +
                         "This analysis is for educational purposes only and should not be considered as investment advice.",
                "citations", List.of(
                    Map.of("source", "NSE Bhavcopy", "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "title", "Market Data"),
                    Map.of("source", "AMFI NAV", "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "title", "Mutual Fund Data"),
                    Map.of("source", "RBI Database", "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "title", "Economic Indicators")
                ),
                "confidence", 70,
                "lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " IST",
                "disclaimer", "Fallback response - Educational purposes only. Not investment advice."
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat request: ", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Chat service temporarily unavailable",
                "disclaimer", "Educational purposes only. Not investment advice."
            ));
        }
    }
    
    private RankingResponse createMockRankingResponse() {
        List<RankingResponse.AssetRankingDto> mockRankings = List.of(
            new RankingResponse.AssetRankingDto("RELIANCE", "Reliance Industries Ltd.", 0.87, 92, 1, "BUY", 2850.50, "+2.3%"),
            new RankingResponse.AssetRankingDto("TCS", "Tata Consultancy Services Ltd.", 0.84, 89, 2, "BUY", 4125.75, "+1.8%"),
            new RankingResponse.AssetRankingDto("INFY", "Infosys Ltd.", 0.81, 85, 3, "BUY", 1875.25, "+1.2%"),
            new RankingResponse.AssetRankingDto("HDFC", "HDFC Bank Ltd.", 0.78, 82, 4, "HOLD", 2650.00, "+0.9%"),
            new RankingResponse.AssetRankingDto("ICICI", "ICICI Bank Ltd.", 0.75, 79, 5, "HOLD", 1235.60, "+0.5%")
        );
        
        RankingResponse.RankingMetadata metadata = new RankingResponse.RankingMetadata(50, 5, "Railway Mock Data");
        metadata.setDisclaimer("Railway deployment - Educational analysis only. Not investment advice.");
        return new RankingResponse(mockRankings, metadata);
    }
}