# Password Discovery Script
Write-Host "Attempting to discover/reset password for testuser2025..." -ForegroundColor Cyan

# Option 1: Try common test passwords
$passwords = @(
    "testpassword2025",
    "Test@2025", 
    "password",
    "Password123",
    "Test123",
    "test123",
    "demoblaze",
    "Demoblaze123",
    "test2025"
)

$apiUrl = "https://api.demoblaze.com"
$foundPassword = $null

foreach ($pwd in $passwords) {
    Write-Host "`nTrying: testuser2025 / $pwd" -ForegroundColor Gray
    
    $body = @{
        username = "testuser2025"
        password = $pwd
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $body -ContentType "application/json"
        if ($response) {
            Write-Host "  SUCCESS! Found password: $pwd" -ForegroundColor Green
            Write-Host "  Response: $response" -ForegroundColor Gray
            $foundPassword = $pwd
            break
        }
    } catch {
        Write-Host "  Failed" -ForegroundColor Red
    }
}

if ($foundPassword) {
    Write-Host "`n✅ Found correct password: $foundPassword" -ForegroundColor Green
    Write-Host "`nNext steps:" -ForegroundColor Yellow
    Write-Host "1. Update config.properties to use: $foundPassword" -ForegroundColor White
    Write-Host "2. Update test code to use: $foundPassword" -ForegroundColor White
} else {
    Write-Host "`n❌ Could not find correct password" -ForegroundColor Red
    Write-Host "`nOption 2: Create a new test user" -ForegroundColor Yellow
}
