# Pre-test verification script
Write-Host "Running pre-test user verification..." -ForegroundColor Yellow

# Compile and run the diagnostic
Write-Host "`nCompiling project..." -ForegroundColor Cyan
./gradlew clean build -x test

Write-Host "`nRunning user diagnostic..." -ForegroundColor Cyan
./gradlew -q --console=plain -PmainClass=com.demoblaze.utils.UserDiagnostic test --tests UserDiagnostic

Write-Host "`nDiagnostic complete!" -ForegroundColor Green
Write-Host "Check the output above to see if testuser2025 can be created/verified" -ForegroundColor Yellow