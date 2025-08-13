@echo off
echo ========================================
echo Testing Chimera MVP Health Endpoints
echo ========================================
echo.

echo Testing health endpoints...
echo.

echo 1. Testing /health/ready:
curl -s http://localhost:8080/health/ready
echo.
echo.

echo 2. Testing /health/live:
curl -s http://localhost:8080/health/live
echo.
echo.

echo 3. Testing /freshness:
curl -s http://localhost:8080/freshness
echo.
echo.

echo 4. Testing management health:
curl -s http://localhost:8080/management/health
echo.
echo.

echo ========================================
echo Testing complete!
echo ========================================
pause