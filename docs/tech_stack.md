# Technology Stack - Chimera MVP

## Overview
This document specifies the exact versions and technologies used in the Chimera MVP to ensure consistency across development, testing, and production environments.

## Backend Stack

### Runtime & Framework
| Component | Version | Justification |
|-----------|---------|---------------|
| **Java** | 21 (LTS) | Latest LTS with enhanced performance and language features |
| **Spring Boot** | 3.3.4 | Stable release with security updates, native compilation ready |
| **Spring Framework** | 6.1.x | Bundled with Spring Boot 3.3.4 |
| **Gradle** | 8.5 | Build tool with dependency management and performance optimization |

### Database & Storage
| Component | Version | Usage |
|-----------|---------|--------|
| **PostgreSQL** | 16.x | Primary database with JSON and advanced indexing |
| **TimescaleDB** | 2.13.0 | Time-series extension for OHLC and metrics data |
| **pgvector** | 0.1.4 | Vector similarity search for RAG embeddings |
| **Redis** | 7.x | Caching, session storage, rate limiting |
| **Flyway** | 9.22.3 | Database migrations and versioning |

### Libraries & Dependencies
| Component | Version | Purpose |
|-----------|---------|---------|
| **Spring Data JPA** | 3.3.x | ORM and database abstraction |
| **Spring Security** | 6.3.x | Authentication, authorization, rate limiting |
| **Jackson** | 2.17.x | JSON serialization/deserialization |
| **Resilience4j** | 2.1.0 | Circuit breakers, rate limiters, retries |
| **Micrometer** | 1.12.0 | Metrics collection and monitoring |
| **Apache Commons Math** | 3.6.1 | Statistical calculations and financial math |
| **Apache Commons Lang** | 3.14.0 | Utility functions and string operations |

### Observability & Monitoring
| Component | Version | Purpose |
|-----------|---------|---------|
| **Sentry** | 7.14.0 | Error tracking and performance monitoring |
| **Actuator** | 3.3.x | Health checks and metrics endpoints |
| **Prometheus** | Via Micrometer | Metrics collection format |
| **Logback** | 1.4.x | Structured logging with JSON output |

## Flutter Stack

### Core Platform
| Component | Version | Justification |
|-----------|---------|---------------|
| **Flutter** | 3.16+ | Latest stable framework with performance improvements |
| **Dart** | 3.2+ | Modern language with sound null safety |
| **Android SDK** | API 34 (Android 14) | Latest stable API level |
| **Minimum SDK** | API 21 (Android 5.0) | 98%+ device coverage |
| **Target SDK** | API 34 | Latest security and privacy features |

### State Management & Architecture
| Component | Version | Purpose |
|-----------|---------|---------|
| **Riverpod** | 2.5.1+ | Modern reactive state management |
| **GoRouter** | 14.2.7+ | Declarative routing and navigation |
| **Freezed** | 2.4.6+ | Immutable data classes and unions |
| **Json Annotation** | 4.8.1+ | JSON serialization code generation |

### Networking & Storage
| Component | Version | Purpose |
|-----------|---------|---------|
| **Dio** | 5.4.3+ | HTTP client with interceptors and error handling |
| **Drift** | 2.19.1+ | Type-safe SQL database for offline caching |
| **SharedPreferences** | 2.3.2+ | Simple key-value storage |
| **Connectivity Plus** | 6.0.5+ | Network connectivity detection |

### UI & Animations
| Component | Version | Purpose |
|-----------|---------|---------|
| **Material 3** | Built-in | Google Material Design system |
| **Flutter Animate** | 4.5.0+ | High-performance animations |
| **Shimmer** | 3.0.0+ | Loading state animations |
| **Cached Network Image** | 3.4.1+ | Efficient image loading and caching |

### Firebase Integration
| Component | Version | Purpose |
|-----------|---------|---------|
| **Firebase Core** | 3.6.0+ | Firebase SDK foundation |
| **Firebase Messaging** | 15.1.3+ | Push notifications (FCM) |
| **Firebase Analytics** | 11.3.3+ | App usage analytics (optional) |

## Development Tools

### Build & CI/CD
| Tool | Version | Purpose |
|------|---------|---------|
| **Gradle** | 8.5 | Backend build system with performance optimization |
| **Flutter CLI** | 3.16+ | Flutter app build and development tools |
| **GitHub Actions** | Latest | CI/CD pipeline for both backend and Flutter |
| **Docker** | 24.x | Backend containerization for Railway deployment |

### Code Quality
| Tool | Version | Purpose |
|------|---------|---------|
| **Flutter Lints** | 4.0+ | Dart/Flutter static analysis |
| **SpotBugs** | 4.8.0 | Java backend static analysis |
| **SonarQube** | Community | Code quality metrics |
| **gitleaks** | 8.x | Secret detection |

## Infrastructure & Deployment

### Platform (PaaS)
| Service | Provider | Purpose |
|---------|----------|---------|
| **App Hosting** | Fly.io | Container deployment and scaling |
| **Database** | Fly.io Postgres | Managed PostgreSQL with backups |
| **Cache** | Fly.io Redis | Managed Redis for caching |
| **Secrets** | Fly.io Secrets | Environment variable management |

### Monitoring & Observability
| Service | Provider | Purpose |
|---------|----------|---------|
| **Error Tracking** | Sentry | Exception monitoring and alerting |
| **Metrics** | Fly.io + Grafana | Infrastructure and app metrics |
| **Logs** | Fly.io Logs | Centralized log aggregation |
| **Uptime** | Fly.io Health | Health check monitoring |

## Version Compatibility Matrix

### Java/Spring Compatibility
```yaml
Java 21:
  Spring Boot: 3.3.x ✅
  Spring Framework: 6.1.x ✅
  Hibernate: 6.4.x ✅
  Gradle: 8.5 ✅
```

### Flutter/Dart Compatibility
```yaml
Flutter 3.16+:
  Dart: 3.2+ ✅
  Riverpod: 2.5.1+ ✅
  GoRouter: 14.2.7+ ✅
  Dio: 5.4.3+ ✅
```

### Database Compatibility
```yaml
PostgreSQL 16:
  TimescaleDB: 2.13.0 ✅
  pgvector: 0.1.4 ✅
  Spring Data JPA: 3.3.x ✅
```

## Environment-Specific Versions

### Development
```properties
# Local development versions
java.version=21
spring.boot.version=3.3.4
android.compileSdk=34
kotlin.version=1.9.25
```

### Staging
```properties  
# Staging environment - same as production
java.version=21
postgres.version=16
redis.version=7
```

### Production
```properties
# Production versions (locked)
java.version=21.0.4
postgres.version=16.4
redis.version=7.4
fly.io.platform=latest
```

## Upgrade Schedule

### Regular Updates (Monthly)
- Security patches for all dependencies
- Spring Boot patch releases
- Android Gradle Plugin updates
- Kotlin patch releases

### Major Updates (Quarterly)
- Spring Boot minor versions
- Compose BOM updates
- Database version upgrades
- Infrastructure platform updates

### Breaking Changes (As Needed)
- Java LTS upgrades (every 3 years)
- Android API level increases (annual)
- Major framework migrations
- Database major version upgrades

## Installation Requirements

### Developer Machine Setup

#### Backend Development
```bash
# Required installations
java --version  # OpenJDK 21
mvn --version   # Maven 3.9.x  
docker --version # Docker 24.x
psql --version  # PostgreSQL client 16.x
```

#### Flutter Development
```bash
# Required installations  
flutter --version    # Flutter 3.16+
dart --version       # Dart 3.2+
java --version       # OpenJDK 21 (for Android builds)
```

#### Verification Script
```bash
# Run from project root
./scripts/verify-toolchain.sh
```

## Dependency Management

### Security Scanning
```xml
<!-- Maven dependency check -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.2</version>
</plugin>
```

### Automated Updates
- Dependabot enabled for security updates
- Renovate for dependency management
- Weekly dependency review process
- Breaking change impact assessment

## Performance Benchmarks

### Backend Performance Targets
| Metric | Target | Measurement |
|--------|--------|-------------|
| Cold Start | < 30s | Time to /health/ready |
| Response Time | < 500ms p95 | /rank endpoint |
| Memory Usage | < 512MB | Steady state |
| CPU Usage | < 50% | Under load |

### Android Performance Targets
| Metric | Target | Measurement |
|--------|--------|-------------|
| Cold Start | < 1.2s p95 | App launch to first frame |
| Frame Time | < 16ms p95 | Compose rendering |
| Memory | < 150MB | Typical usage |
| Battery | < 2% per hour | Background idle |

---

## Validation Commands

### Backend Validation
```bash
cd backend
./gradlew clean build
./gradlew bootRun --args='--spring.profiles.active=dev'
curl http://localhost:8080/health/ready
```

### Flutter Validation
```bash
cd chimera_flutter
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```

### Docker Validation
```bash
cd backend
docker build -t chimera-backend .
docker run -p 8080:8080 chimera-backend
```

---
*Last Updated: 2025-08-10*  
*Next Review: 2025-09-10*  
*Version Freeze Date: Before Phase M7 deployment*