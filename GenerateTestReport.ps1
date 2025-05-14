# DemoBlaze Test Automation Report Generator
# This script runs all tests and generates a comprehensive report

param(
    [string]$TestType = "all",  # all, api, web
    [string]$Environment = "default",
    [string]$Browser = "chrome",
    [bool]$Headless = $false
)

# Set up report file names with timestamp
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportFileName = "TestReport_${TestType}_${Environment}_${timestamp}.txt"
$detailedLogFile = "DetailedTestLog_${timestamp}.txt"

# Function to write section header
function Write-SectionHeader {
    param([string]$Title)
    $separator = "=" * 40
    return "`n$separator`n$Title`n$separator"
}

# Function to collect system information
function Get-SystemInfo {
    return @{
        OS = [System.Environment]::OSVersion.ToString()
        ComputerName = $env:COMPUTERNAME
        PSVersion = $PSVersionTable.PSVersion.ToString()
        JavaVersion = (java -version 2>&1 | Select-String "version").ToString()
        GradleVersion = (.\gradlew --version | Select-String "Gradle").ToString()
        CurrentDirectory = Get-Location
    }
}

# Start transcript for detailed logging
Start-Transcript -Path $detailedLogFile

# Initialize report content
$report = @()
$report += "=" * 40
$report += "TEST AUTOMATION REPORT"
$report += "=" * 40
$report += "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$report += "Project: DemoBlaze Automation Framework"
$report += "Environment: $Environment"
$report += "Test Type: $TestType"
$report += "Browser: $Browser"
$report += "Headless: $Headless"
$report += "=" * 40
$report += ""

# Collect system information
Write-Host "Collecting System Information..." -ForegroundColor Yellow
$sysInfo = Get-SystemInfo
$report += Write-SectionHeader "SYSTEM INFORMATION"
$sysInfo.GetEnumerator() | ForEach-Object {
    $report += "$($_.Key): $($_.Value)"
}
$report += ""

# Check project structure
Write-Host "Checking Project Structure..." -ForegroundColor Yellow
$report += Write-SectionHeader "PROJECT STRUCTURE"

# Check feature files
$apiFeatures = Get-ChildItem -Path "src/test/resources/features/api" -Filter "*.feature" -ErrorAction SilentlyContinue
$webFeatures = Get-ChildItem -Path "src/test/resources/features/web" -Filter "*.feature" -ErrorAction SilentlyContinue

$report += "API Test Features: " + ($apiFeatures | ForEach-Object { " - $($_.Name)" })
$report += ""
$report += "Web Test Features: " + ($webFeatures | ForEach-Object { " - $($_.Name)" })
$report += ""

# Check test classes
$testClasses = Get-ChildItem -Path "src/test/java" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
$report += "Test Classes: " + ($testClasses | ForEach-Object { " - $($_.FullName)" })
$report += ""

# Clean and build
Write-Host "Cleaning and Building Project..." -ForegroundColor Yellow
$report += Write-SectionHeader "BUILD INFORMATION"
Write-Host "Running: ./gradlew clean" -ForegroundColor Cyan
$cleanOutput = .\gradlew clean 2>&1
$report += "Clean Status: " + $(if ($LASTEXITCODE -eq 0) { "SUCCESS" } else { "FAILED" })

Write-Host "Running: ./gradlew compileJava compileTestJava" -ForegroundColor Cyan
$buildOutput = .\gradlew compileJava compileTestJava 2>&1
$report += "Build Status: " + $(if ($LASTEXITCODE -eq 0) { "SUCCESS" } else { "FAILED" })
$report += ""

# Run tests based on type
Write-Host "Running Tests..." -ForegroundColor Yellow
$report += Write-SectionHeader "TEST EXECUTION"

function Run-Tests {
    param([string]$Type)
    
    $testResults = @()
    
    switch ($Type) {
        "api" {
            Write-Host "Executing API Tests..." -ForegroundColor Green
            $testResults += "Running API Tests..."
            $apiOutput = .\gradlew apiTests --no-daemon 2>&1
            $testResults += $apiOutput
        }
        "web" {
            Write-Host "Executing Web Tests..." -ForegroundColor Green
            $testResults += "Running Web Tests..."
            $cmd = ".\gradlew webTests -Dbrowser=$Browser"
            if ($Headless) { $cmd += " -Dheadless=true" }
            $webOutput = Invoke-Expression "$cmd --no-daemon 2>&1"
            $testResults += $webOutput
        }
        "all" {
            Write-Host "Executing All Tests..." -ForegroundColor Green
            $testResults += "Running All Tests..."
            
            # API Tests
            $testResults += "`nAPI Tests:"
            $apiOutput = .\gradlew apiTests --no-daemon 2>&1
            $testResults += $apiOutput
            
            # Web Tests
            $testResults += "`nWeb Tests:"
            $cmd = ".\gradlew webTests -Dbrowser=$Browser"
            if ($Headless) { $cmd += " -Dheadless=true" }
            $webOutput = Invoke-Expression "$cmd --no-daemon 2>&1"
            $testResults += $webOutput
        }
    }
    
    return $testResults
}

$testOutput = Run-Tests -Type $TestType
$report += $testOutput
$report += ""

# Parse test results
Write-Host "Parsing Test Results..." -ForegroundColor Yellow
$report += Write-SectionHeader "TEST RESULTS SUMMARY"

# Extract test counts from output
$scenarioPattern = "(\d+) Scenarios? \((.+)\)"
$stepPattern = "(\d+) Steps? \((.+)\)"

$scenarios = $testOutput | Select-String -Pattern $scenarioPattern -AllMatches
$steps = $testOutput | Select-String -Pattern $stepPattern -AllMatches

if ($scenarios) {
    $report += "Scenarios:"
    $scenarios.Matches | ForEach-Object {
        $report += "  $($_.Value)"
    }
    $report += ""
}

if ($steps) {
    $report += "Steps:"
    $steps.Matches | ForEach-Object {
        $report += "  $($_.Value)"
    }
    $report += ""
}

# Check for failures
Write-Host "Collecting Failure Information..." -ForegroundColor Yellow
$failures = $testOutput | Select-String -Pattern "FAILED|Failed|failed" -Context 2,2
if ($failures) {
    $report += Write-SectionHeader "FAILURES DETECTED"
    $failures | ForEach-Object {
        $report += $_.Line
        if ($_.Context.PreContext) { $report += $_.Context.PreContext }
        if ($_.Context.PostContext) { $report += $_.Context.PostContext }
        $report += ""
    }
}

# Extract error details
$errors = $testOutput | Select-String -Pattern "Error|Exception" -Context 1,3
if ($errors) {
    $report += Write-SectionHeader "ERROR DETAILS"
    $errors | ForEach-Object {
        $report += $_.Line
        if ($_.Context.PostContext) { $report += $_.Context.PostContext }
        $report += ""
    }
}

# Generate test coverage summary
Write-Host "Generating Test Coverage Summary..." -ForegroundColor Yellow
$report += Write-SectionHeader "TEST COVERAGE SUMMARY"

# Count feature files and scenarios
$apiScenarios = 0
$webScenarios = 0

if ($apiFeatures) {
    $apiFeatures | ForEach-Object {
        $content = Get-Content $_.FullName
        $scenarioCount = ($content | Select-String -Pattern "Scenario:" -AllMatches).Matches.Count
        $apiScenarios += $scenarioCount
        $report += "$($_.Name): $scenarioCount scenarios"
    }
}

if ($webFeatures) {
    $webFeatures | ForEach-Object {
        $content = Get-Content $_.FullName
        $scenarioCount = ($content | Select-String -Pattern "Scenario:" -AllMatches).Matches.Count
        $webScenarios += $scenarioCount
        $report += "$($_.Name): $scenarioCount scenarios"
    }
}

$report += ""
$report += "Total API Scenarios: $apiScenarios"
$report += "Total Web Scenarios: $webScenarios"
$report += "Total Scenarios: $($apiScenarios + $webScenarios)"
$report += ""

# Calculate performance metrics
Write-Host "Calculating Performance Metrics..." -ForegroundColor Yellow
$report += Write-SectionHeader "PERFORMANCE METRICS"

$buildTime = $testOutput | Select-String -Pattern "BUILD SUCCESSFUL in (\d+[ms]+)" -AllMatches
if ($buildTime) {
    $report += "Build Time: $($buildTime.Matches[0].Groups[1].Value)"
}

$testDuration = $testOutput | Select-String -Pattern "(\d+m\d+\.\d+s)" -AllMatches
if ($testDuration) {
    $report += "Test Duration: $($testDuration.Matches[0].Value)"
}
$report += ""

# Generate recommendations
Write-Host "Generating Recommendations..." -ForegroundColor Yellow
$report += Write-SectionHeader "RECOMMENDATIONS"

if ($failures -or $errors) {
    $report += "1. Fix failing tests before deployment"
    $report += "2. Review error logs and stack traces"
    $report += "3. Check for environment-specific issues"
} else {
    $report += "1. All tests passing - ready for deployment"
    $report += "2. Consider adding more test coverage"
    $report += "3. Review test execution time for optimization"
}

if ($testDuration) {
    $duration = $testDuration.Matches[0].Value
    if ($duration -match "(\d+)m") {
        $minutes = [int]$Matches[1]
        if ($minutes -gt 5) {
            $report += "4. Tests taking over 5 minutes - consider optimization"
        }
    }
}
$report += ""

# Check for report files
Write-Host "Checking File Status..." -ForegroundColor Yellow
$report += Write-SectionHeader "GENERATED REPORTS"

$reportPaths = @(
    "build/reports/cucumber/api/index.html",
    "build/reports/cucumber/web/index.html",
    "build/reports/cucumber/api/cucumber.json",
    "build/reports/cucumber/web/cucumber.json"
)

$reportPaths | ForEach-Object {
    $exists = Test-Path $_
    $report += "$_`: " + $(if ($exists) { "GENERATED" } else { "NOT FOUND" })
}
$report += ""

# Final summary
$report += Write-SectionHeader "SUMMARY"
$totalTests = 0
$passedTests = 0
$failedTests = 0

# Parse the test output for actual numbers
if ($testOutput -match "(\d+) Scenarios? \(([^)]+)\)") {
    $scenarioInfo = $Matches[2]
    if ($scenarioInfo -match "(\d+) passed") { $passedTests = [int]$Matches[1] }
    if ($scenarioInfo -match "(\d+) failed") { $failedTests = [int]$Matches[1] }
    $totalTests = $passedTests + $failedTests
}

$report += "Total Tests Run: $totalTests"
$report += "Passed: $passedTests"
$report += "Failed: $failedTests"
$report += "Success Rate: " + $(if ($totalTests -gt 0) { "{0:P}" -f ($passedTests / $totalTests) } else { "N/A" })
$report += "Report Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$report += "Report File: $reportFileName"
$report += "Detailed Log: $detailedLogFile"
$report += "=" * 40

# Save report
$report | Out-File -FilePath $reportFileName -Encoding UTF8
Write-Host "`nReport saved to: $reportFileName" -ForegroundColor Green
Write-Host "Detailed log saved to: $detailedLogFile" -ForegroundColor Green

# Stop transcript
Stop-Transcript

# Display summary
Write-Host "`n" -NoNewline
Write-Host "=" * 40 -ForegroundColor Cyan
Write-Host "REPORT SUMMARY" -ForegroundColor Cyan
Write-Host "=" * 40 -ForegroundColor Cyan
Write-Host "Total Tests Run: $totalTests" -ForegroundColor White
Write-Host "Passed: $passedTests" -ForegroundColor Green
Write-Host "Failed: $failedTests" -ForegroundColor $(if ($failedTests -gt 0) { "Red" } else { "Green" })
Write-Host "Report Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
Write-Host "Report File: $reportFileName" -ForegroundColor Yellow
Write-Host "Detailed Log: $detailedLogFile" -ForegroundColor Yellow
Write-Host "=" * 40 -ForegroundColor Cyan
