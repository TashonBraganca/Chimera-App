@echo off
echo ========================================
echo Chimera MVP - Windows Setup Script
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as Administrator... Good!
) else (
    echo This script requires Administrator privileges.
    echo Please right-click and "Run as Administrator"
    pause
    exit /b 1
)

echo.
echo Step 1: Installing Java 21...
winget install Microsoft.OpenJDK.21 --accept-package-agreements --accept-source-agreements
if %errorlevel% neq 0 echo Failed to install Java 21

echo.
echo Step 2: Installing Maven...
winget install Apache.Maven --accept-package-agreements --accept-source-agreements
if %errorlevel% neq 0 echo Failed to install Maven

echo.
echo Step 3: Installing Docker Desktop...
winget install Docker.DockerDesktop --accept-package-agreements --accept-source-agreements
if %errorlevel% neq 0 echo Failed to install Docker

echo.
echo Step 4: Installing Git...
winget install Git.Git --accept-package-agreements --accept-source-agreements
if %errorlevel% neq 0 echo Failed to install Git

echo.
echo Step 5: Installing Node.js...
winget install OpenJS.NodeJS --accept-package-agreements --accept-source-agreements
if %errorlevel% neq 0 echo Failed to install Node.js

echo.
echo ========================================
echo Installation complete!
echo Please restart your terminal and run:
echo   setup-development.bat
echo ========================================
pause