# Run this to test basic setup
Write-Host "Testing basic setup..." -ForegroundColor Yellow

# Test 1: Check if config file exists
if (Test-Path "src/main/resources/config.properties") {
    Write-Host "✓ Config file exists" -ForegroundColor Green
} else {
    Write-Host "✗ Config file missing" -ForegroundColor Red
}

# Test 2: Check web.base.url property
$config = Get-Content "src/main/resources/config.properties" | Select-String "web.base.url"
if ($config) {
    Write-Host "✓ web.base.url found: $config" -ForegroundColor Green
} else {
    Write-Host "✗ web.base.url not found" -ForegroundColor Red
}

# Test 3: Check if UserGenerator exists
if (Test-Path "src/main/java/com/demoblaze/utils/UserGenerator.java") {
    Write-Host "✓ UserGenerator exists" -ForegroundColor Green
} else {
    Write-Host "✗ UserGenerator missing" -ForegroundColor Red
}

# Test 4: Check WebDriver manager
Write-Host "`nRunning minimal driver test..." -ForegroundColor Yellow
$driverTest = @"
package com.demoblaze.test;

import com.demoblaze.web.utils.DriverManager;
import org.openqa.selenium.WebDriver;

public class DriverTest {
    public static void main(String[] args) {
        try {
            WebDriver driver = DriverManager.getDriver();
            driver.get("https://www.demoblaze.com");
            Thread.sleep(2000);
            System.out.println("Success: " + driver.getTitle());
            DriverManager.quitDriver();
        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
"@
Write-FileWithoutBOM -Path "src/test/java/com/demoblaze/test/DriverTest.java" -Content $driverTest

Write-Host "`nRun './gradlew webTests --debug' for more detailed output" -ForegroundColor Yellow
