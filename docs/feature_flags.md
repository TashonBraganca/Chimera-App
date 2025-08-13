# Feature Flags Configuration

## Purpose
Control experimental, risky, or premium features through configuration flags. All risky sources and experimental features are OFF by default for MVP.

## Flag Categories

### Data Sources (High Risk - OFF by default)

#### Social/Community Data
```yaml
# Reddit Financial Discussions
reddit_sentiment_enabled: false
reddit_wsb_sentiment: false
reddit_investing_sentiment: false
# Risk: Unverified user content, potential manipulation

# Twitter/X Financial Sentiment  
twitter_sentiment_enabled: false
twitter_fintwit_analysis: false
# Risk: API costs, content moderation challenges

# Discord Financial Communities
discord_sentiment_enabled: false
# Risk: Private data, TOS violations
```

#### Broker/Proprietary Data
```yaml
# Broker Research Reports
broker_research_enabled: false
zerodha_research_integration: false
upstox_research_integration: false
# Risk: Copyright infringement, access restrictions

# Proprietary Trading Signals
proprietary_signals_enabled: false
algorithmic_trading_signals: false
# Risk: Regulatory compliance, signal quality
```

#### Experimental Technologies
```yaml
# Quantum Computing Signals
quantum_computing_enabled: false
quantum_optimization_models: false
# Risk: Unproven technology, high costs

# Neuromorphic Computing
neuromorphic_models_enabled: false
spiking_neural_networks: false
# Risk: Experimental, no proven financial benefit

# Satellite/Alternative Data
satellite_data_enabled: false
economic_nowcasting_satellite: false
# Risk: High costs, unclear ROI
```

### Core Features (Safe - Default ON)

#### Essential Data Sources
```yaml
# Official Market Data
nse_bse_data_enabled: true
amfi_nav_enabled: true
rbi_data_enabled: true
mospi_data_enabled: true
fbil_rates_enabled: true

# News Sources
reuters_rss_enabled: true
pib_rss_enabled: true
gdelt_india_enabled: true
google_trends_enabled: true
```

#### Core Analytics
```yaml
# Ranking System
asset_ranking_enabled: true
multi_horizon_analysis: true
risk_adjusted_scoring: true
calibration_enabled: true

# LLM Explanations  
llm_explanations_enabled: true
citation_enforcement: true
disclaimer_injection: true
```

### Performance & Scaling

#### Caching & Optimization
```yaml
# Data Caching
redis_caching_enabled: true
response_caching_ttl: 300  # 5 minutes
data_prefetching_enabled: false  # MVP: false

# Database Optimization
read_replicas_enabled: false  # MVP: false
query_optimization_enabled: true
connection_pooling_enabled: true
```

#### Rate Limiting
```yaml
# API Rate Limits
global_rate_limit_enabled: true
per_user_rate_limit: true
burst_limit_enabled: true

# Resource Limits
max_concurrent_queries: 100
query_timeout_seconds: 30
max_response_tokens: 1000
```

### Monetization (OFF by default)

#### Premium Features
```yaml
# Subscription Gating
freemium_gating_enabled: false
premium_features_enabled: false
usage_limits_enabled: false

# Premium Data
real_time_data_enabled: false
institutional_data_enabled: false
```

#### Advertising
```yaml
# Ad Integration
admob_enabled: false
ad_frequency_capping: true
ads_on_advice_screens: false  # Never allow ads near financial advice

# A/B Testing
ab_testing_enabled: false  # MVP: disable for simplicity
```

### Development & Debugging

#### Logging & Monitoring
```yaml
# Debug Logging
debug_logging_enabled: false  # Production: false
detailed_query_logging: false  # Privacy: false
performance_logging_enabled: true

# Monitoring
health_checks_enabled: true
metrics_collection_enabled: true
alerting_enabled: true
```

#### Development Tools
```yaml
# Development Features
hot_reload_enabled: false  # Production: false
debug_ui_enabled: false    # Production: false
test_data_enabled: false   # Production: false
```

## Environment-Specific Overrides

### Development Environment
```yaml
environment: development
debug_logging_enabled: true
test_data_enabled: true
rate_limiting_relaxed: true
llm_response_caching_disabled: true  # For testing
```

### Staging Environment  
```yaml
environment: staging
debug_logging_enabled: false
rate_limiting_enabled: true
real_data_subset: true
monitoring_enabled: true
```

### Production Environment
```yaml
environment: production
debug_logging_enabled: false
all_risky_sources_disabled: true
rate_limiting_strict: true
monitoring_comprehensive: true
alerting_critical_only: true
```

## Flag Management

### Configuration Sources (Priority Order)
1. Environment Variables (highest priority)
2. Database configuration table
3. Configuration files (lowest priority)

### Environment Variable Format
```bash
# Format: CHIMERA_FEATURE_<FLAG_NAME>
export CHIMERA_FEATURE_REDDIT_SENTIMENT_ENABLED=false
export CHIMERA_FEATURE_LLM_EXPLANATIONS_ENABLED=true
export CHIMERA_FEATURE_DEBUG_LOGGING_ENABLED=false
```

### Database Schema
```sql
CREATE TABLE feature_flags (
    flag_name VARCHAR(100) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    risk_level VARCHAR(20), -- LOW, MEDIUM, HIGH, CRITICAL
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(100),
    rollout_percentage INTEGER DEFAULT 0  -- For gradual rollouts
);
```

## Safety Mechanisms

### Risk Categories
- **LOW**: Core features, safe by default
- **MEDIUM**: Performance features, may impact costs
- **HIGH**: Experimental sources, regulatory risk
- **CRITICAL**: Never enable without legal review

### Automatic Safeguards
```yaml
# Automatic Disabling
auto_disable_on_error: true
error_threshold_disable: 5  # Disable after 5 consecutive errors
cost_threshold_disable: true
regulatory_violation_auto_disable: true

# Manual Override Requirements
critical_flags_require_approval: true
high_risk_flags_require_justification: true
```

## Rollout Strategy

### Gradual Feature Rollout
```yaml
# Beta Testing
beta_user_percentage: 5    # 5% of users get new features first
beta_duration_days: 7      # Beta period before full rollout
rollback_on_issues: true   # Auto-rollback if issues detected

# Full Rollout Criteria
error_rate_threshold: 0.1  # <0.1% error rate required
performance_impact_max: 10 # <10% performance impact
user_feedback_score_min: 4 # >4.0/5.0 user satisfaction
```

### Emergency Procedures
```yaml
# Kill Switch
emergency_disable_all_experimental: true
emergency_contact_method: "slack_alert"
rollback_procedure_documented: true
escalation_path_defined: true
```

## Monitoring & Alerts

### Flag Usage Tracking
- Track which flags are actively used
- Monitor performance impact per flag
- Cost tracking for premium features
- User adoption rates for new features

### Alert Conditions
```yaml
# Performance Alerts
response_time_increase_threshold: 50  # Alert if >50% slower
error_rate_increase_threshold: 10     # Alert if >10x errors
cost_spike_threshold: 200             # Alert if >200% cost increase

# Security Alerts  
unauthorized_flag_changes: immediate_alert
high_risk_flag_enabled: management_alert
data_source_tos_violation: legal_alert
```

---

## Implementation Notes

### MVP Phase Priorities
1. **Phase M0-M2**: Only safe flags enabled (official data sources)
2. **Phase M3-M4**: Core analytics and LLM features
3. **Phase M5-M8**: Performance optimization flags
4. **Phase M9+**: Consider premium/monetization flags

### Code Integration
```java
// Example usage in code
@Component
public class FeatureFlags {
    @Value("${chimera.feature.reddit_sentiment_enabled:false}")
    private boolean redditSentimentEnabled;
    
    public boolean isRedditSentimentEnabled() {
        return redditSentimentEnabled && !isHighRiskDisabled();
    }
}
```

---
*Last Updated: 2025-08-10*
*Review Required: Product Team*
*Next Review: Before each phase milestone*