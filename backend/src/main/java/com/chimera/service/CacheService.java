package com.chimera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    // Cache duration constants
    private static final Duration RANKING_CACHE_DURATION = Duration.ofMinutes(30);
    private static final Duration CHAT_CACHE_DURATION = Duration.ofHours(12);
    private static final Duration DAILY_USAGE_CACHE_DURATION = Duration.ofDays(1);
    
    private boolean isRedisAvailable() {
        return redisTemplate != null;
    }
    
    public void put(String key, Object value) {
        put(key, value, RANKING_CACHE_DURATION);
    }
    
    public void put(String key, Object value, Duration duration) {
        if (!isRedisAvailable()) {
            logger.debug("Redis not available, skipping cache put for key: {}", key);
            return;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, duration.toMillis(), TimeUnit.MILLISECONDS);
            logger.debug("Cached value for key: {} with TTL: {}", key, duration);
        } catch (Exception e) {
            logger.error("Error caching value for key {}: ", key, e);
        }
    }
    
    public <T> T get(String key, Class<T> type) {
        if (!isRedisAvailable()) {
            logger.debug("Redis not available, cache miss for key: {}", key);
            return null;
        }
        
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                logger.debug("Cache hit for key: {}", key);
                return type.cast(value);
            }
            logger.debug("Cache miss for key: {}", key);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving cached value for key {}: ", key, e);
            return null;
        }
    }
    
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                logger.debug("Cache hit for key: {}", key);
            } else {
                logger.debug("Cache miss for key: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("Error retrieving cached value for key {}: ", key, e);
            return null;
        }
    }
    
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Error checking if key exists {}: ", key, e);
            return false;
        }
    }
    
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            logger.debug("Deleted cache key: {}", key);
        } catch (Exception e) {
            logger.error("Error deleting cache key {}: ", key, e);
        }
    }
    
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Deleted {} cache keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            logger.error("Error deleting cache keys with pattern {}: ", pattern, e);
        }
    }
    
    public long increment(String key) {
        return increment(key, 1);
    }
    
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            logger.error("Error incrementing cache key {}: ", key, e);
            return 0;
        }
    }
    
    public void expire(String key, Duration duration) {
        try {
            redisTemplate.expire(key, duration.toMillis(), TimeUnit.MILLISECONDS);
            logger.debug("Set TTL for key: {} to {}", key, duration);
        } catch (Exception e) {
            logger.error("Error setting TTL for key {}: ", key, e);
        }
    }
    
    // Specialized cache methods for different data types
    public void cacheRankings(String requestKey, Object rankings) {
        put("rankings:" + requestKey, rankings, RANKING_CACHE_DURATION);
    }
    
    public Object getCachedRankings(String requestKey) {
        return get("rankings:" + requestKey);
    }
    
    public void cacheChatResponse(String question, Object response) {
        String key = "chat:" + question.hashCode();
        put(key, response, CHAT_CACHE_DURATION);
    }
    
    public Object getCachedChatResponse(String question) {
        String key = "chat:" + question.hashCode();
        return get(key);
    }
    
    // Cost tracking
    public void trackDailyUsage(String date, double cost) {
        String key = "usage:" + date;
        try {
            Double currentUsage = get(key, Double.class);
            double newUsage = (currentUsage != null ? currentUsage : 0.0) + cost;
            put(key, newUsage, DAILY_USAGE_CACHE_DURATION);
            logger.debug("Updated daily usage for {}: ${:.4f}", date, newUsage);
        } catch (Exception e) {
            logger.error("Error tracking daily usage: ", e);
        }
    }
    
    public double getDailyUsage(String date) {
        String key = "usage:" + date;
        Double usage = get(key, Double.class);
        return usage != null ? usage : 0.0;
    }
    
    // Rate limiting
    public boolean isRateLimited(String clientId, int maxRequests, Duration window) {
        String key = "rate_limit:" + clientId;
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            
            if (currentCount == 1) {
                // First request in window - set expiration
                expire(key, window);
            }
            
            boolean limited = currentCount > maxRequests;
            if (limited) {
                logger.warn("Rate limit exceeded for client: {} ({} > {})", clientId, currentCount, maxRequests);
            }
            
            return limited;
        } catch (Exception e) {
            logger.error("Error checking rate limit for client {}: ", clientId, e);
            return false; // Allow request on error
        }
    }
    
    public void clearAllCaches() {
        try {
            // Clear specific cache patterns
            deletePattern("rankings:*");
            deletePattern("chat:*");
            deletePattern("usage:*");
            deletePattern("rate_limit:*");
            logger.info("Cleared all caches");
        } catch (Exception e) {
            logger.error("Error clearing all caches: ", e);
        }
    }
    
    public CacheStats getCacheStats() {
        try {
            Set<String> allKeys = redisTemplate.keys("*");
            int totalKeys = allKeys != null ? allKeys.size() : 0;
            
            Set<String> rankingKeys = redisTemplate.keys("rankings:*");
            int rankingCacheSize = rankingKeys != null ? rankingKeys.size() : 0;
            
            Set<String> chatKeys = redisTemplate.keys("chat:*");
            int chatCacheSize = chatKeys != null ? chatKeys.size() : 0;
            
            return new CacheStats(totalKeys, rankingCacheSize, chatCacheSize);
        } catch (Exception e) {
            logger.error("Error getting cache stats: ", e);
            return new CacheStats(0, 0, 0);
        }
    }
    
    public static class CacheStats {
        private final int totalKeys;
        private final int rankingCacheSize;
        private final int chatCacheSize;
        
        public CacheStats(int totalKeys, int rankingCacheSize, int chatCacheSize) {
            this.totalKeys = totalKeys;
            this.rankingCacheSize = rankingCacheSize;
            this.chatCacheSize = chatCacheSize;
        }
        
        public int getTotalKeys() { return totalKeys; }
        public int getRankingCacheSize() { return rankingCacheSize; }
        public int getChatCacheSize() { return chatCacheSize; }
        
        @Override
        public String toString() {
            return "CacheStats{" +
                    "totalKeys=" + totalKeys +
                    ", rankingCacheSize=" + rankingCacheSize +
                    ", chatCacheSize=" + chatCacheSize +
                    '}';
        }
    }
}