# DemoBlaze Comprehensive Test Report with Retry Logic
param(
    [string]$TestType = "all",
    [bool]$GenerateFullReport = $true,
    [bool]$OpenReport = $true,
    [int]$MaxRetries = 2
)

# Create report directory
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportDir = "test-reports\final-$timestamp"
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null

# Initialize overall results
$overallResults = @{
    Summary = @{
        StartTime = Get-Date
        EndTime = $null
        Duration = $null
        TotalScenarios = 0
        PassedScenarios = 0
        FailedScenarios = 0
        RetrySuccesses = 0
        FinalPassRate = 0
    }
    API = @{
        TotalScenarios = 0
        Passed = 0
        Failed = 0
        Output = ""
    }
    Web = @{
        TotalScenarios = 0
        Passed = 0
        Failed = 0
        InitialFailures = @()
        RetryResults = @()
        Output = ""
    }
}

Write-Host "DemoBlaze Test Automation - Final Report" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
Write-Host ""

# Function to parse test output
function Parse-TestOutput {
    param([string]$Output)
    
    $result = @{
        Scenarios = 0
        Passed = 0
        Failed = 0
        FailedList = @()
    }
    
    if ($Output -match "(\d+) Scenarios? \(([^)]+)\)") {
        $result.Scenarios = [int]$Matches[1]
        $details = $Matches[2]
        
        if ($details -match "(\d+) passed") { $result.Passed = [int]$Matches[1] }
        if ($details -match "(\d+) failed") { $result.Failed = [int]$Matches[1] }
    }
    
    # Extract failed scenarios
    $lines = $Output -split "`n"
    $captureFailures = $false
    
    foreach ($line in $lines) {
        if ($line -match "Failed scenarios:") {
            $captureFailures = $true
            continue
        }
        
        if ($captureFailures -and $line -match "\.feature:\d+") {
            $result.FailedList += $line.Trim()
        }
        
        if ($captureFailures -and $line -match "^\s*$") {
            break
        }
    }
    
    return $result
}

# Function to run specific test type with retries
function Run-TestWithRetry {
    param(
        [string]$Type,
        [int]$MaxRetries
    )
    
    Write-Host "`nRunning $Type tests..." -ForegroundColor Yellow
    
    $attempt = 1
    $success = $false
    $finalResult = $null
    
    while ($attempt -le $MaxRetries -and -not $success) {
        Write-Host "Attempt $attempt of $MaxRetries" -ForegroundColor Cyan
        
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
            
            $parsed = Parse-TestOutput -Output $output
            
            if ($parsed.Failed -eq 0 -or $attempt -eq $MaxRetries) {
                $success = $true
                $finalResult = $parsed
                $finalResult.Output = $output
            } else {
                Write-Host "$($parsed.Failed) scenarios failed. Retrying..." -ForegroundColor Yellow
                
                if ($Type -eq "web") {
                    $overallResults.Web.InitialFailures += $parsed.FailedList
                }
                
                Start-Sleep -Seconds 5
            }
            
        } catch {
            Write-Host "Error during test execution: $_" -ForegroundColor Red
            if ($attempt -eq $MaxRetries) {
                $finalResult = @{
                    Scenarios = 0
                    Passed = 0
                    Failed = 0
                    FailedList = @()
                    Output = "Error: $_"
                }
            }
        }
        
        $attempt++
    }
    
    return $finalResult
}

# Run tests based on type
if ($TestType -eq "all" -or $TestType -eq "api") {
    $apiResults = Run-TestWithRetry -Type "api" -MaxRetries 1
    $overallResults.API.TotalScenarios = $apiResults.Scenarios
    $overallResults.API.Passed = $apiResults.Passed
    $overallResults.API.Failed = $apiResults.Failed
    $overallResults.API.Output = $apiResults.Output
}

if ($TestType -eq "all" -or $TestType -eq "web") {
    $webResults = Run-TestWithRetry -Type "web" -MaxRetries $MaxRetries
    $overallResults.Web.TotalScenarios = $webResults.Scenarios
    $overallResults.Web.Passed = $webResults.Passed
    $overallResults.Web.Failed = $webResults.Failed
    $overallResults.Web.Output = $webResults.Output
    
    if ($webResults.Failed -lt $overallResults.Web.InitialFailures.Count) {
        $overallResults.Summary.RetrySuccesses = $overallResults.Web.InitialFailures.Count - $webResults.Failed
    }
}

# Calculate final summary
$overallResults.Summary.EndTime = Get-Date
$overallResults.Summary.Duration = $overallResults.Summary.EndTime - $overallResults.Summary.StartTime
$overallResults.Summary.TotalScenarios = $overallResults.API.TotalScenarios + $overallResults.Web.TotalScenarios
$overallResults.Summary.PassedScenarios = $overallResults.API.Passed + $overallResults.Web.Passed
$overallResults.Summary.FailedScenarios = $overallResults.API.Failed + $overallResults.Web.Failed

if ($overallResults.Summary.TotalScenarios -gt 0) {
    $overallResults.Summary.FinalPassRate = [math]::Round(($overallResults.Summary.PassedScenarios / $overallResults.Summary.TotalScenarios) * 100, 2)
}

# Generate comprehensive HTML report
if ($GenerateFullReport) {
    $html = @"
<!DOCTYPE html>
<html>
<head>
    <title>DemoBlaze Final Test Report - $timestamp</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1400px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        h1 { color: #333; text-align: center; font-size: 2.5em; margin-bottom: 30px; }
        h2 { color: #555; border-bottom: 3px solid #007bff; padding-bottom: 10px; margin-top: 30px; }
        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 30px 0; }
        .summary-card { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
        .metric { font-size: 48px; font-weight: bold; margin: 10px 0; }
        .label { font-size: 14px; color: #666; text-transform: uppercase; }
        .passed { color: #28a745; }
        .failed { color: #dc3545; }
        .retry { color: #ffc107; }
        .total { color: #007bff; }
        .test-section { margin: 30px 0; padding: 25px; background-color: #f8f9fa; border-radius: 8px; }
        .status-badge { display: inline-block; padding: 5px 15px; border-radius: 20px; color: white; font-weight: bold; margin: 5px; }
        .status-passed { background-color: #28a745; }
        .status-failed { background-color: #dc3545; }
        .status-retried { background-color: #ffc107; }
        .progress-container { margin: 20px 0; }
        .progress-bar { width: 100%; height: 40px; background-color: #e0e0e0; border-radius: 20px; overflow: hidden; position: relative; }
        .progress-fill { height: 100%; background: linear-gradient(to right, #28a745 0%, #28a745 var(--passed), #dc3545 var(--passed), #dc3545 100%); }
        .chart-container { width: 400px; height: 400px; margin: 30px auto; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #007bff; color: white; font-weight: bold; }
        tr:hover { background-color: #f5f5f5; }
        .retry-info { background-color: #fff3cd; border: 1px solid #ffeeba; padding: 15px; border-radius: 5px; margin: 20px 0; }
        .timestamp { text-align: center; color: #666; font-size: 16px; margin: 10px 0; }
        .duration { text-align: center; color: #666; font-size: 18px; margin: 15px 0; font-weight: bold; }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <h1>DemoBlaze Test Automation - Final Report</h1>
        <p class="timestamp">Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
        <p class="duration">Total Duration: $([math]::Round($overallResults.Summary.Duration.TotalMinutes, 2)) minutes</p>
        
        <div class="summary-grid">
            <div class="summary-card">
                <div class="label">Total Scenarios</div>
                <div class="metric total">$($overallResults.Summary.TotalScenarios)</div>
            </div>
            <div class="summary-card">
                <div class="label">Passed</div>
                <div class="metric passed">$($overallResults.Summary.PassedScenarios)</div>
            </div>
            <div class="summary-card">
                <div class="label">Failed</div>
                <div class="metric failed">$($overallResults.Summary.FailedScenarios)</div>
            </div>
            <div class="summary-card">
                <div class="label">Pass Rate</div>
                <div class="metric total">$($overallResults.Summary.FinalPassRate)%</div>
            </div>
        </div>
        
        $(if ($overallResults.Summary.RetrySuccesses -gt 0) {
            "<div class='retry-info'>
                <strong>Retry Information:</strong> $($overallResults.Summary.RetrySuccesses) scenarios passed after retry
            </div>"
        })
        
        <div class="progress-container">
            <div class="progress-bar">
                <div class="progress-fill" style="--passed: $($overallResults.Summary.FinalPassRate)%"></div>
            </div>
        </div>
        
        <div class="test-section">
            <h2>API Test Results</h2>
            <span class="status-badge status-$( if ($overallResults.API.Failed -eq 0) { 'passed' } else { 'failed' } )">
                $(if ($overallResults.API.Failed -eq 0) { 'ALL PASSED' } else { 'HAS FAILURES' })
            </span>
            <table>
                <tr>
                    <th>Metric</th>
                    <th>Value</th>
                    <th>Status</th>
                </tr>
                <tr>
                    <td>Total Scenarios</td>
                    <td>$($overallResults.API.TotalScenarios)</td>
                    <td>-</td>
                </tr>
                <tr>
                    <td>Passed</td>
                    <td class="passed">$($overallResults.API.Passed)</td>
                    <td><span class="status-badge status-passed">PASSED</span></td>
                </tr>
                <tr>
                    <td>Failed</td>
                    <td class="failed">$($overallResults.API.Failed)</td>
                    <td>$(if ($overallResults.API.Failed -gt 0) { '<span class="status-badge status-failed">FAILED</span>' })</td>
                </tr>
            </table>
        </div>
        
        <div class="test-section">
            <h2>Web Test Results</h2>
            <span class="status-badge status-$( if ($overallResults.Web.Failed -eq 0) { 'passed' } else { 'failed' } )">
                $(if ($overallResults.Web.Failed -eq 0) { 'ALL PASSED' } else { 'HAS FAILURES' })
            </span>
            <table>
                <tr>
                    <th>Metric</th>
                    <th>Value</th>
                    <th>Status</th>
                </tr>
                <tr>
                    <td>Total Scenarios</td>
                    <td>$($overallResults.Web.TotalScenarios)</td>
                    <td>-</td>
                </tr>
                <tr>
                    <td>Passed</td>
                    <td class="passed">$($overallResults.Web.Passed)</td>
                    <td><span class="status-badge status-passed">PASSED</span></td>
                </tr>
                <tr>
                    <td>Failed</td>
                    <td class="failed">$($overallResults.Web.Failed)</td>
                    <td>$(if ($overallResults.Web.Failed -gt 0) { '<span class="status-badge status-failed">FAILED</span>' })</td>
                </tr>
                $(if ($overallResults.Summary.RetrySuccesses -gt 0) {
                    "<tr>
                        <td>Passed After Retry</td>
                        <td class='retry'>$($overallResults.Summary.RetrySuccesses)</td>
                        <td><span class='status-badge status-retried'>RETRIED</span></td>
                    </tr>"
                })
            </table>
        </div>
        
        <div class="chart-container">
            <canvas id="resultChart"></canvas>
        </div>
        
        <script>
            const ctx = document.getElementById('resultChart').getContext('2d');
            new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: ['API Passed', 'API Failed', 'Web Passed', 'Web Failed'],
                    datasets: [{
                        data: [
                            $($overallResults.API.Passed),
                            $($overallResults.API.Failed),
                            $($overallResults.Web.Passed),
                            $($overallResults.Web.Failed)
                        ],
                        backgroundColor: ['#28a745', '#dc3545', '#20c997', '#e74c3c']
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
                            text: 'Test Results Distribution',
                            font: {
                                size: 20
                            }
                        }
                    }
                }
            });
        </script>
    </div>
</body>
</html>
"@
    
    $htmlPath = "$reportDir\FinalTestReport_$timestamp.html"
    $html | Out-File -FilePath $htmlPath -Encoding UTF8
    
    # Save detailed results as JSON
    $jsonPath = "$reportDir\TestResults_$timestamp.json"
    $overallResults | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath
    
    # Save summary text report
    $summaryPath = "$reportDir\Summary_$timestamp.txt"
    $summary = @"
DEMOBLAZE TEST AUTOMATION - FINAL REPORT
=======================================
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Duration: $([math]::Round($overallResults.Summary.Duration.TotalMinutes, 2)) minutes

OVERALL RESULTS
==============
Total Scenarios: $($overallResults.Summary.TotalScenarios)
Passed: $($overallResults.Summary.PassedScenarios)
Failed: $($overallResults.Summary.FailedScenarios)
Pass Rate: $($overallResults.Summary.FinalPassRate)%
Retry Successes: $($overallResults.Summary.RetrySuccesses)

API TEST RESULTS
===============
Total: $($overallResults.API.TotalScenarios)
Passed: $($overallResults.API.Passed)
Failed: $($overallResults.API.Failed)

WEB TEST RESULTS
===============
Total: $($overallResults.Web.TotalScenarios)
Passed: $($overallResults.Web.Passed)
Failed: $($overallResults.Web.Failed)
"@
    $summary | Out-File -FilePath $summaryPath
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "FINAL TEST REPORT GENERATED" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Overall Pass Rate: $($overallResults.Summary.FinalPassRate)%" -ForegroundColor $(if ($overallResults.Summary.FinalPassRate -ge 80) { "Green" } else { "Yellow" })
    Write-Host "Retry Successes: $($overallResults.Summary.RetrySuccesses)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Reports:" -ForegroundColor Yellow
    Write-Host "  HTML Report: $htmlPath" -ForegroundColor White
    Write-Host "  JSON Results: $jsonPath" -ForegroundColor White
    Write-Host "  Text Summary: $summaryPath" -ForegroundColor White
    Write-Host "========================================" -ForegroundColor Cyan
    
    if ($OpenReport) {
        Start-Process $htmlPath
    }
}
