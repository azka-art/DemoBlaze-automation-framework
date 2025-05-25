# Fix Password Issue in Tests
Write-Host "Fixing password in test files..." -ForegroundColor Yellow

# Update LoginWebSteps to use different password
$loginWebStepsPath = "src/test/java/com/demoblaze/stepdefinitions/web/LoginWebSteps.java"
if (Test-Path $loginWebStepsPath) {
    $content = Get-Content $loginWebStepsPath -Raw
    
    # Try updating the password constant
    $content = $content -replace 'performLogin\("testuser2025", "testpassword2025"', 'performLogin("testuser2025", "Test@2025"'
    
    [System.IO.File]::WriteAllText($loginWebStepsPath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  Updated LoginWebSteps" -ForegroundColor Green
}

# Update config.properties
$configPath = "src/main/resources/config.properties"
if (Test-Path $configPath) {
    $content = Get-Content $configPath -Raw
    $content = $content -replace 'test.password=testpassword2025', 'test.password=Test@2025'
    
    [System.IO.File]::WriteAllText($configPath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  Updated config.properties" -ForegroundColor Green
}

Write-Host "`nRebuilding..." -ForegroundColor Yellow
& cmd /c "gradlew.bat clean build -x test"

Write-Host "`nRe-running web tests..." -ForegroundColor Yellow
& cmd /c "gradlew.bat webTests -Dheadless=true"
