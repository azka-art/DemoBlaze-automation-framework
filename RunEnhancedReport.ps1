# Run enhanced test report
Write-Host "Running enhanced test report..." -ForegroundColor Green
.\EnhancedTestReport.ps1 -TestType all -GenerateHTML $true -OpenReport $true -Verbose $true
