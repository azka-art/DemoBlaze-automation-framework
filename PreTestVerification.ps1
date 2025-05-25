# DemoBlaze Pre-Test User Verification
Write-Host "Pre-Test User Verification" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan

# Build the project first
Write-Host "`nBuilding project..." -ForegroundColor Yellow
./gradlew clean classes testClasses

# Run UserDiagnostic to check API connectivity
Write-Host "`nChecking API connectivity and user setup..." -ForegroundColor Yellow

# Create a simple Java test class if needed
$testUserSetup = @"
package com.demoblaze.test;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import io.restassured.response.Response;

public class TestUserSetup {
    public static void main(String[] args) {
        System.out.println("=== Test User Setup ===");
        
        ApiClient apiClient = new ApiClient();
        UserModel user = new UserModel("testuser2025", "testpassword2025");
        
        // Try signup
        System.out.println("\n1. Attempting signup...");
        Response signupResponse = apiClient.withBody(user).post("/signup");
        System.out.println("Signup: " + signupResponse.getStatusCode() + " - " + signupResponse.getBody().asString());
        
        // Small delay
        try { Thread.sleep(2000); } catch (Exception e) {}
        
        // Try login
        System.out.println("\n2. Attempting login...");
        Response loginResponse = apiClient.withBody(user).post("/login");
        System.out.println("Login: " + loginResponse.getStatusCode() + " - " + loginResponse.getBody().asString());
        
        if (loginResponse.getStatusCode() == 200 && loginResponse.getBody().asString().length() > 10) {
            System.out.println("\n✓ User setup successful!");
        } else {
            System.out.println("\n✗ User setup failed!");
        }
    }
}
"@

# Create the test directory if it doesn't exist
New-Item -ItemType Directory -Path "src/test/java/com/demoblaze/test" -Force | Out-Null
$testUserSetup | Out-File -FilePath "src/test/java/com/demoblaze/test/TestUserSetup.java" -Encoding UTF8

# Compile and run
Write-Host "`nCompiling test class..." -ForegroundColor Yellow
./gradlew classes testClasses

Write-Host "`nRunning user setup test..." -ForegroundColor Yellow
java -cp "build/classes/java/main;build/classes/java/test;build/libs/*" com.demoblaze.test.TestUserSetup

Write-Host "`nNow running actual tests..." -ForegroundColor Yellow
