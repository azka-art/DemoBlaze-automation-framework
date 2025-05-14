# DemoBlaze Test Report Generator
Write-Host "DemoBlaze Test Automation Report" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Create report directory
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportDir = "test-reports\$timestamp"
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null

# Initialize results
$results = @{
    API = @{Total = 0; Passed = 0; Failed = 0}
    Web = @{Total = 0; Passed = 0; Failed = 0}
    Summary = @{StartTime = Get-Date}
}

# Run API Tests
Write-Host "Running API Tests..." -ForegroundColor Yellow
$apiOutput = & ./gradlew apiTests --console=plain 2>&1 | Out-String

# Parse API results
if ($apiOutput -match "(\d+) Scenarios.*(\d+) passed.*(\d+) failed") {
    $results.API.Total = [int]$Matches[1]
    $results.API.Passed = [int]$Matches[2]
    $results.API.Failed = [int]$Matches[3]
} elseif ($apiOutput -match "(\d+) Scenarios.*(\d+) passed") {
    $results.API.Total = [int]$Matches[1]
    $results.API.Passed = [int]$Matches[2]
    $results.API.Failed = 0
}

Write-Host "API Tests: $($results.API.Passed)/$($results.API.Total) passed" -ForegroundColor Green

# Run Web Tests
Write-Host "`nRunning Web Tests..." -ForegroundColor Yellow
$webOutput = & ./gradlew webTests --console=plain 2>&1 | Out-String

# Parse Web results
if ($webOutput -match "(\d+) Scenarios.*(\d+) passed.*(\d+) failed") {
    $results.Web.Total = [int]$Matches[1]
    $results.Web.Passed = [int]$Matches[2]
    $results.Web.Failed = [int]$Matches[3]
} elseif ($webOutput -match "(\d+) Scenarios.*(\d+) passed") {
    $results.Web.Total = [int]$Matches[1]
    $results.Web.Passed = [int]$Matches[2]
    $results.Web.Failed = 0
}

Write-Host "Web Tests: $($results.Web.Passed)/$($results.Web.Total) passed" -ForegroundColor Green

# Calculate summary
$results.Summary.EndTime = Get-Date
$results.Summary.Duration = $results.Summary.EndTime - $results.Summary.StartTime
$results.Summary.TotalTests = $results.API.Total + $results.Web.Total
$results.Summary.TotalPassed = $results.API.Passed + $results.Web.Passed
$results.Summary.TotalFailed = $results.API.Failed + $results.Web.Failed
$results.Summary.PassRate = if ($results.Summary.TotalTests -gt 0) { 
    [math]::Round(($results.Summary.TotalPassed / $results.Summary.TotalTests) * 100, 2) 
} else { 0 }

# Generate HTML Report
$htmlReport = @"
<!DOCTYPE html>
<html>
<head>
    <title>DemoBlaze Test Report - $timestamp</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #333; text-align: center; }
        .summary { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
        .metric { display: inline-block; margin: 10px 20px; }
        .metric-value { font-size: 36px; font-weight: bold; }
        .metric-label { color: #666; font-size: 14px; }
        .passed { color: #28a745; }
        .failed { color: #dc3545; }
        .test-section { margin: 30px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #007bff; color: white; }
        .timestamp { text-align: center; color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <h1>DemoBlaze Test Automation Report</h1>
        <p class="timestamp">Generated on $(Get-Date -Format 'dddd, MMMM dd, yyyy HH:mm:ss')</p>
        
        <div class="summary">
            <div class="metric">
                <div class="metric-value">$($results.Summary.TotalTests)</div>
                <div class="metric-label">Total Tests</div>
            </div>
            <div class="metric">
                <div class="metric-value passed">$($results.Summary.TotalPassed)</div>
                <div class="metric-label">Passed</div>
            </div>
            <div class="metric">
                <div class="metric-value failed">$($results.Summary.TotalFailed)</div>
                <div class="metric-label">Failed</div>
            </div>
            <div class="metric">
                <div class="metric-value">$($results.Summary.PassRate)%</div>
                <div class="metric-label">Pass Rate</div>
            </div>
        </div>
        
        <div class="test-section">
            <h2>Test Results by Type</h2>
            <table>
                <tr>
                    <th>Test Type</th>
                    <th>Total</th>
                    <th>Passed</th>
                    <th>Failed</th>
                    <th>Pass Rate</th>
                </tr>
                <tr>
                    <td>API Tests</td>
                    <td>$($results.API.Total)</td>
                    <td class="passed">$($results.API.Passed)</td>
                    <td class="failed">$($results.API.Failed)</td>
                    <td>$(if ($results.API.Total -gt 0) { [math]::Round(($results.API.Passed / $results.API.Total) * 100, 2) } else { 0 })%</td>
                </tr>
                <tr>
                    <td>Web Tests</td>
                    <td>$($results.Web.Total)</td>
                    <td class="passed">$($results.Web.Passed)</td>
                    <td class="failed">$($results.Web.Failed)</td>
                    <td>$(if ($results.Web.Total -gt 0) { [math]::Round(($results.Web.Passed / $results.Web.Total) * 100, 2) } else { 0 })%</td>
                </tr>
            </table>
        </div>
        
        <div class="test-section">
            <h3>Execution Details</h3>
            <p>Duration: $([math]::Round($results.Summary.Duration.TotalMinutes, 2)) minutes</p>
            <p>Start Time: $($results.Summary.StartTime.ToString('yyyy-MM-dd HH:mm:ss'))</p>
            <p>End Time: $($results.Summary.EndTime.ToString('yyyy-MM-dd HH:mm:ss'))</p>
        </div>
    </div>
</body>
</html>
"@

# Save reports
$htmlReport | Out-File -FilePath "$reportDir\TestReport_$timestamp.html" -Encoding UTF8
$results | ConvertTo-Json -Depth 10 | Out-File -FilePath "$reportDir\TestResults_$timestamp.json"

# Save logs
$apiOutput | Out-File -FilePath "$reportDir\APITestLog_$timestamp.txt"
$webOutput | Out-File -FilePath "$reportDir\WebTestLog_$timestamp.txt"

# Summary output
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST EXECUTION SUMMARY" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests: $($results.Summary.TotalTests)" -ForegroundColor White
Write-Host "Passed: $($results.Summary.TotalPassed)" -ForegroundColor Green
Write-Host "Failed: $($results.Summary.TotalFailed)" -ForegroundColor Red
Write-Host "Pass Rate: $($results.Summary.PassRate)%" -ForegroundColor Yellow
Write-Host "Duration: $([math]::Round($results.Summary.Duration.TotalMinutes, 2)) minutes" -ForegroundColor White
Write-Host ""
Write-Host "Reports saved to: $reportDir" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Open HTML report
Start-Process "$reportDir\TestReport_$timestamp.html"
