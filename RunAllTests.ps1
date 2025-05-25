# DemoBlaze Test Runner
Write-Host "DemoBlaze Test Automation" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan

# Clean and build
Write-Host "`nBuilding project..." -ForegroundColor Yellow
./gradlew clean build -x test

# Run tests
Write-Host "`nRunning API tests..." -ForegroundColor Yellow
./gradlew apiTests

Write-Host "`nRunning Web tests..." -ForegroundColor Yellow
./gradlew webTests -Dheadless=true

Write-Host "`nTest execution complete!" -ForegroundColor Green
Write-Host "Reports available at:" -ForegroundColor Yellow
Write-Host "  API: build/reports/cucumber/api/index.html" -ForegroundColor White
Write-Host "  Web: build/reports/cucumber/web/index.html" -ForegroundColor White
