# DemoBlaze Test Runner
Write-Host "DemoBlaze Test Automation" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
Write-Host ""

# Fix issues first
Write-Host "1. Fixing known issues..." -ForegroundColor Yellow
.\FixProductDetailPage.ps1

# Rebuild project
Write-Host "`n2. Rebuilding project..." -ForegroundColor Yellow
& ./gradlew clean build -x test

# Run tests and generate report
Write-Host "`n3. Running tests..." -ForegroundColor Yellow
.\TestReport.ps1
