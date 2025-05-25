# Password Diagnosis
$apiUrl = "https://api.demoblaze.com"

# Test different password combinations
$testCombos = @(
    @{ username = "testuser2025"; password = "testpassword2025" },
    @{ username = "testuser2025"; password = "Test@2025" },
    @{ username = "testuser2025"; password = "password" },
    @{ username = "testuser2025"; password = "Test123" }
)

foreach ($combo in $testCombos) {
    Write-Host "`nTesting: $($combo.username) / $($combo.password)" -ForegroundColor Gray
    
    try {
        $body = $combo | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        Write-Host "  SUCCESS: $response" -ForegroundColor Green
        break
    } catch {
        Write-Host "  FAILED: $_" -ForegroundColor Red
    }
}

Write-Host "`nSuggestion: Update the test to use the correct password" -ForegroundColor Yellow
