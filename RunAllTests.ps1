Write-Host "Starting DemoBlaze test execution..." -ForegroundColor Green
.\EnhancedTestReport.ps1 -TestType all -EnableRetry $true -MaxRetries 3
