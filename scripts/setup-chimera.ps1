# Chimera MVP - Complete Setup Script
# Run with: PowerShell -ExecutionPolicy Bypass -File setup-chimera.ps1

param(
    [switch]$SkipInstalls,
    [switch]$TestOnly
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Chimera MVP - Automated Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if running as administrator
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "This script requires Administrator privileges!" -ForegroundColor Red
    Write-Host "Please right-click and 'Run as Administrator'" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Set-Location "D:\Chimera MVP"

# Function to check if command exists
function Test-Command {
    param($Command)
    try {
        Get-Command $Command -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

# Function to install with winget
function Install-WithWinget {
    param($Package, $Name)
    
    Write-Host "Installing $Name..." -ForegroundColor Yellow
    try {
        winget install $Package --accept-package-agreements --accept-source-agreements --silent
        Write-Host "✓ $Name installed successfully" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to install $Name" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
    }
}

# Install required tools
if (-not $SkipInstalls) {
    Write-Host "`nStep 1: Installing required tools..." -ForegroundColor Cyan
    
    if (-not (Test-Command "java")) {
        Install-WithWinget "Microsoft.OpenJDK.21" "Java 21"
    } else {
        Write-Host "✓ Java already installed" -ForegroundColor Green
    }
    
    if (-not (Test-Command "mvn")) {
        Install-WithWinget "Apache.Maven" "Maven"
    } else {
        Write-Host "✓ Maven already installed" -ForegroundColor Green
    }
    
    if (-not (Test-Command "docker")) {
        Install-WithWinget "Docker.DockerDesktop" "Docker Desktop"
        Write-Host "⚠ Docker Desktop installed. Please restart your computer!" -ForegroundColor Yellow
    } else {
        Write-Host "✓ Docker already installed" -ForegroundColor Green
    }
    
    if (-not (Test-Command "git")) {
        Install-WithWinget "Git.Git" "Git"
    } else {
        Write-Host "✓ Git already installed" -ForegroundColor Green
    }
    
    if (-not (Test-Command "node")) {
        Install-WithWinget "OpenJS.NodeJS" "Node.js"
    } else {
        Write-Host "✓ Node.js already installed" -ForegroundColor Green
    }
    
    # Refresh environment variables
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

if ($TestOnly) {
    Write-Host "`nTest Mode: Skipping setup, only testing..." -ForegroundColor Yellow
}

# Verify installations
Write-Host "`nStep 2: Verifying installations..." -ForegroundColor Cyan

$tools = @(
    @{Name="Java"; Command="java"; Args="--version"},
    @{Name="Maven"; Command="mvn"; Args="--version"},
    @{Name="Docker"; Command="docker"; Args="--version"},
    @{Name="Git"; Command="git"; Args="--version"}
)

$allGood = $true
foreach ($tool in $tools) {
    try {
        $version = & $tool.Command $tool.Args.Split() 2>&1 | Select-Object -First 1
        Write-Host "✓ $($tool.Name): $version" -ForegroundColor Green
    } catch {
        Write-Host "✗ $($tool.Name): Not found or not working" -ForegroundColor Red
        $allGood = $false
    }
}

if (-not $allGood -and -not $TestOnly) {
    Write-Host "`nSome tools are missing. Please restart your computer and run the script again." -ForegroundColor Red
    Read-Host "Press Enter to continue anyway or Ctrl+C to exit"
}

if (-not $TestOnly) {
    # Setup environment file
    Write-Host "`nStep 3: Setting up environment..." -ForegroundColor Cyan
    if (-not (Test-Path ".env")) {
        Copy-Item ".env.example" ".env"
        Write-Host "✓ Created .env from template" -ForegroundColor Green
        Write-Host "⚠ IMPORTANT: Edit .env file with your actual values!" -ForegroundColor Yellow
    } else {
        Write-Host "✓ .env file already exists" -ForegroundColor Green
    }
    
    # Start Docker services
    Write-Host "`nStep 4: Starting Docker services..." -ForegroundColor Cyan
    try {
        docker-compose up -d
        Write-Host "✓ Docker services started" -ForegroundColor Green
        
        # Wait for services
        Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30
        
        # Test database
        $dbTest = docker exec chimera-postgres pg_isready -U chimera -d chimera_local 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ PostgreSQL is ready" -ForegroundColor Green
        } else {
            Write-Host "⚠ PostgreSQL not ready yet" -ForegroundColor Yellow
        }
        
        # Test Redis
        $redisTest = docker exec chimera-redis redis-cli ping 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Redis is ready" -ForegroundColor Green
        } else {
            Write-Host "⚠ Redis not ready yet" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "✗ Failed to start Docker services" -ForegroundColor Red
        Write-Host "Make sure Docker Desktop is running" -ForegroundColor Yellow
    }
    
    # Build backend
    Write-Host "`nStep 5: Building backend..." -ForegroundColor Cyan
    Push-Location "backend"
    try {
        mvn clean compile -q
        Write-Host "✓ Backend compiled successfully" -ForegroundColor Green
        
        # Run tests
        Write-Host "Running tests..." -ForegroundColor Yellow
        mvn test -q
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ All tests passed" -ForegroundColor Green
        } else {
            Write-Host "⚠ Some tests failed" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "✗ Backend build failed" -ForegroundColor Red
    }
    Pop-Location
}

# Test endpoints if application is running
Write-Host "`nStep 6: Testing application (if running)..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/health/ready" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ Application is running and healthy!" -ForegroundColor Green
    Write-Host "Health endpoint returned: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "⚠ Application not running yet" -ForegroundColor Yellow
    Write-Host "To start: cd backend && mvn spring-boot:run" -ForegroundColor Cyan
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Edit .env file with your values" -ForegroundColor White
Write-Host "2. Run: .\scripts\start-application.bat" -ForegroundColor White
Write-Host "3. Test: .\scripts\test-endpoints.bat" -ForegroundColor White
Write-Host ""
Write-Host "Create accounts at:" -ForegroundColor Cyan
Write-Host "- Railway: https://railway.app" -ForegroundColor White
Write-Host "- Sentry: https://sentry.io" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"