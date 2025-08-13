# Infrastructure Architecture Decisions

## Purpose
Document chosen infrastructure stack and secret management approach for Chimera MVP deployment.

## Recommended Platform-as-a-Service (PaaS) Track

### Primary Choice: **Railway**
```yaml
Platform: Railway
Reasoning: 
  - Free tier available for MVP development
  - Excellent GitHub integration for auto-deployments
  - Docker-native deployment support
  - Simple deployment process with minimal configuration
  - Built-in observability and logging
  - Automatic HTTPS and custom domains
  - Competitive pricing for small scale
  - Strong developer experience

Estimated Monthly Cost (MVP):
  - Free tier: $0 (500 hours/month execution time)
  - Pro tier: $5-20/month (after free tier usage)
  - Total: ~$0-20/month (significantly cheaper than Fly.io)
```

### Alternative Options

#### Option 2: **Fly.io** 
```yaml
Platform: Fly.io
Pros:
  - Global edge deployment with India region proximity
  - Managed Postgres and Redis
  - Advanced networking features
  - Mature platform
Cons:
  - No free tier (costs from day 1)
  - More complex pricing structure
  - Higher minimum costs (~$35/month)
```

#### Option 3: **Render**
```yaml
Platform: Render
Pros:
  - Easy setup and deployment
  - Free tier available
  - Auto-scaling
Cons:
  - Limited to US/Europe regions (higher latency for India)
  - Less control over infrastructure
```

## Secret Management Strategy

### Recommended: **Railway Environment Variables**
```yaml
Secret Store: Railway Environment Variables
Approach:
  - Use Railway's built-in environment variable management
  - Environment variable injection at deployment time
  - No secrets in code or configuration files
  - Easy rotation through Railway dashboard/CLI

Storage Locations:
  - Database credentials: Railway environment variables
  - API keys (LLM, News): Railway environment variables  
  - JWT signing keys: Railway environment variables
  - Encryption keys: Railway environment variables
```

### Secret Categories & Handling

#### Database Secrets
```bash
# Managed by platform, auto-injected
DATABASE_URL=postgresql://user:pass@host:port/db
REDIS_URL=redis://user:pass@host:port
```

#### External API Keys
```bash
# Set via Railway dashboard or CLI
OPENAI_API_KEY=[Your OpenAI API key]
NEWS_API_KEY=[Your News API key]
SENTRY_DSN=... # Optional
FIREBASE_CONFIG=... # For Flutter FCM
```

#### Application Secrets
```bash
# JWT and encryption
JWT_SECRET_KEY=...
ENCRYPTION_KEY=...
SESSION_SECRET=...
```

## Database Architecture

### Primary Database: **PostgreSQL 16 + TimescaleDB**
```yaml
Database: Managed PostgreSQL 16
Extensions:
  - TimescaleDB for time-series data (prices, metrics)
  - pgvector for embeddings (RAG system)
  - pg_stat_statements for query performance

Schema Design:
  - Time-series tables for OHLC data
  - Document tables for news/filings
  - Vector tables for embeddings
  - Configuration tables for feature flags
```

### Cache Layer: **Redis 7**
```yaml
Cache: Managed Redis
Usage:
  - API response caching (5-15 min TTL)
  - Session storage
  - Rate limiting counters
  - Background job queues
```

## Application Architecture

### Backend: **Spring Boot 3.3.x + Java 21**
```yaml
Framework: Spring Boot 3.3.x
Runtime: Java 21 (LTS)
Build: Gradle with Docker multi-stage builds
Packaging: Fat JAR in minimal container for Railway deployment

Key Dependencies:
  - Spring Data JPA + Hibernate
  - Spring Security for rate limiting/auth
  - Spring Boot Actuator for health checks
  - Spring Cache with Redis
  - Jackson for JSON processing
  - Resilience4j for circuit breakers
```

### Flutter: **Flutter + Dart**
```yaml
Framework: Flutter 3.16+
Language: Dart 3.2+
Architecture: Riverpod state management with Repository pattern
Networking: Dio HTTP client
Database: Drift for offline caching
Build: Flutter CLI with pub dependency management
```

## Deployment Strategy

### Container Strategy
```dockerfile
# Multi-stage build
FROM openjdk:21-jdk-slim as builder
# Build application

FROM openjdk:21-jre-slim as runtime  
# Minimal runtime image
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/health/ready || exit 1
```

### Environment Configuration
```yaml
Environments:
  - development: Local Docker Compose
  - staging: Railway staging environment
  - production: Railway production environment

Configuration:
  - Environment variables for all config
  - No hardcoded values in application.properties
  - Profile-specific overrides (dev/staging/prod)
```

## Observability & Monitoring

### Health Check Strategy
```yaml
Health Endpoints:
  - /health/ready: Application + dependencies ready
  - /health/live: Application running (for liveness probe)
  - /freshness: Data freshness summary
  - /metrics: Prometheus metrics

Monitoring Stack:
  - Platform native metrics (Railway dashboard)
  - Sentry for error tracking (optional)
  - Custom metrics via Micrometer
```

### Logging Strategy
```yaml
Logging:
  - Structured JSON logs
  - No PII in logs
  - Centralized via platform log aggregation
  - Log levels: ERROR (production), DEBUG (development)
  
Security:
  - Redact sensitive data
  - API keys masked in logs
  - User data anonymized
```

## Data Residency & Compliance

### Geographic Considerations
```yaml
Data Residency:
  - Primary: ap-south-1 (Mumbai) preferred
  - Fallback: sin (Singapore) for Fly.io
  - User data: Keep within India/APAC when possible
  
DPDP Compliance:
  - Data processing consent recorded
  - Right to deletion implemented
  - Cross-border transfer safeguards
```

## Security Configuration

### Network Security
```yaml
Security:
  - TLS 1.3 for all external connections
  - Internal service-to-service encryption
  - No public database access
  - VPC/private networking where available
  
API Security:
  - Rate limiting (100 req/min per IP)
  - Input validation and sanitization
  - CORS policy restrictive
  - No unnecessary endpoints exposed
```

### Access Control
```yaml
Access:
  - Principle of least privilege
  - Separate credentials per environment
  - Regular credential rotation
  - MFA for platform access
  - Audit logs for admin actions
```

## Cost Optimization

### MVP Cost Targets
```yaml
Target Monthly Costs:
  - Infrastructure: $0-20/month (Railway free tier + Pro)
  - External APIs: <$30/month (LLM usage)
  - Total: <$50/month for first 1000 users

Cost Controls:
  - Auto-scaling limits set
  - API usage monitoring and alerts
  - Free tiers maximized where possible
```

### Scaling Considerations
```yaml
Scale-up Triggers:
  - >1000 daily active users: Consider dedicated instances
  - >10k requests/day: Optimize database queries
  - >$100/month API costs: Implement smarter caching
  
Scale-out Plan:
  - Horizontal scaling via platform auto-scaling
  - Database read replicas if needed
  - CDN for static assets
```

## Disaster Recovery

### Backup Strategy
```yaml
Backups:
  - Database: Daily automated backups (7-day retention)
  - Secrets: Platform managed with version history
  - Application: Git repository + Docker images
  - Configuration: Infrastructure as code
```

### Recovery Procedures
```yaml
Recovery:
  - RTO: 4 hours (Recovery Time Objective)
  - RPO: 24 hours (Recovery Point Objective)
  - Runbook documented for common failure scenarios
  - Tested quarterly in staging environment
```

## Migration Path

### MVP to Scale Migration
```yaml
Migration Strategy:
  - Phase 1: PaaS deployment (MVP)
  - Phase 2: Managed services optimization  
  - Phase 3: Consider cloud-native (AWS/GCP) if >10k users
  - Phase 4: Multi-region deployment if needed
```

---

## Decision Log

### Why PaaS over Cloud-Native?
- **Speed**: Faster to deploy and iterate
- **Cost**: Lower operational overhead for MVP
- **Expertise**: Less DevOps expertise required initially
- **Risk**: Lower risk of over-engineering

### Why Railway over Others?
- **Cost**: Free tier for MVP development
- **Simplicity**: Easiest deployment experience
- **GitHub**: Excellent CI/CD integration
- **Speed**: Fastest time to market

### Why Not Self-Hosted?
- **Maintenance**: Too much operational burden for MVP
- **Security**: Platform handles many security concerns
- **Compliance**: Platform provides compliance frameworks
- **Cost**: Hidden costs of self-management

---

## Human Intervention Required

Before proceeding with M7:
1. **✅ Infrastructure choice approved**: Railway selected
2. **✅ API Keys provided**: OpenAI and News API keys configured
3. **Set budget limits**: Monthly spend thresholds and alerts ($50/month max)
4. **Create Railway account**: Sign up and connect to GitHub repository

---
*Last Updated: 2025-08-10*
*Decision Owner: [PENDING APPROVAL]*
*Implementation Target: Phase M1*