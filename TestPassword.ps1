$apiUrl = "https://api.demoblaze.com"
$credentials = @{
    username = "testuser2025"
    password = "Test123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $credentials -ContentType "application/json"
    Write-Host "  API Response: $response" -ForegroundColor Green
    
    if ($response -match "Wrong password") {
        Write-Host "  ❌ Test123 is not the correct password" -ForegroundColor Red
        Write-Host "`nLet's find the actual password..." -ForegroundColor Yellow
        
        # Run the deep investigation
        if (Test-Path "./DeepInvestigation.ps1") {
            & ./DeepInvestigation.ps1
        }
    } else {
        Write-Host "  ✅ Test123 appears to work!" -ForegroundColor Green
    }
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
}
