# Health & Observability Plan

## Overview
Comprehensive monitoring and health check strategy for Chimera MVP backend service to ensure reliability, performance, and operational visibility.

## Health Endpoints

### Core Health Checks

#### `/health/ready` - Readiness Probe
```yaml
Purpose: Determine if service is ready to accept traffic
Response Time: < 200ms p95
Dependencies Checked:
  - Database connection (PostgreSQL)
  - Redis cache connectivity  
  - External API availability (optional)
  - Data freshness status

Success Criteria:
  - All critical dependencies responsive
  - Last data ingestion within acceptable window
  - Memory usage below threshold (80%)
  - Active connections within limits

Response Format:
{
  "status": "UP|DOWN",
  "timestamp": "2025-08-10T10:30:00Z",
  "checks": {
    "database": {"status": "UP", "responseTime": "15ms"},
    "redis": {"status": "UP", "responseTime": "5ms"},
    "dataFreshness": {"status": "UP", "lastUpdate": "2025-08-10T09:45:00Z"},
    "memory": {"status": "UP", "usage": "65%"}
  }
}
```

#### `/health/live` - Liveness Probe
```yaml
Purpose: Determine if service is running (for container restart decisions)
Response Time: < 50ms p95
Checks: Minimal - just service responsiveness
Dependencies: None (lightweight check)

Response Format:
{
  "status": "UP",
  "timestamp": "2025-08-10T10:30:00Z",
  "uptime": "PT2H15M"
}
```

### Data Quality Health Checks

#### `/freshness` - Data Freshness Summary
```yaml
Purpose: Monitor data ingestion pipeline health and timeliness
Audience: Internal monitoring + user-facing status

Response Format:
{
  "status": "HEALTHY|DEGRADED|UNHEALTHY",
  "timestamp": "2025-08-10T10:30:00Z",
  "sources": {
    "nse_bhavcopy": {
      "lastSuccess": "2025-08-10T18:45:00+05:30",
      "ageMinutes": 35,
      "status": "FRESH",
      "recordCount": 1847,
      "anomalies": 0
    },
    "amfi_nav": {
      "lastSuccess": "2025-08-10T21:15:00+05:30", 
      "ageMinutes": 75,
      "status": "FRESH",
      "recordCount": 15432,
      "anomalies": 2
    },
    "gdelt_news": {
      "lastSuccess": "2025-08-10T10:15:00+05:30",
      "ageMinutes": 15, 
      "status": "FRESH",
      "recordCount": 156,
      "anomalies": 0
    }
  },
  "aggregateHealth": {
    "freshSources": 3,
    "staleSources": 0,
    "failingSources": 0,
    "totalAnomalies": 2
  }
}
```

#### Health Status Definitions
```yaml
FRESH: Data age within expected window (< 2x normal cadence)
STALE: Data age beyond expected window but < 24 hours  
FAILING: No successful update in 24+ hours
ANOMALY: Z-score > 6 for volume/price/count metrics
```

## Observability Stack

### Metrics Collection

#### Application Metrics (Micrometer + Prometheus)
```yaml
Categories:
  Business Metrics:
    - ranking_requests_total: Counter of /rank API calls
    - explanation_requests_total: Counter of /explain API calls  
    - user_queries_total: Counter of chat queries
    - data_ingestion_success_total: Counter by source
    - calibration_accuracy: Gauge for precision@k
    
  Performance Metrics:
    - http_request_duration_seconds: Histogram by endpoint
    - database_query_duration_seconds: Histogram by query type
    - cache_hit_ratio: Gauge for Redis hit rate
    - llm_response_time_seconds: Histogram for AI responses
    
  System Metrics:
    - jvm_memory_used_bytes: JVM memory usage
    - jvm_gc_duration_seconds: Garbage collection times
    - database_connections_active: Connection pool usage
    - redis_connections_active: Redis connection count

Labels Strategy:
  - endpoint: /rank, /explain, /chat, /freshness
  - source: nse, bse, amfi, gdelt, reuters, pib
  - status: success, error, timeout
  - horizon: 1d, 1w, 1m (for ranking requests)
```

#### Custom Business Metrics
```yaml
Data Quality Metrics:
  - chimera_data_freshness_minutes: Age of last successful ingestion by source
  - chimera_data_anomalies_total: Count of detected anomalies by source
  - chimera_data_volume_records: Record count per ingestion by source
  
Ranking Quality Metrics:  
  - chimera_ranking_confidence_score: Distribution of confidence scores
  - chimera_ranking_precision_at_k: Precision@5, precision@10 metrics
  - chimera_ranking_kendall_tau: Ranking correlation metric
  
User Experience Metrics:
  - chimera_explanation_citation_count: Citations per response
  - chimera_chat_refusal_rate: Percentage of refused queries  
  - chimera_uncertainty_rate: Percentage of "uncertain" responses
```

### Logging Strategy

#### Structured JSON Logging (Logback)
```json
{
  "timestamp": "2025-08-10T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.chimera.controller.RankingController",
  "message": "Ranking request processed",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "userId": "anonymous_12345",
  "endpoint": "/rank",
  "duration": 245,
  "statusCode": 200,
  "responseSize": 2048,
  "metadata": {
    "amountInr": 100000,
    "horizonDays": 30,
    "resultCount": 10,
    "confidenceThreshold": 0.85
  }
}
```

#### Log Levels & Content
```yaml
ERROR:
  - Unhandled exceptions with full stack traces
  - Data ingestion failures with source details
  - External API failures with retry context
  - Security violations and rate limit breaches
  
WARN:
  - Data anomalies detected with z-scores
  - Cache misses above threshold
  - Slow queries above p95 threshold
  - Circuit breaker state changes
  
INFO:
  - Request/response for all API endpoints
  - Data ingestion success with record counts  
  - Model calibration updates
  - Feature flag changes
  
DEBUG: (Development only)
  - SQL queries with parameters
  - Cache operations and hit rates
  - Detailed ranking calculations
  - LLM prompt/response pairs (sanitized)
```

#### PII Redaction & Security
```yaml
Automatic Redaction:
  - User identifiers: Hash to anonymous IDs
  - Financial amounts: Round to ranges (e.g., "100K-500K")
  - Query text: Sanitize personal references
  - IP addresses: Truncate last octet
  
Never Log:
  - API keys or secrets
  - Full user financial portfolios
  - Personally identifiable information
  - Sensitive business logic parameters
```

### Error Tracking (Sentry)

#### Sentry Configuration
```yaml
Environment Variables:
  SENTRY_DSN: https://key@sentry.io/project-id
  SENTRY_ENVIRONMENT: development|staging|production
  SENTRY_RELEASE: 0.1.0-SNAPSHOT
  SENTRY_TRACES_SAMPLE_RATE: 0.1

Integration Features:
  - Exception capture with context
  - Performance transaction monitoring  
  - Release tracking and deployment markers
  - User feedback collection
  - Custom fingerprinting for similar errors
```

#### Error Categorization
```yaml
Business Errors:
  - Data source unavailable
  - Ranking confidence below threshold
  - LLM service timeout
  - Invalid user input parameters
  
Technical Errors:  
  - Database connection failures
  - Redis cache unavailable
  - Memory allocation issues
  - Thread pool exhaustion
  
Security Errors:
  - Rate limit exceeded
  - Invalid JWT tokens
  - Suspicious request patterns
  - Input validation failures
```

## Alerting Strategy

### Alert Channels
```yaml
Critical Alerts (PagerDuty/Phone):
  - Service completely down
  - Database connectivity lost
  - Memory usage > 90%
  - Error rate > 10%
  
High Priority (Slack):
  - Data ingestion failing > 2 hours
  - API latency p95 > 2 seconds
  - Cache hit rate < 50%
  - Anomaly detection spike
  
Medium Priority (Email):
  - Data staleness warnings
  - Ranking accuracy degradation  
  - LLM timeout increase
  - Storage usage > 80%
  
Low Priority (Dashboard):
  - Performance trends
  - Usage pattern changes
  - Feature flag modifications
  - Deployment notifications
```

### Alert Thresholds
```yaml
Performance Alerts:
  - API Response Time: p95 > 2.5s (5 min window)
  - Database Query Time: p95 > 500ms (5 min window)  
  - Memory Usage: > 85% (sustained 10 min)
  - CPU Usage: > 80% (sustained 15 min)
  
Data Quality Alerts:
  - Data Age: > 2x expected cadence
  - Anomaly Count: > 10 per hour per source
  - Record Count: < 50% of expected
  - Ingestion Failures: > 3 consecutive failures
  
Business Logic Alerts:
  - Ranking Confidence: Average < 0.7 over 1 hour
  - Uncertain Rate: > 30% of responses
  - Citation Failures: > 5% of explanations
  - Refusal Rate: > 15% of chat queries
```

## Monitoring Dashboard

### Grafana Dashboard Panels

#### Service Overview
```yaml
Panels:
  - Service Status: UP/DOWN with uptime percentage
  - Request Rate: Requests per minute by endpoint
  - Response Times: p50/p95/p99 latencies
  - Error Rates: 4xx/5xx error percentages
  - Active Users: Unique request sources per hour
```

#### Data Pipeline Health
```yaml  
Panels:
  - Data Freshness: Time since last update by source
  - Ingestion Success Rate: Success/failure ratio
  - Record Volume: Daily ingestion counts by source
  - Anomaly Detection: Flagged anomalies over time
  - Processing Lag: Time from source publish to ingestion
```

#### Business Metrics
```yaml
Panels:
  - Ranking Requests: Volume and success rate
  - Explanation Usage: Chat/explain request volume
  - Confidence Distribution: Histogram of confidence scores
  - Uncertainty Rate: Percentage of uncertain responses
  - User Satisfaction: Implicit feedback metrics
```

### SLA Tracking
```yaml
Service Level Objectives:
  Availability: 99.5% uptime (target), 99.0% (threshold)
  Latency: 
    - /rank endpoint: p95 < 2.5s
    - /explain endpoint: p95 < 3.0s  
    - /freshness endpoint: p95 < 200ms
  Data Freshness: 95% of sources updated within 2x cadence
  Error Rate: < 1% of requests result in 5xx errors
```

## Deployment Health Gates

### Pre-Deployment Validation
```yaml
Health Gate Checks:
  - All unit tests pass
  - Integration tests with real DB pass
  - Health endpoints respond correctly
  - Memory/CPU within normal ranges
  - No critical security vulnerabilities
```

### Post-Deployment Validation  
```yaml
Rollout Validation (5 minutes):
  - Health checks green
  - Error rates within baseline
  - Response times within SLA
  - Data ingestion continuing normally
  - No new critical alerts firing
  
Rollback Triggers:
  - Health checks failing > 2 minutes
  - Error rate > 5x baseline
  - Response time > 2x baseline  
  - Critical alerts firing
  - Memory usage > 95%
```

## Development vs Production

### Development Environment
```yaml
Logging: DEBUG level enabled
Metrics: Exported to local Prometheus
Health Checks: Include detailed diagnostics
Sentry: Development project with all traces
Alerts: None (local development)
```

### Staging Environment
```yaml
Logging: INFO level, structured JSON
Metrics: Full production metrics
Health Checks: Production configuration
Sentry: Staging project, sample rate 50%
Alerts: Slack only, relaxed thresholds
```

### Production Environment
```yaml
Logging: WARN/ERROR only, PII redacted
Metrics: Full suite with alerting
Health Checks: Minimal response time
Sentry: Production project, sample rate 10%
Alerts: Full escalation chain active
```

---

## Implementation Checklist

### Phase M1 (Foundation)
- [x] Document health endpoint specifications
- [x] Plan metrics collection strategy
- [x] Define logging format and levels
- [x] Specify alert channels and thresholds

### Phase M2 (Implementation)
- [ ] Implement /health/ready and /health/live
- [ ] Add Micrometer metrics collection
- [ ] Configure structured JSON logging
- [ ] Integrate Sentry error tracking

### Phase M7 (Deployment)
- [ ] Set up Grafana dashboards
- [ ] Configure production alerting
- [ ] Validate health gate automation
- [ ] Test rollback procedures

---
*Last Updated: 2025-08-10*  
*Review Schedule: After each phase completion*  
*Alert Contact: [TO BE CONFIGURED IN M7]*