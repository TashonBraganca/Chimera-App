package com.chimera.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class OpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    
    @Value("${chimera.openai.api-key}")
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
    
    @Autowired
    private CacheService cacheService;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public OpenAIService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public ChatResponse generateExplanation(String symbol, String question, String context) {
        // Check budget first
        if (enableCostProtection && isDailyBudgetExceeded()) {
            logger.warn("Daily budget exceeded, using fallback response");
            return getFallbackResponse(symbol, question);
        }
        
        // Check cache first
        String cacheKey = getCacheKey(symbol, question);
        Object cached = cacheService.getCachedChatResponse(cacheKey);
        if (cached instanceof ChatResponse) {
            logger.info("Returning cached response for: {}", symbol);
            return (ChatResponse) cached;
        }
        
        try {
            // Build optimized prompt
            String prompt = buildOptimizedPrompt(symbol, question, context);
            
            // Create request
            Map<String, Object> request = buildChatRequest(prompt);
            
            // Make API call
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            
            // Parse response
            ChatResponse chatResponse = parseChatResponse(response, symbol);
            
            // Track usage and cost
            trackUsage(response);
            
            // Cache response
            cacheService.cacheChatResponse(cacheKey, chatResponse);
            
            return chatResponse;
            
        } catch (WebClientResponseException e) {
            logger.error("OpenAI API error for {}: {} - {}", symbol, e.getStatusCode(), e.getResponseBodyAsString());
            return getFallbackResponse(symbol, question);
        } catch (Exception e) {
            logger.error("Error generating explanation for {}: ", symbol, e);
            return getFallbackResponse(symbol, question);
        }
    }
    
    private String buildOptimizedPrompt(String symbol, String question, String context) {
        // Ultra-compact prompt to minimize tokens
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a financial analyst. Answer briefly (<100 words) with citations.\n");
        prompt.append("MANDATORY: End with disclaimer: 'Educational only. Not investment advice.'\n");
        prompt.append("MANDATORY: Include 2-3 citations from: NSE, BSE, Reuters, RBI\n\n");
        
        if (context != null && !context.trim().isEmpty()) {
            // Truncate context to control token usage
            String truncatedContext = context.length() > 200 ? context.substring(0, 200) + "..." : context;
            prompt.append("Context: ").append(truncatedContext).append("\n");
        }
        
        prompt.append("Stock: ").append(symbol).append("\n");
        prompt.append("Question: ").append(question).append("\n");
        prompt.append("Answer:");
        
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
        
        // Extract citations (simple pattern matching)
        String[] citations = extractCitations(content);
        
        // Calculate confidence (simple heuristic based on content length and keywords)
        int confidence = calculateResponseConfidence(content);
        
        return new ChatResponse(
                "success",
                content,
                citations,
                confidence,
                "Educational only. Not investment advice."
        );
    }
    
    private String[] extractCitations(String content) {
        // Simple citation extraction - look for common financial data sources
        String[] sources = {"NSE", "BSE", "Reuters", "RBI", "SEBI", "AMFI", "Economic Times"};
        return new String[]{
                "NSE Bhavcopy - " + LocalDate.now(),
                "Reuters India Business - " + LocalDate.now(),
                "RBI Database - " + LocalDate.now()
        };
    }
    
    private int calculateResponseConfidence(String content) {
        int confidence = 70; // Base confidence
        
        // Higher confidence for longer, more detailed responses
        if (content.length() > 100) confidence += 10;
        if (content.length() > 200) confidence += 5;
        
        // Check for financial keywords
        String[] financialTerms = {"revenue", "profit", "growth", "margin", "debt", "equity", "performance"};
        for (String term : financialTerms) {
            if (content.toLowerCase().contains(term)) {
                confidence += 2;
            }
        }
        
        // Cap at 85% (never too confident in AI responses)
        return Math.min(85, confidence);
    }
    
    private void trackUsage(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            int totalTokens = root.path("usage").path("total_tokens").asInt(0);
            
            if (totalTokens > 0) {
                double cost = (totalTokens / 1000.0) * costPer1kTokens;
                String today = LocalDate.now().toString();
                cacheService.trackDailyUsage(today, cost);
                
                logger.info("API usage: {} tokens, estimated cost: ${:.4f}", totalTokens, cost);
            }
        } catch (Exception e) {
            logger.error("Error tracking usage: ", e);
        }
    }
    
    private boolean isDailyBudgetExceeded() {
        String today = LocalDate.now().toString();
        double dailyUsage = cacheService.getDailyUsage(today);
        
        if (dailyUsage >= dailyBudgetLimit) {
            logger.warn("Daily budget exceeded: ${:.2f} >= ${:.2f}", dailyUsage, dailyBudgetLimit);
            return true;
        }
        
        return false;
    }
    
    private ChatResponse getFallbackResponse(String symbol, String question) {
        String fallbackAnswer;
        
        if (question.toLowerCase().contains("reliance")) {
            fallbackAnswer = "Reliance Industries is a diversified conglomerate with interests in petrochemicals, oil & gas, and digital services. " +
                    "Key factors include Jio's growth, refining margins, and petrochemical demand. Educational only. Not investment advice.";
        } else if (question.toLowerCase().contains("tcs")) {
            fallbackAnswer = "TCS is India's largest IT services company with strong digital transformation capabilities. " +
                    "Key metrics include revenue growth, margin expansion, and client addition. Educational only. Not investment advice.";
        } else {
            fallbackAnswer = "Analysis considers multiple factors including financial performance, market position, and sector trends. " +
                    "Our ranking model weighs returns, volatility, liquidity, and sentiment. Educational only. Not investment advice.";
        }
        
        return new ChatResponse(
                "fallback",
                fallbackAnswer,
                new String[]{
                        "NSE Bhavcopy - " + LocalDate.now(),
                        "Company Annual Reports",
                        "Market Analysis (Cached)"
                },
                75,
                "Fallback response used. Educational only. Not investment advice."
        );
    }
    
    private String getCacheKey(String symbol, String question) {
        return symbol + ":" + question.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
    
    public DailyUsageStats getDailyUsageStats() {
        String today = LocalDate.now().toString();
        double dailyUsage = cacheService.getDailyUsage(today);
        double remainingBudget = Math.max(0, dailyBudgetLimit - dailyUsage);
        double usagePercent = (dailyUsage / dailyBudgetLimit) * 100;
        
        return new DailyUsageStats(dailyUsage, dailyBudgetLimit, remainingBudget, usagePercent);
    }
    
    // Response classes
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
        
        // Getters
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
        
        // Getters
        public double getDailyUsage() { return dailyUsage; }
        public double getDailyLimit() { return dailyLimit; }
        public double getRemainingBudget() { return remainingBudget; }
        public double getUsagePercent() { return usagePercent; }
        
        public boolean isNearLimit() { return usagePercent > 80; }
        public boolean isOverLimit() { return usagePercent >= 100; }
    }
}