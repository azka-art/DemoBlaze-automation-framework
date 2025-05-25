# Create New Test User
Write-Host "Creating new test user with known credentials..." -ForegroundColor Cyan

$apiUrl = "https://api.demoblaze.com"
$newUsername = "testuser_" + (Get-Date -Format "yyyyMMddHHmmss")
$newPassword = "Test123"

# Create new user
$body = @{
    username = $newUsername
    password = $newPassword
} | ConvertTo-Json

try {
    Write-Host "`nCreating user: $newUsername / $newPassword" -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "$apiUrl/signup" -Method Post -Body $body -ContentType "application/json"
    Write-Host "  Signup response: $response" -ForegroundColor Green
    
    # Verify login
    Start-Sleep -Seconds 2
    $loginResponse = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $body -ContentType "application/json"
    Write-Host "  Login response: $loginResponse" -ForegroundColor Green
    
    Write-Host "`n✅ New user created successfully!" -ForegroundColor Green
    Write-Host "  Username: $newUsername" -ForegroundColor White
    Write-Host "  Password: $newPassword" -ForegroundColor White
    
    # Update all test files to use new credentials
    Write-Host "`nUpdating test files with new credentials..." -ForegroundColor Yellow
    & ./UpdatePassword.ps1 -NewPassword $newPassword
    
    # Also update username references
    $filesToUpdate = @(
        "src/main/resources/config.properties",
        "src/test/java/com/demoblaze/stepdefinitions/web/LoginWebSteps.java",
        "src/test/java/com/demoblaze/stepdefinitions/web/CheckoutSteps.java"
    )
    
    foreach ($file in $filesToUpdate) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            $content = $content -replace 'testuser2025', $newUsername
            [System.IO.File]::WriteAllText($file, $content, [System.Text.UTF8Encoding]::new($false))
            Write-Host "  Updated $file with new username" -ForegroundColor Green
        }
    }
    
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
}
