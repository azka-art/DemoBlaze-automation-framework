# Create Brand New Test User
Write-Host "Creating a brand new test user..." -ForegroundColor Cyan

$apiUrl = "https://api.demoblaze.com"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$newUsername = "autotest_$timestamp"
$newPassword = "Test123!"

Write-Host "Creating user: $newUsername / $newPassword" -ForegroundColor Yellow

# Create the user
$body = @{
    username = $newUsername
    password = $newPassword
} | ConvertTo-Json

try {
    # Signup
    $signupResponse = Invoke-RestMethod -Uri "$apiUrl/signup" -Method Post -Body $body -ContentType "application/json"
    Write-Host "  Signup response: $signupResponse" -ForegroundColor Green
    
    # Wait a bit
    Start-Sleep -Seconds 2
    
    # Verify login
    $loginResponse = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $body -ContentType "application/json"
    Write-Host "  Login response: $loginResponse" -ForegroundColor Green
    
    if ($loginResponse -and $loginResponse -notmatch "Wrong password") {
        Write-Host "`n✅ New user created successfully!" -ForegroundColor Green
        
        # Update all files with new credentials
        Write-Host "`nUpdating all files with new credentials..." -ForegroundColor Yellow
        
        $files = @{
            "src/main/resources/config.properties" = @{
                "test.username=.*" = "test.username=$newUsername"
                "test.password=.*" = "test.password=$newPassword"
            }
            "src/test/java/com/demoblaze/stepdefinitions/web/LoginWebSteps.java" = @{
                '"testuser2025"' = "`"$newUsername`""
                '"Test123"' = "`"$newPassword`""
            }
            "src/test/java/com/demoblaze/stepdefinitions/web/CheckoutSteps.java" = @{
                '"testuser2025"' = "`"$newUsername`""
                '"Test123"' = "`"$newPassword`""
            }
            "src/test/resources/features/web/login_web.feature" = @{
                '"testuser2025"' = "`"$newUsername`""
                '"testpassword2025"' = "`"$newPassword`""
            }
        }
        
        foreach ($file in $files.Keys) {
            if (Test-Path $file) {
                $content = Get-Content $file -Raw
                foreach ($pattern in $files[$file].Keys) {
                    $replacement = $files[$file][$pattern]
                    $content = $content -replace $pattern, $replacement
                }
                [System.IO.File]::WriteAllText($file, $content, [System.Text.UTF8Encoding]::new($false))
                Write-Host "  Updated: $file" -ForegroundColor Green
            }
        }
        
        Write-Host "`nRebuilding project..." -ForegroundColor Yellow
        & cmd /c "gradlew.bat clean build -x test"
        
        Write-Host "`n✅ Everything updated with new user!" -ForegroundColor Green
        Write-Host "Username: $newUsername" -ForegroundColor White
        Write-Host "Password: $newPassword" -ForegroundColor White
        
        Write-Host "`nRunning tests again..." -ForegroundColor Yellow
        & cmd /c "gradlew.bat webTests"
    }
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
}
