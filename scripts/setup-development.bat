@echo off
echo ========================================
echo Chimera MVP - Development Environment Setup
echo ========================================
echo.

cd /d "D:\Chimera MVP"

echo Step 1: Verifying tools installation...
java --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Please run setup-windows.bat first.
    pause
    exit /b 1
)

mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Please run setup-windows.bat first.
    pause
    exit /b 1
)

docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker not found. Please install Docker Desktop manually.
    pause
    exit /b 1
)

echo All tools found! Continuing...
echo.

echo Step 2: Setting up environment file...
if not exist .env (
    copy .env.example .env >nul
    echo Created .env file from template
    echo IMPORTANT: Edit .env file with your actual values!
) else (
    echo .env file already exists
)

echo.
echo Step 3: Starting Docker services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ERROR: Failed to start Docker services
    echo Make sure Docker Desktop is running
    pause
    exit /b 1
)

echo Waiting for services to start...
timeout /t 30 /nobreak >nul

echo.
echo Step 4: Testing database connection...
docker exec chimera-postgres pg_isready -U chimera -d chimera_local >nul 2>&1
if %errorlevel% eq 0 (
    echo ✓ PostgreSQL is ready
) else (
    echo ⚠ PostgreSQL not ready yet, may need more time
)

docker exec chimera-redis redis-cli ping >nul 2>&1
if %errorlevel% eq 0 (
    echo ✓ Redis is ready
) else (
    echo ⚠ Redis not ready yet, may need more time
)

echo.
echo Step 5: Building backend application...
cd backend
mvn clean compile -q
if %errorlevel% eq 0 (
    echo ✓ Backend compiled successfully
) else (
    echo ⚠ Backend compilation failed, check logs
)

echo.
echo Step 6: Running tests...
mvn test -q
if %errorlevel% eq 0 (
    echo ✓ All tests passed
) else (
    echo ⚠ Some tests failed, check logs
)

cd ..

echo.
echo ========================================
echo Setup Complete!
echo.
echo To start the application:
echo   1. cd backend
echo   2. mvn spring-boot:run
echo.
echo To test health endpoints:
echo   curl http://localhost:8080/health/ready
echo.
echo Next steps:
echo   1. Edit .env file with your values
echo   2. Create Railway account at railway.app
echo   3. Create Sentry account at sentry.io
echo ========================================
pause