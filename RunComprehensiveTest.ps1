# One-click comprehensive test and report
Write-Host "Running comprehensive test suite with retry logic..." -ForegroundColor Green
.\GenerateFinalReport.ps1 -TestType all -GenerateFullReport $true -OpenReport $true -MaxRetries 2
