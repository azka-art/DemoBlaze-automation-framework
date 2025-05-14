# DemoBlaze Enhanced Test Report Generator
# This script runs tests separately and generates comprehensive reports

param(
    [string]$TestType = "all",  # all, api, web
    [string]$ReportName = "TestReport",
    [bool]$GenerateHTML = $true,
    [bool]$OpenReport = $true,
    [bool]$Verbose = $true
)

# Create report directories
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportDir = "test-reports\$timestamp"
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null

# Define report files
$summaryFile = "$reportDir\TestSummary_$timestamp.txt"
$detailedFile = "$reportDir\DetailedReport_$timestamp.txt"
$htmlFile = "$reportDir\TestReport_$timestamp.html"
$jsonFile = "$reportDir\TestResults_$timestamp.json"

# Start logging
Start-Transcript -Path $detailedFile

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DEMOBLAZE TEST AUTOMATION REPORT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
Write-Host "Test Type: $TestType" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Initialize results structure
$allResults = @{
    Summary = @{
        TotalScenarios = 0
        PassedScenarios = 0
        FailedScenarios = 0
        SkippedScenarios = 0
        PassRate = 0
        StartTime = Get-Date
        EndTime = $null
        Duration = $null
    }
    API = @{
        Scenarios = 0
        Passed = 0
        Failed = 0
        Skipped = 0
        FailedList = @()
        Output = ""
    }
    Web = @{
        Scenarios = 0
        Passed = 0
        Failed = 0
        Skipped = 0
        FailedList = @()
        Output = ""
    }
}

# Function to parse test output
function Parse-TestOutput {
    param(
        [string]$Output,
        [string]$Type
    )
    
    $result = @{
        Scenarios = 0
        Passed = 0
        Failed = 0
        Skipped = 0
        FailedList = @()
    }
    
    # Parse scenario counts
    if ($Output -match "(\d+) Scenarios? \(([^)]+)\)") {
        $result.Scenarios = [int]$Matches[1]
        $details = $Matches[2]
        
        if ($details -match "(\d+) passed") { $result.Passed = [int]$Matches[1] }
        if ($details -match "(\d+) failed") { $result.Failed = [int]$Matches[1] }
        if ($details -match "(\d+) skipped") { $result.Skipped = [int]$Matches[1] }
    }
    
    # Extract failed scenarios
    $failedPattern = "Failed scenarios:|FAILED"
    $lines = $Output -split "`n"
    $captureFailures = $false
    
    foreach ($line in $lines) {
        if ($line -match $failedPattern) {
            $captureFailures = $true
            continue
        }
        
        if ($captureFailures -and $line -match "^\s*$") {
            $captureFailures = $false
        }
        
        if ($captureFailures -and $line -match "\.feature:\d+") {
            $result.FailedList += $line.Trim()
        }
    }
    
    return $result
}

# Function to run specific test type
function Run-TestType {
    param([string]$Type)
    
    Write-Host "`nRunning $Type tests..." -ForegroundColor Yellow
    
    $output = ""
    try {
        switch($Type) {
            "api" {
                $output = & ./gradlew apiTests --console=plain 2>&1 | Out-String
            }
            "web" {
                $output = & ./gradlew webTests --console=plain 2>&1 | Out-String
            }
        }
        
        if ($Verbose) {
            Write-Host $output
        }
        
        $parsed = Parse-TestOutput -Output $output -Type $Type
        
        # Update results
        $allResults.$Type.Scenarios = $parsed.Scenarios
        $allResults.$Type.Passed = $parsed.Passed
        $allResults.$Type.Failed = $parsed.Failed
        $allResults.$Type.Skipped = $parsed.Skipped
        $allResults.$Type.FailedList = $parsed.FailedList
        $allResults.$Type.Output = $output
        
        Write-Host "$Type Test Results: $($parsed.Scenarios) scenarios, $($parsed.Passed) passed, $($parsed.Failed) failed" -ForegroundColor Cyan
        
    } catch {
        Write-Host "Error running $Type tests: $_" -ForegroundColor Red
        $allResults.$Type.Output = "Error: $_"
    }
}

# Run tests based on type
switch($TestType) {
    "api" {
        Run-TestType -Type "api"
    }
    "web" {
        Run-TestType -Type "web"
    }
    "all" {
        Run-TestType -Type "api"
        Run-TestType -Type "web"
    }
}

# Calculate summary
$allResults.Summary.EndTime = Get-Date
$allResults.Summary.Duration = $allResults.Summary.EndTime - $allResults.Summary.StartTime

$allResults.Summary.TotalScenarios = $allResults.API.Scenarios + $allResults.Web.Scenarios
$allResults.Summary.PassedScenarios = $allResults.API.Passed + $allResults.Web.Passed
$allResults.Summary.FailedScenarios = $allResults.API.Failed + $allResults.Web.Failed
$allResults.Summary.SkippedScenarios = $allResults.API.Skipped + $allResults.Web.Skipped

if ($allResults.Summary.TotalScenarios -gt 0) {
    $allResults.Summary.PassRate = [math]::Round(($allResults.Summary.PassedScenarios / $allResults.Summary.TotalScenarios) * 100, 2)
}

# Generate text summary
$summary = @()
$summary += "DEMOBLAZE TEST AUTOMATION SUMMARY"
$summary += "================================="
$summary += "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$summary += "Test Type: $TestType"
$summary += "Duration: $([math]::Round($allResults.Summary.Duration.TotalSeconds, 2)) seconds"
$summary += ""
$summary += "OVERALL RESULTS"
$summary += "==============="
$summary += "Total Scenarios: $($allResults.Summary.TotalScenarios)"
$summary += "Passed: $($allResults.Summary.PassedScenarios)"
$summary += "Failed: $($allResults.Summary.FailedScenarios)"
$summary += "Skipped: $($allResults.Summary.SkippedScenarios)"
$summary += "Pass Rate: $($allResults.Summary.PassRate)%"
$summary += ""

if ($TestType -eq "all" -or $TestType -eq "api") {
    $summary += "API TEST RESULTS"
    $summary += "================"
    $summary += "Scenarios: $($allResults.API.Scenarios)"
    $summary += "Passed: $($allResults.API.Passed)"
    $summary += "Failed: $($allResults.API.Failed)"
    $summary += "Skipped: $($allResults.API.Skipped)"
    if ($allResults.API.FailedList.Count -gt 0) {
        $summary += "Failed Scenarios:"
        $allResults.API.FailedList | ForEach-Object { $summary += "  $_" }
    }
    $summary += ""
}

if ($TestType -eq "all" -or $TestType -eq "web") {
    $summary += "WEB TEST RESULTS"
    $summary += "================"
    $summary += "Scenarios: $($allResults.Web.Scenarios)"
    $summary += "Passed: $($allResults.Web.Passed)"
    $summary += "Failed: $($allResults.Web.Failed)"
    $summary += "Skipped: $($allResults.Web.Skipped)"
    if ($allResults.Web.FailedList.Count -gt 0) {
        $summary += "Failed Scenarios:"
        $allResults.Web.FailedList | ForEach-Object { $summary += "  $_" }
    }
    $summary += ""
}

# Save summary
$summary | Out-File -FilePath $summaryFile

# Save JSON results
$allResults | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonFile

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
        h2 { color: #555; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
        .summary { display: flex; justify-content: space-around; margin: 20px 0; }
        .summary-item { text-align: center; padding: 20px; flex: 1; }
        .passed { color: #28a745; }
        .failed { color: #dc3545; }
        .skipped { color: #ffc107; }
        .total { color: #007bff; }
        .metric { font-size: 36px; font-weight: bold; }
        .label { font-size: 14px; color: #666; }
        .section { margin: 30px 0; padding: 20px; background-color: #f8f9fa; border-radius: 5px; }
        .failed-list { background-color: #f8d7da; padding: 15px; border-radius: 5px; margin-top: 10px; }
        .timestamp { text-align: center; color: #666; font-size: 14px; }
        .duration { text-align: center; color: #666; font-size: 16px; margin: 10px 0; }
        .test-type { display: inline-block; padding: 5px 15px; background-color: #007bff; color: white; border-radius: 20px; margin: 10px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #007bff; color: white; }
        .chart-container { width: 300px; height: 300px; margin: 20px auto; }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <h1>DemoBlaze Test Automation Report</h1>
        <p class="timestamp">Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
        <p class="duration">Duration: $([math]::Round($allResults.Summary.Duration.TotalSeconds, 2)) seconds</p>
        
        <div style="text-align: center;">
            <span class="test-type">Test Type: $TestType</span>
        </div>
        
        <div class="summary">
            <div class="summary-item total">
                <div class="metric">$($allResults.Summary.TotalScenarios)</div>
                <div class="label">Total Scenarios</div>
            </div>
            <div class="summary-item passed">
                <div class="metric">$($allResults.Summary.PassedScenarios)</div>
                <div class="label">Passed</div>
            </div>
            <div class="summary-item failed">
                <div class="metric">$($allResults.Summary.FailedScenarios)</div>
                <div class="label">Failed</div>
            </div>
            <div class="summary-item skipped">
                <div class="metric">$($allResults.Summary.SkippedScenarios)</div>
                <div class="label">Skipped</div>
            </div>
        </div>
        
        <div style="text-align: center;">
            <h2>Pass Rate: $($allResults.Summary.PassRate)%</h2>
        </div>
        
        <div class="chart-container">
            <canvas id="resultsChart"></canvas>
        </div>
        
        $(if ($TestType -eq "all" -or $TestType -eq "api") {
            "<div class='section'>
                <h2>API Test Results</h2>
                <table>
                    <tr><th>Metric</th><th>Value</th></tr>
                    <tr><td>Total Scenarios</td><td>$($allResults.API.Scenarios)</td></tr>
                    <tr><td>Passed</td><td class='passed'>$($allResults.API.Passed)</td></tr>
                    <tr><td>Failed</td><td class='failed'>$($allResults.API.Failed)</td></tr>
                    <tr><td>Skipped</td><td class='skipped'>$($allResults.API.Skipped)</td></tr>
                </table>
                $(if ($allResults.API.FailedList.Count -gt 0) {
                    '<div class="failed-list">
                        <h3>Failed API Scenarios</h3>
                        <ul>' + ($allResults.API.FailedList | ForEach-Object { "<li>$_</li>" }) + '</ul>
                    </div>'
                })
            </div>"
        })
        
        $(if ($TestType -eq "all" -or $TestType -eq "web") {
            "<div class='section'>
                <h2>Web Test Results</h2>
                <table>
                    <tr><th>Metric</th><th>Value</th></tr>
                    <tr><td>Total Scenarios</td><td>$($allResults.Web.Scenarios)</td></tr>
                    <tr><td>Passed</td><td class='passed'>$($allResults.Web.Passed)</td></tr>
                    <tr><td>Failed</td><td class='failed'>$($allResults.Web.Failed)</td></tr>
                    <tr><td>Skipped</td><td class='skipped'>$($allResults.Web.Skipped)</td></tr>
                </table>
                $(if ($allResults.Web.FailedList.Count -gt 0) {
                    '<div class="failed-list">
                        <h3>Failed Web Scenarios</h3>
                        <ul>' + ($allResults.Web.FailedList | ForEach-Object { "<li>$_</li>" }) + '</ul>
                    </div>'
                })
            </div>"
        })
    </div>
    
    <script>
        const ctx = document.getElementById('resultsChart').getContext('2d');
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Passed', 'Failed', 'Skipped'],
                datasets: [{
                    data: [$($allResults.Summary.PassedScenarios), $($allResults.Summary.FailedScenarios), $($allResults.Summary.SkippedScenarios)],
                    backgroundColor: ['#28a745', '#dc3545', '#ffc107']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    title: {
                        display: true,
                        text: 'Test Results Distribution'
                    }
                }
            }
        });
    </script>
</body>
</html>
"@
    
    $html | Out-File -FilePath $htmlFile -Encoding UTF8
}

# Copy Cucumber reports
$cucumberReports = @(
    @{Source = "build\reports\cucumber\api\index.html"; Dest = "$reportDir\cucumber-api-report.html"},
    @{Source = "build\reports\cucumber\web\index.html"; Dest = "$reportDir\cucumber-web-report.html"},
    @{Source = "build\reports\cucumber\api\cucumber.json"; Dest = "$reportDir\cucumber-api.json"},
    @{Source = "build\reports\cucumber\web\cucumber.json"; Dest = "$reportDir\cucumber-web.json"}
)

foreach ($report in $cucumberReports) {
    if (Test-Path $report.Source) {
        Copy-Item $report.Source -Destination $report.Dest -Force
        Write-Host "Copied: $($report.Source) -> $($report.Dest)" -ForegroundColor Green
    }
}

# Stop transcript
Stop-Transcript

# Display summary in console
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST EXECUTION COMPLETED" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Overall: $($allResults.Summary.TotalScenarios) scenarios | Pass Rate: $($allResults.Summary.PassRate)%" -ForegroundColor White
Write-Host "API: $($allResults.API.Scenarios) total, $($allResults.API.Passed) passed, $($allResults.API.Failed) failed" -ForegroundColor Yellow
Write-Host "Web: $($allResults.Web.Scenarios) total, $($allResults.Web.Passed) passed, $($allResults.Web.Failed) failed" -ForegroundColor Yellow
Write-Host ""
Write-Host "Reports generated in: $reportDir" -ForegroundColor Green
Write-Host "  Summary: $summaryFile" -ForegroundColor White
Write-Host "  HTML: $htmlFile" -ForegroundColor White
Write-Host "  JSON: $jsonFile" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

# Open HTML report if requested
if ($OpenReport -and $GenerateHTML -and (Test-Path $htmlFile)) {
    Start-Process $htmlFile
}
