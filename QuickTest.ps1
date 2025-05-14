# Quick test runner with report
Write-Host "Running all tests and generating report..." -ForegroundColor Green
.\RunTestsAndGenerateReport.ps1 -TestType all -GenerateHTML $true -OpenReport $true
