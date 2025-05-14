# DemoBlaze Comprehensive Test Report Generator
# This script runs tests and generates detailed reports

param(
    [string]$TestType = "all",  # all, api, web
    [string]$ReportName = "TestReport",
    [bool]$GenerateHTML = $true,
    [bool]$OpenReport = $true
)

# Create report directories
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportDir = "test-reports\$timestamp"
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null

# Define report files
$summaryFile = "$reportDir\TestSummary_$timestamp.txt"
$detailedFile = "$reportDir\DetailedReport_$timestamp.txt"
$htmlFile = "$reportDir\TestReport_$timestamp.html"

# Start logging
Start-Transcript -Path $detailedFile

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DEMOBLAZE TEST AUTOMATION REPORT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
Write-Host "Test Type: $TestType" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Initialize summary content
$summary = @()
$summary += "DEMOBLAZE TEST AUTOMATION SUMMARY"
$summary += "================================="
$summary += "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$summary += "Test Type: $TestType"
$summary += ""

# Function to run tests
function Run-Tests {
    param([string]$Type)
    
    $results = @{
        StartTime = Get-Date
        EndTime = $null
        Duration = $null
        Passed = 0
        Failed = 0
        Skipped = 0
        Total = 0
        Output = ""
        Scenarios = @()
    }
    
    Write-Host "Starting $Type tests..." -ForegroundColor Yellow
    
    switch($Type) {
        "api" {
            $results.Output = ./gradlew apiTests --console=plain 2>&1 | Out-String
        }
        "web" {
            $results.Output = ./gradlew webTests --console=plain 2>&1 | Out-String
        }
        "all" {
            Write-Host "Running API tests..." -ForegroundColor Green
            $apiOutput = ./gradlew apiTests --console=plain 2>&1 | Out-String
            Write-Host "Running Web tests..." -ForegroundColor Green
            $webOutput = ./gradlew webTests --console=plain 2>&1 | Out-String
            $results.Output = $apiOutput + "`n`n" + $webOutput
        }
    }
    
    $results.EndTime = Get-Date
    $results.Duration = $results.EndTime - $results.StartTime
    
    # Parse results
    if ($results.Output -match "(\d+) Scenarios? \(([^)]+)\)") {
        $results.Total = [int]$Matches[1]
        $details = $Matches[2]
        
        if ($details -match "(\d+) passed") { $results.Passed = [int]$Matches[1] }
        if ($details -match "(\d+) failed") { $results.Failed = [int]$Matches[1] }
        if ($details -match "(\d+) skipped") { $results.Skipped = [int]$Matches[1] }
    }
    
    # Extract failed scenarios
    $failedScenarios = $results.Output | Select-String -Pattern "Failed scenarios:" -Context 0,20
    if ($failedScenarios) {
        $results.FailedScenarios = $failedScenarios.Context.PostContext
    }
    
    return $results
}

# Run the tests
$testResults = Run-Tests -Type $TestType

# Generate summary
$summary += "TEST EXECUTION SUMMARY"
$summary += "======================"
$summary += "Start Time: $($testResults.StartTime.ToString('yyyy-MM-dd HH:mm:ss'))"
$summary += "End Time: $($testResults.EndTime.ToString('yyyy-MM-dd HH:mm:ss'))"
$summary += "Duration: $([math]::Round($testResults.Duration.TotalSeconds, 2)) seconds"
$summary += ""
$summary += "RESULTS"
$summary += "======="
$summary += "Total Scenarios: $($testResults.Total)"
$summary += "Passed: $($testResults.Passed)"
$summary += "Failed: $($testResults.Failed)"
$summary += "Skipped: $($testResults.Skipped)"
$summary += "Pass Rate: $(if ($testResults.Total -gt 0) { [math]::Round(($testResults.Passed / $testResults.Total) * 100, 2) } else { 0 })%"
$summary += ""

# Add failure details if any
if ($testResults.Failed -gt 0) {
    $summary += "FAILED SCENARIOS"
    $summary += "================"
    if ($testResults.FailedScenarios) {
        $summary += $testResults.FailedScenarios
    }
    $summary += ""
}

# Check for report files
$summary += "GENERATED REPORTS"
$summary += "================="
$reportFiles = @(
    "build\reports\cucumber\api\index.html",
    "build\reports\cucumber\web\index.html",
    "build\reports\cucumber\api\cucumber.json",
    "build\reports\cucumber\web\cucumber.json"
)

foreach ($file in $reportFiles) {
    if (Test-Path $file) {
        $summary += "✓ $file"
        # Copy reports to our report directory
        Copy-Item $file -Destination $reportDir -Force
    } else {
        $summary += "✗ $file (not found)"
    }
}

# Save summary
$summary | Out-File -FilePath $summaryFile

# Generate HTML report if requested
if ($GenerateHTML) {
    $html = @"
<!DOCTYPE html>
<html>
<head>
    <title>DemoBlaze Test Report - $timestamp</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h1 { color: #333; text-align: center; }
        .summary { display: flex; justify-content: space-around; margin: 20px 0; }
        .summary-item { text-align: center; padding: 20px; }
        .passed { color: #28a745; }
        .failed { color: #dc3545; }
        .skipped { color: #ffc107; }
        .total { color: #007bff; }
        .metric { font-size: 36px; font-weight: bold; }
        .label { font-size: 14px; color: #666; }
        .details { margin-top: 30px; }
        .failed-list { background-color: #f8d7da; padding: 15px; border-radius: 5px; margin-top: 20px; }
        .timestamp { text-align: center; color: #666; font-size: 14px; }
        .duration { text-align: center; color: #666; font-size: 16px; margin: 10px 0; }
        .progress-bar { width: 100%; height: 30px; background-color: #e0e0e0; border-radius: 5px; overflow: hidden; margin: 20px 0; }
        .progress-passed { background-color: #28a745; height: 100%; float: left; }
        .progress-failed { background-color: #dc3545; height: 100%; float: left; }
        .progress-skipped { background-color: #ffc107; height: 100%; float: left; }
    </style>
</head>
<body>
    <div class="container">
        <h1>DemoBlaze Test Automation Report</h1>
        <p class="timestamp">Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
        <p class="duration">Duration: $([math]::Round($testResults.Duration.TotalSeconds, 2)) seconds</p>
        
        <div class="progress-bar">
            <div class="progress-passed" style="width: $(if ($testResults.Total -gt 0) { ($testResults.Passed / $testResults.Total) * 100 } else { 0 })%"></div>
            <div class="progress-failed" style="width: $(if ($testResults.Total -gt 0) { ($testResults.Failed / $testResults.Total) * 100 } else { 0 })%"></div>
            <div class="progress-skipped" style="width: $(if ($testResults.Total -gt 0) { ($testResults.Skipped / $testResults.Total) * 100 } else { 0 })%"></div>
        </div>
        
        <div class="summary">
            <div class="summary-item total">
                <div class="metric">$($testResults.Total)</div>
                <div class="label">Total Scenarios</div>
            </div>
            <div class="summary-item passed">
                <div class="metric">$($testResults.Passed)</div>
                <div class="label">Passed</div>
            </div>
            <div class="summary-item failed">
                <div class="metric">$($testResults.Failed)</div>
                <div class="label">Failed</div>
            </div>
            <div class="summary-item skipped">
                <div class="metric">$($testResults.Skipped)</div>
                <div class="label">Skipped</div>
            </div>
        </div>
        
        <div class="details">
            <h2>Pass Rate: $(if ($testResults.Total -gt 0) { [math]::Round(($testResults.Passed / $testResults.Total) * 100, 2) } else { 0 })%</h2>
            
            $(if ($testResults.Failed -gt 0) {
                '<div class="failed-list">
                    <h3>Failed Scenarios</h3>
                    <pre>' + ($testResults.FailedScenarios -join "`n") + '</pre>
                </div>'
            })
        </div>
    </div>
</body>
</html>
"@
    
    $html | Out-File -FilePath $htmlFile -Encoding UTF8
}

# Stop transcript
Stop-Transcript

# Display summary in console
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST EXECUTION COMPLETED" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total: $($testResults.Total) | Passed: $($testResults.Passed) | Failed: $($testResults.Failed) | Skipped: $($testResults.Skipped)" -ForegroundColor White
Write-Host "Pass Rate: $(if ($testResults.Total -gt 0) { [math]::Round(($testResults.Passed / $testResults.Total) * 100, 2) } else { 0 })%" -ForegroundColor $(if ($testResults.Failed -eq 0) { "Green" } else { "Yellow" })
Write-Host ""
Write-Host "Reports generated in: $reportDir" -ForegroundColor Yellow
Write-Host "Summary: $summaryFile" -ForegroundColor White
Write-Host "Detailed: $detailedFile" -ForegroundColor White
Write-Host "HTML: $htmlFile" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

# Open HTML report if requested
if ($OpenReport -and $GenerateHTML -and (Test-Path $htmlFile)) {
    Start-Process $htmlFile
}

# Return results object for further processing
return $testResults
