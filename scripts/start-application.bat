@echo off
echo ========================================
echo Starting Chimera MVP Application
echo ========================================
echo.

cd /d "D:\Chimera MVP"

echo Step 1: Starting Docker services...
docker-compose up -d

echo Waiting for services...
timeout /t 15 /nobreak >nul

echo.
echo Step 2: Checking service health...
docker-compose ps

echo.
echo Step 3: Starting backend application...
cd backend

echo Starting Spring Boot application...
echo Access URLs:
echo   Health: http://localhost:8080/health/ready
echo   API: http://localhost:8080
echo   Management: http://localhost:8080/management/health
echo.
echo Press Ctrl+C to stop the application
echo.

mvn spring-boot:run