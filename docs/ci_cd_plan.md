# CI/CD Pipeline Plan

## Overview
Automated build, test, and deployment pipeline for Chimera MVP using GitHub Actions with security-first approach and zero-secrets-in-logs policy.

## Pipeline Architecture

### Branch Strategy
```yaml
Branches:
  main: Production-ready code, protected branch
  develop: Integration branch for features  
  feature/*: Feature development branches
  hotfix/*: Critical production fixes
  phase-m*/*: Phase-specific development branches

Protection Rules:
  main: Requires PR review + CI success + admin approval
  develop: Requires CI success + 1 reviewer
  feature/*: No protection (development freedom)
```

### Pipeline Triggers
```yaml
Push Events:
  main: Full pipeline + production deployment
  develop: Full pipeline + staging deployment  
  feature/*: Build + test only
  
Pull Request Events:
  To main/develop: Full validation pipeline
  To feature/*: Basic validation only
  
Scheduled Events:
  Daily: Dependency security scan
  Weekly: Full integration test suite
  Monthly: Performance regression testing
```

## Backend CI/CD Pipeline

### Stage 1: Code Quality & Security
```yaml
Jobs:
  secret-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run gitleaks
        uses: gitleaks/gitleaks-action@v2
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      - name: Fail on secrets detected
        if: failure()
        run: exit 1
        
  static-analysis:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run SpotBugs
        run: |
          cd backend
          mvn compile spotbugs:check
      - name: Run dependency check
        run: |
          cd backend  
          mvn org.owasp:dependency-check-maven:check
      - name: Upload SARIF results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: backend/target/spotbugs.sarif
```

### Stage 2: Build & Test
```yaml
Jobs:
  build-test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: test_password_123
          POSTGRES_DB: chimera_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
      redis:
        image: redis:7-alpine
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Cache Maven dependencies
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
          
      - name: Compile application
        run: |
          cd backend
          mvn clean compile -B -V
          
      - name: Run unit tests
        run: |
          cd backend
          mvn test -B -Dspring.profiles.active=test
        env:
          # Test database configuration (not secrets)
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/chimera_test
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: test_password_123
          SPRING_REDIS_HOST: localhost
          SPRING_REDIS_PORT: 6379
          
      - name: Run integration tests
        run: |
          cd backend
          mvn integration-test -B -Dspring.profiles.active=integration-test
          
      - name: Generate test coverage report
        run: |
          cd backend
          mvn jacoco:report
          
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          file: backend/target/site/jacoco/jacoco.xml
          fail_ci_if_error: true
```

### Stage 3: Package & Vulnerability Scan
```yaml
Jobs:
  package-scan:
    runs-on: ubuntu-latest
    needs: [build-test, static-analysis]
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Build application JAR
        run: |
          cd backend
          mvn package -B -DskipTests
          
      - name: Build Docker image
        run: |
          cd backend
          docker build -t chimera-backend:${{ github.sha }} .
          
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'chimera-backend:${{ github.sha }}'
          format: 'sarif'
          output: 'trivy-results.sarif'
          
      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-results.sarif'
```

## Android CI/CD Pipeline

### Stage 1: Lint & Security
```yaml
Jobs:
  android-lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Cache Gradle dependencies
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
            
      - name: Run Detekt
        run: |
          cd android
          ./gradlew detekt
          
      - name: Run Android Lint
        run: |
          cd android
          ./gradlew lintDebug
          
      - name: Upload lint results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: android-lint-results
          path: android/app/build/reports/
```

### Stage 2: Build & Test
```yaml
Jobs:
  android-build-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Build debug APK
        run: |
          cd android
          ./gradlew assembleDebug
          
      - name: Run unit tests
        run: |
          cd android  
          ./gradlew testDebugUnitTest
          
      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          target: google_apis
          arch: x86_64
          script: |
            cd android
            ./gradlew connectedDebugAndroidTest
```

## Deployment Pipelines

### Staging Deployment
```yaml
Jobs:
  deploy-staging:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    needs: [build-test, package-scan]
    environment: staging
    
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Fly.io staging
        uses: superfly/flyctl-actions/setup-flyctl@master
      - run: |
          cd backend
          flyctl deploy --app chimera-staging --config fly.staging.toml
        env:
          FLY_API_TOKEN: ${{ secrets.FLY_API_TOKEN }}
          
      - name: Run smoke tests
        run: |
          sleep 60  # Wait for deployment
          curl -f https://chimera-staging.fly.dev/health/ready
          curl -f https://chimera-staging.fly.dev/freshness
```

### Production Deployment
```yaml
Jobs:
  deploy-production:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    needs: [build-test, package-scan, android-build-test]
    environment: production
    
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Fly.io production
        uses: superfly/flyctl-actions/setup-flyctl@master
      - run: |
          cd backend
          flyctl deploy --app chimera-prod --config fly.production.toml
        env:
          FLY_API_TOKEN: ${{ secrets.FLY_API_TOKEN }}
          
      - name: Run production health checks
        run: |
          sleep 90  # Wait for deployment
          curl -f https://api.chimera.app/health/ready
          curl -f https://api.chimera.app/freshness
          
      - name: Notify deployment success
        uses: 8398a7/action-slack@v3
        with:
          status: success
          text: 'Production deployment successful'
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

## Security & Secrets Management

### Secret Classification
```yaml
Build Secrets (GitHub Secrets):
  FLY_API_TOKEN: Fly.io deployment token
  SLACK_WEBHOOK_URL: Deployment notifications
  CODECOV_TOKEN: Code coverage reporting
  SONAR_TOKEN: SonarQube analysis (optional)
  
Runtime Secrets (Fly.io Secrets):
  DATABASE_URL: PostgreSQL connection string
  REDIS_URL: Redis connection string
  SENTRY_DSN: Error tracking endpoint
  OPENAI_API_KEY: LLM API access
  JWT_SECRET_KEY: Token signing key
```

### Zero-Secrets-in-Logs Policy
```yaml
Log Sanitization Rules:
  - Automatically redact patterns like API keys, tokens, passwords
  - Mask environment variables containing "SECRET", "PASSWORD", "TOKEN", "KEY"
  - Replace sensitive values with "[REDACTED]" in all log outputs
  - Never log full URLs containing auth parameters
  
Enforcement:
  - Pre-commit hooks to detect potential secret logging
  - CI pipeline fails if log analysis detects patterns
  - Regular audit of deployed logs for leaked secrets
  - Automated secret rotation on suspected exposure
```

### Access Control
```yaml
Repository Access:
  - Main branch: Admin approval required for pushes
  - Develop branch: Team write access with review
  - Feature branches: Developer full access
  
Secret Access:
  - Production secrets: Admin only
  - Staging secrets: Senior developers
  - Test secrets: All developers (non-sensitive)
  
Deployment Access:
  - Production deployment: Manual approval required
  - Staging deployment: Automatic on develop branch
  - Feature deployment: Manual trigger only
```

## Quality Gates & PR Checks

### Required Checks (Cannot Merge Without)
```yaml
Backend Checks:
  ✅ All unit tests pass (100% required)
  ✅ Integration tests pass (100% required)  
  ✅ Code coverage ≥ 80% (line coverage)
  ✅ No critical security vulnerabilities
  ✅ No secrets detected in code
  ✅ Static analysis passes (SpotBugs)
  ✅ Build generates working JAR
  
Android Checks:
  ✅ Lint passes with no errors
  ✅ Unit tests pass (100% required)
  ✅ Build generates working APK
  ✅ No critical security issues
  
Documentation Checks:
  ✅ Phase completion report updated
  ✅ API documentation current (if applicable)
  ✅ Architecture decision records updated
```

### Advisory Checks (Warning Only)
```yaml
Performance Checks:
  ⚠️ Build time increase >20%
  ⚠️ JAR size increase >10%
  ⚠️ Test execution time >2x baseline
  ⚠️ Docker image size >500MB
  
Code Quality Checks:
  ⚠️ Code coverage decrease >5%
  ⚠️ Cyclomatic complexity >10
  ⚠️ TODO/FIXME comments added
  ⚠️ Dependency vulnerabilities (medium/low)
```

## Environment Configuration

### Development (Local)
```yaml
Triggers: Manual only
Tests: Full suite including slow tests
Secrets: Local .env file (not committed)
Database: Docker Compose PostgreSQL
Deployment: Local container only
```

### Staging (Feature Validation)
```yaml
Triggers: Push to develop branch
Tests: Full suite with integration tests
Secrets: GitHub Secrets → Fly.io Secrets
Database: Fly.io managed PostgreSQL (small)
Deployment: Automatic with health checks
```

### Production (Live Service)
```yaml
Triggers: Push to main branch + manual approval
Tests: Full suite + smoke tests post-deploy
Secrets: GitHub Secrets → Fly.io Secrets (encrypted)
Database: Fly.io managed PostgreSQL (production)
Deployment: Blue/green with rollback capability
```

## Performance & Optimization

### Build Optimization
```yaml
Parallelization:
  - Run backend and Android builds in parallel
  - Execute tests in parallel where possible
  - Cache dependencies aggressively
  - Use matrix builds for multiple configurations
  
Cache Strategy:
  - Maven dependencies: ~/.m2 cache
  - Gradle dependencies: ~/.gradle cache  
  - Docker layers: Multi-stage build optimization
  - Node modules: package-lock.json based caching
```

### Monitoring & Alerts
```yaml
Pipeline Health Monitoring:
  - Build success rate >95%
  - Average build time <10 minutes  
  - Test failure rate <2%
  - Deployment success rate >98%
  
Alert Conditions:
  🚨 Production deployment failure
  🚨 Critical security vulnerability detected
  🚨 Main branch build broken >1 hour
  ⚠️ Test success rate drops below 95%
  ⚠️ Build time increases >50% from baseline
```

## Rollback & Recovery

### Automated Rollback Triggers
```yaml
Health Check Failures:
  - /health/ready returns non-200 for >2 minutes
  - Error rate >5% for >5 minutes  
  - Response time p95 >5 seconds for >5 minutes
  - Memory usage >95% for >3 minutes
  
Manual Rollback Process:
  1. Identify last known good deployment
  2. Execute: flyctl releases rollback
  3. Verify health checks pass
  4. Notify team of rollback
  5. Create incident report
```

### Recovery Procedures
```yaml
Pipeline Failure Recovery:
  - Automatic retry for transient failures (network, etc.)
  - Manual intervention for persistent failures
  - Hotfix branch for critical production issues
  - Emergency deployment bypass for security patches
```

---

## GitHub Actions Workflow Files

### `.github/workflows/backend-ci.yml`
```yaml
name: Backend CI/CD

on:
  push:
    branches: [main, develop]
    paths: ['backend/**', '.github/workflows/backend-*.yml']
  pull_request:
    branches: [main, develop]
    paths: ['backend/**']

jobs:
  secret-scan:
    # [Implementation as specified above]
    
  build-test:
    # [Implementation as specified above]
    
  deploy-staging:
    # [Implementation as specified above]
    
  deploy-production:
    # [Implementation as specified above]
```

### `.github/workflows/android-ci.yml`
```yaml
name: Android CI

on:
  push:
    branches: [main, develop]
    paths: ['android/**', '.github/workflows/android-*.yml']
  pull_request:
    branches: [main, develop]  
    paths: ['android/**']

jobs:
  lint-test:
    # [Implementation as specified above]
    
  build:
    # [Implementation as specified above]
```

---

## Implementation Timeline

### Phase M1 (Current)
- [x] Document CI/CD strategy and requirements
- [ ] Create basic GitHub Actions workflow files
- [ ] Set up secret scanning and static analysis
- [ ] Configure build/test pipeline for backend

### Phase M2 (Data Ingestion)
- [ ] Add integration tests with real database
- [ ] Configure test data fixtures and cleanup
- [ ] Add performance testing for ingestion jobs

### Phase M7 (Deployment)
- [ ] Complete production deployment pipeline
- [ ] Set up monitoring and alerting
- [ ] Configure rollback procedures
- [ ] Validate end-to-end deployment flow

---
*Last Updated: 2025-08-10*  
*Implementation Owner: DevOps Team*  
*Review Schedule: Before each phase deployment*