# ================================================================
#  start.ps1 - One-command launcher for MicroQuest
#  Usage:  .\start.ps1        (from project root)
#       or  start              (via start.bat)
#
#  What it does:
#    1. Ensures PostgreSQL 17 service is running
#    2. Starts Spring Boot via Maven (logs stream here)
#    3. Opens Chrome once http://localhost:8080 is responsive
#
#  Prerequisites on this machine:
#    - JDK 21         : C:\jdk21\jdk-21.0.8
#    - Maven 3.9      : C:\tools\maven\maven-3.9.14
#    - PostgreSQL 17  : Windows service postgresql-x64-17
#    - DB + user 'microquest' already created (password: microquest)
# ================================================================

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot

# Set JDK 21 and PostgreSQL in PATH for this session
$env:JAVA_HOME = "C:\jdk21\jdk-21.0.8"
$jdk21Bin = "C:\jdk21\jdk-21.0.8\bin"
$pgBin    = "C:\Program Files\PostgreSQL\17\bin"
$env:PATH = "$jdk21Bin;$pgBin;$env:PATH"

Write-Host ""
Write-Host "=== MicroQuest Launcher ===" -ForegroundColor Cyan
Write-Host ""

# ------------------------------------------------------------------
# Step 1: PostgreSQL
# ------------------------------------------------------------------
Write-Host "[1/3] Checking PostgreSQL service..." -ForegroundColor Yellow
$pgService = "postgresql-x64-17"
$svc = Get-Service -Name $pgService -ErrorAction SilentlyContinue
if ($null -eq $svc) {
    Write-Host "ERROR: PostgreSQL service '$pgService' not found." -ForegroundColor Red
    Write-Host "       Download from https://www.postgresql.org/download/windows/" -ForegroundColor Red
    exit 1
}
if ($svc.Status -ne "Running") {
    Write-Host "      Starting PostgreSQL service (may need elevation)..." -ForegroundColor Yellow
    try {
        Start-Service $pgService -ErrorAction Stop
        Start-Sleep 3
    } catch {
        Write-Host "ERROR: Could not start PostgreSQL. Try running as Administrator." -ForegroundColor Red
        exit 1
    }
}
$env:PGPASSWORD = "microquest"
$pgReady = & "$pgBin\pg_isready.exe" -h 127.0.0.1 -p 5432 -U microquest -d microquest 2>&1
Write-Host "[1/3] PostgreSQL is ready." -ForegroundColor Green

# ------------------------------------------------------------------
# Step 2: Background job - wait for app then open Chrome
# ------------------------------------------------------------------
Write-Host "[2/3] Starting Spring Boot (logs below). Chrome opens when ready." -ForegroundColor Yellow
Write-Host "      Press Ctrl+C to stop." -ForegroundColor DarkGray
Write-Host ""

Set-Location $ProjectRoot

$chromeJob = Start-Job -ScriptBlock {
    for ($i = 0; $i -lt 80; $i++) {
        Start-Sleep 3
        try {
            $r = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
            if ($r.StatusCode -lt 500) {
                Start-Process "chrome.exe" "http://localhost:8080"
                return
            }
        } catch { }
    }
    Start-Process "chrome.exe" "http://localhost:8080"
}

# ------------------------------------------------------------------
# Step 3: Maven in foreground (logs stream here)
# ------------------------------------------------------------------
try {
    & mvn spring-boot:run
} finally {
    Stop-Job   $chromeJob -ErrorAction SilentlyContinue
    Remove-Job $chromeJob -ErrorAction SilentlyContinue
    Write-Host ""
    Write-Host "[3/3] MicroQuest stopped." -ForegroundColor Cyan
}
