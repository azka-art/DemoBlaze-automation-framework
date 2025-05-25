param(
    [string]$NewPassword = "Test123"  # Default to a common test password
)

Write-Host "Updating password to: $NewPassword" -ForegroundColor Cyan

# Update config.properties
$configPath = "src/main/resources/config.properties"
if (Test-Path $configPath) {
    $content = Get-Content $configPath -Raw
    $content = $content -replace 'test\.password=.*', "test.password=$NewPassword"
    [System.IO.File]::WriteAllText($configPath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  Updated config.properties" -ForegroundColor Green
}

# Update LoginWebSteps
$loginWebStepsPath = "src/test/java/com/demoblaze/stepdefinitions/web/LoginWebSteps.java"
if (Test-Path $loginWebStepsPath) {
    $content = Get-Content $loginWebStepsPath -Raw
    $content = $content -replace 'performLogin\("testuser2025", "[^"]*"', "performLogin(`"testuser2025`", `"$NewPassword`""
    [System.IO.File]::WriteAllText($loginWebStepsPath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  Updated LoginWebSteps.java" -ForegroundColor Green
}

# Update CheckoutSteps
$checkoutStepsPath = "src/test/java/com/demoblaze/stepdefinitions/web/CheckoutSteps.java"
if (Test-Path $checkoutStepsPath) {
    $content = Get-Content $checkoutStepsPath -Raw
    $content = $content -replace 'private static final String CHECKOUT_PASSWORD = "[^"]*"', "private static final String CHECKOUT_PASSWORD = `"$NewPassword`""
    [System.IO.File]::WriteAllText($checkoutStepsPath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  Updated CheckoutSteps.java" -ForegroundColor Green
}

# Update GlobalHooks if needed
$globalHooksPath = "src/test/java/com/demoblaze/hooks/GlobalHooks.java"
if (Test-Path $globalHooksPath) {
    $content = Get-Content $globalHooksPath -Raw
    $content = $content -replace 'setupTestUser\("testuser2025", "[^"]*"\)', "setupTestUser(`"testuser2025`", `"$NewPassword`")"
    [System.IO.File]::WriteAllText($globalHooksPath, $content, [System.Text.UTF8Encoding]::new($false))
    Write-Host "  Updated GlobalHooks.java" -ForegroundColor Green
}

Write-Host "`nRebuilding project..." -ForegroundColor Yellow
& cmd /c "gradlew.bat clean build -x test"

Write-Host "`n✅ Password updated to: $NewPassword" -ForegroundColor Green
