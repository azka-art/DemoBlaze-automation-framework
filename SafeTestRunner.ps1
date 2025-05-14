# Safe Test Runner
Write-Host "DemoBlaze Safe Test Runner" -ForegroundColor Cyan
Write-Host "==========================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Test gradle
Write-Host "1. Testing Gradle installation..." -ForegroundColor Yellow
$gradleTest = & ./gradlew --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Gradle error:" -ForegroundColor Red
    Write-Host $gradleTest
    exit 1
}
Write-Host "✓ Gradle is working" -ForegroundColor Green

# Step 2: Clean and compile
Write-Host "`n2. Cleaning and compiling..." -ForegroundColor Yellow
$compileResult = & ./gradlew clean compileJava compileTestJava 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation error:" -ForegroundColor Red
    Write-Host $compileResult
    Write-Host "`nTrying to fix common issues..." -ForegroundColor Yellow
    
    # Try to fix encoding issues
    Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object {
        $content = Get-Content $_.FullName -Raw
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($_.FullName, $content, $utf8NoBom)
    }
    
    # Try again
    $compileResult2 = & ./gradlew compileJava compileTestJava 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Still failing. Please check the errors above." -ForegroundColor Red
        exit 1
    }
}
Write-Host "✓ Compilation successful" -ForegroundColor Green

# Step 3: Run tests
Write-Host "`n3. Running tests..." -ForegroundColor Yellow

# Create report directory
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportDir = "test-reports\$timestamp"
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null

# Run API tests
Write-Host "`nRunning API tests..." -ForegroundColor Cyan
$apiStart = Get-Date
$apiOutput = & ./gradlew apiTests --console=plain 2>&1 | Out-String
$apiEnd = Get-Date
$apiDuration = $apiEnd - $apiStart

# Parse API results
$apiPassed = 0
$apiTotal = 0
if ($apiOutput -match "(\d+) Scenarios.*(\d+) passed") {
    $apiTotal = [int]$Matches[1]
    $apiPassed = [int]$Matches[2]
}

Write-Host "API Tests: $apiPassed/$apiTotal passed (Duration: $([math]::Round($apiDuration.TotalSeconds, 1))s)" -ForegroundColor Green

# Run Web tests
Write-Host "`nRunning Web tests..." -ForegroundColor Cyan
$webStart = Get-Date
$webOutput = & ./gradlew webTests --console=plain 2>&1 | Out-String
$webEnd = Get-Date
$webDuration = $webEnd - $webStart

# Parse Web results
$webPassed = 0
$webTotal = 0
if ($webOutput -match "(\d+) Scenarios.*(\d+) passed") {
    $webTotal = [int]$Matches[1]
    $webPassed = [int]$Matches[2]
}

Write-Host "Web Tests: $webPassed/$webTotal passed (Duration: $([math]::Round($webDuration.TotalSeconds, 1))s)" -ForegroundColor Green

# Save logs
$apiOutput | Out-File -FilePath "$reportDir\api-test.log"
$webOutput | Out-File -FilePath "$reportDir\web-test.log"

# Generate simple summary
$summary = @"
DemoBlaze Test Summary
=====================
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

API Tests: $apiPassed/$apiTotal passed
Web Tests: $webPassed/$webTotal passed
Total: $($apiPassed + $webPassed)/$($apiTotal + $webTotal) passed

API Duration: $([math]::Round($apiDuration.TotalSeconds, 1))s
Web Duration: $([math]::Round($webDuration.TotalSeconds, 1))s
Total Duration: $([math]::Round(($apiDuration + $webDuration).TotalSeconds, 1))s

Logs saved to: $reportDir
"@

$summary | Out-File -FilePath "$reportDir\summary.txt"
Write-Host $summary -ForegroundColor Cyan

Write-Host "`nTest execution complete!" -ForegroundColor Green
Write-Host "Reports saved to: $reportDir" -ForegroundColor Yellow
