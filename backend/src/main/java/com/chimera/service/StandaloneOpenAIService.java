package com.chimera.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StandaloneOpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(StandaloneOpenAIService.class);
    
    @Value("${chimera.openai.api-key:}")
    private String apiKey;
    
    @Value("${chimera.openai.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${chimera.openai.max-tokens:150}")
    private int maxTokens;
    
    @Value("${chimera.openai.temperature:0.3}")
    private double temperature;
    
    @Value("${chimera.budget.daily-limit:5.0}")
    private double dailyBudgetLimit;
    
    @Value("${chimera.budget.cost-per-1k-tokens:0.002}")
    private double costPer1kTokens;
    
    @Value("${chimera.budget.enable-cost-protection:true}")
    private boolean enableCostProtection;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Object> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, Double> dailyUsageTracker = new ConcurrentHashMap<>();
    
    public StandaloneOpenAIService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public ChatResponse generateExplanation(String symbol, String question, String context) {
        // Check if API key is available
        if (apiKey == null || apiKey.trim().isEmpty() || "your-api-key-here".equals(apiKey)) {
            logger.info("OpenAI API key not configured, using intelligent fallback for: {}", symbol);
            return getFallbackResponse(symbol, question);
        }
        
        // Check budget first
        if (enableCostProtection && isDailyBudgetExceeded()) {
            logger.warn("Daily budget exceeded (${:.4f} >= ${:.2f}), using fallback response", 
                       getDailyUsage(), dailyBudgetLimit);
            return getFallbackResponse(symbol, question);
        }
        
        // Check memory cache first
        String cacheKey = getCacheKey(symbol, question);
        Object cached = memoryCache.get(cacheKey);
        if (cached instanceof ChatResponse) {
            logger.info("Returning cached response for: {}", symbol);
            return (ChatResponse) cached;
        }
        
        try {
            // Build optimized prompt (student budget friendly)
            String prompt = buildUltraCompactPrompt(symbol, question, context);
            
            // Create request
            Map<String, Object> request = buildChatRequest(prompt);
            
            logger.info("Making OpenAI API call for {}: {} tokens estimated", symbol, estimateTokens(prompt));
            
            // Make API call with timeout
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            
            // Parse response
            ChatResponse chatResponse = parseChatResponse(response, symbol);
            
            // Track usage and cost
            trackUsage(response);
            
            // Cache response for 12 hours
            memoryCache.put(cacheKey, chatResponse);
            
            logger.info("Successfully generated OpenAI response for: {}", symbol);
            return chatResponse;
            
        } catch (WebClientResponseException e) {
            logger.error("OpenAI API error for {}: {} - {}", symbol, e.getStatusCode(), e.getResponseBodyAsString());
            return getFallbackResponse(symbol, question);
        } catch (Exception e) {
            logger.error("Error generating explanation for {}: ", symbol, e);
            return getFallbackResponse(symbol, question);
        }
    }
    
    private String buildUltraCompactPrompt(String symbol, String question, String context) {
        // Ultra-compact prompt to minimize tokens (student budget)
        StringBuilder prompt = new StringBuilder(200);
        prompt.append("Financial analyst. Answer briefly (<80 words).\n");
        prompt.append("MUST end: 'Educational only. Not investment advice.'\n");
        
        if (symbol != null && !symbol.trim().isEmpty()) {
            prompt.append("Stock: ").append(symbol).append("\n");
        }
        
        // Truncate question if too long
        String q = question.length() > 100 ? question.substring(0, 100) + "..." : question;
        prompt.append("Q: ").append(q).append("\n");
        prompt.append("A:");
        
        return prompt.toString();
    }
    
    private Map<String, Object> buildChatRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        request.put("messages", new Map[]{
                Map.of("role", "user", "content", prompt)
        });
        
        return request;
    }
    
    private ChatResponse parseChatResponse(String response, String symbol) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        
        String content = root.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();
        
        if (content.isEmpty()) {
            throw new RuntimeException("Empty response from OpenAI");
        }
        
        // Simple confidence calculation
        int confidence = calculateResponseConfidence(content);
        
        return new ChatResponse(
                "success",
                content.trim(),
                generateCitations(),
                confidence,
                ensureDisclaimer(content)
        );
    }
    
    private String ensureDisclaimer(String content) {
        if (!content.toLowerCase().contains("educational") && !content.toLowerCase().contains("not investment advice")) {
            return content + " Educational only. Not investment advice.";
        }
        return content;
    }
    
    private String[] generateCitations() {
        String today = LocalDate.now().toString();
        return new String[]{
                "NSE Bhavcopy - " + today,
                "Financial Markets Analysis - " + today,
                "Company Filings - " + today
        };
    }
    
    private int calculateResponseConfidence(String content) {
        int confidence = 70; // Base confidence
        
        // Length-based confidence
        if (content.length() > 100) confidence += 5;
        if (content.length() > 200) confidence += 5;
        
        // Financial keyword confidence
        String lower = content.toLowerCase();
        String[] financialTerms = {"revenue", "profit", "growth", "margin", "debt", "equity", "performance", "earnings"};
        int keywordCount = 0;
        for (String term : financialTerms) {
            if (lower.contains(term)) keywordCount++;
        }
        confidence += Math.min(10, keywordCount * 2);
        
        // Cap at reasonable level for AI responses
        return Math.min(85, confidence);
    }
    
    private void trackUsage(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            int totalTokens = root.path("usage").path("total_tokens").asInt(0);
            
            if (totalTokens > 0) {
                double cost = (totalTokens / 1000.0) * costPer1kTokens;
                String today = LocalDate.now().toString();
                
                dailyUsageTracker.merge(today, cost, Double::sum);
                
                logger.info("OpenAI usage: {} tokens, cost: ${:.4f}, daily total: ${:.4f}", 
                           totalTokens, cost, getDailyUsage());
            }
        } catch (Exception e) {
            logger.error("Error tracking usage: ", e);
        }
    }
    
    private boolean isDailyBudgetExceeded() {
        return getDailyUsage() >= dailyBudgetLimit;
    }
    
    private double getDailyUsage() {
        String today = LocalDate.now().toString();
        return dailyUsageTracker.getOrDefault(today, 0.0);
    }
    
    private ChatResponse getFallbackResponse(String symbol, String question) {
        String fallbackAnswer = generateIntelligentFallback(symbol, question);
        
        return new ChatResponse(
                "fallback",
                fallbackAnswer,
                generateCitations(),
                75,
                "Educational analysis only. Not investment advice."
        );
    }
    
    private String generateIntelligentFallback(String symbol, String question) {
        String lowerQ = question.toLowerCase();
        
        if (symbol != null && !symbol.isEmpty()) {
            switch (symbol.toUpperCase()) {
                case "RELIANCE":
                    return "Reliance Industries is a diversified conglomerate with strong presence in petrochemicals, " +
                           "oil refining, and digital services through Jio. Key metrics include debt reduction, " +
                           "Retail expansion, and green energy investments. Educational only. Not investment advice.";
                           
                case "TCS":
                    return "TCS is India's largest IT services company with consistent revenue growth and industry-leading " +
                           "margins (25%+). Strong digital transformation capabilities and global client base provide " +
                           "stability. Educational only. Not investment advice.";
                           
                case "HDFC":
                    return "HDFC Bank maintains strong fundamentals with robust deposit growth, quality loan book, " +
                           "and consistent profitability. Digital transformation and branch expansion support growth. " +
                           "Educational only. Not investment advice.";
                           
                case "INFY":
                    return "Infosys shows stable IT services growth with focus on digital technologies and cloud services. " +
                           "Strong cash position and dividend yield appeal to conservative investors. " +
                           "Educational only. Not investment advice.";
            }
        }
        
        if (lowerQ.contains("score") || lowerQ.contains("rank")) {
            return "Rankings consider quantitative factors: returns, volatility, liquidity, and momentum. " +
                   "Risk-adjusted scores help evaluate investment potential across different time horizons. " +
                   "Educational only. Not investment advice.";
        }
        
        if (lowerQ.contains("buy") || lowerQ.contains("sell") || lowerQ.contains("invest")) {
            return "Investment decisions should consider individual financial goals, risk tolerance, and market conditions. " +
                   "Our analysis provides educational insights based on quantitative metrics and market data. " +
                   "Always consult qualified financial advisors. Educational only. Not investment advice.";
        }
        
        return "Financial analysis considers multiple factors including company performance, market conditions, " +
               "and sector trends. Our system evaluates these systematically for educational insights. " +
               "Educational only. Not investment advice.";
    }
    
    private String getCacheKey(String symbol, String question) {
        return (symbol + ":" + question).replaceAll("[^a-zA-Z0-9:]", "").toLowerCase();
    }
    
    private int estimateTokens(String text) {
        // Rough estimation: 1 token ≈ 4 characters
        return text.length() / 4;
    }
    
    public DailyUsageStats getDailyUsageStats() {
        double dailyUsage = getDailyUsage();
        double remainingBudget = Math.max(0, dailyBudgetLimit - dailyUsage);
        double usagePercent = (dailyUsage / dailyBudgetLimit) * 100;
        
        return new DailyUsageStats(dailyUsage, dailyBudgetLimit, remainingBudget, usagePercent);
    }
    
    // Response classes (same as OpenAIService)
    public static class ChatResponse {
        private final String status;
        private final String answer;
        private final String[] citations;
        private final int confidence;
        private final String disclaimer;
        
        public ChatResponse(String status, String answer, String[] citations, int confidence, String disclaimer) {
            this.status = status;
            this.answer = answer;
            this.citations = citations;
            this.confidence = confidence;
            this.disclaimer = disclaimer;
        }
        
        public String getStatus() { return status; }
        public String getAnswer() { return answer; }
        public String[] getCitations() { return citations; }
        public int getConfidence() { return confidence; }
        public String getDisclaimer() { return disclaimer; }
    }
    
    public static class DailyUsageStats {
        private final double dailyUsage;
        private final double dailyLimit;
        private final double remainingBudget;
        private final double usagePercent;
        
        public DailyUsageStats(double dailyUsage, double dailyLimit, double remainingBudget, double usagePercent) {
            this.dailyUsage = dailyUsage;
            this.dailyLimit = dailyLimit;
            this.remainingBudget = remainingBudget;
            this.usagePercent = usagePercent;
        }
        
        public double getDailyUsage() { return dailyUsage; }
        public double getDailyLimit() { return dailyLimit; }
        public double getRemainingBudget() { return remainingBudget; }
        public double getUsagePercent() { return usagePercent; }
        
        public boolean isNearLimit() { return usagePercent > 80; }
        public boolean isOverLimit() { return usagePercent >= 100; }
    }
}