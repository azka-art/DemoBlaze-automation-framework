package com.demoblaze.hooks;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.demoblaze.web.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.Set;

public class UnifiedHooks {
    
    private static boolean initialized = false;
    private static final Set<String> verifiedUsers = new HashSet<>();
    
    @BeforeAll
    public static void setupGlobalTestData() {
        if (!initialized) {
            System.out.println("\n=== INITIALIZING TEST ENVIRONMENT ===");
            
            boolean primaryUserReady = setupTestUser("testuser2025", "testpassword2025");
            
            if (primaryUserReady) {
                System.out.println("✓ Primary test user ready: testuser2025");
                verifiedUsers.add("testuser2025");
            } else {
                System.err.println("⚠️ Primary user setup failed, but continuing...");
            }
            
            setupTestUser("checkout_stable_user", "Test123");
            setupTestUser("test_user_basic", "password123");
            
            initialized = true;
            System.out.println("Verified users: " + verifiedUsers);
            System.out.println("=== INITIALIZATION COMPLETE ===\n");
        }
    }
    
    private static boolean setupTestUser(String username, String password) {
        try {
            System.out.println("Setting up user: " + username);
            ApiClient apiClient = new ApiClient();
            UserModel user = new UserModel(username, password);
            
            Response loginResponse = apiClient.withBody(user).post("/login");
            
            if (loginResponse.getStatusCode() == 200) {
                String body = loginResponse.getBody().asString();
                if (body.contains("Auth_token") || body.contains("auth_token")) {
                    System.out.println("✓ User exists and can login: " + username);
                    verifiedUsers.add(username);
                    return true;
                }
            }
            
            System.out.println("Creating user: " + username);
            Response signupResponse = apiClient.withBody(user).post("/signup");
            
            Thread.sleep(2000);
            
            Response verifyResponse = apiClient.withBody(user).post("/login");
            if (verifyResponse.getStatusCode() == 200) {
                String body = verifyResponse.getBody().asString();
                if (body.contains("Auth_token") || body.contains("auth_token")) {
                    System.out.println("✓ User created and verified: " + username);
                    verifiedUsers.add(username);
                    return true;
                }
            }
            
            System.err.println("⚠️ User setup incomplete: " + username);
            return false;
            
        } catch (Exception e) {
            System.err.println("Error setting up user " + username + ": " + e.getMessage());
            return false;
        }
    }
    
    public static boolean isUserVerified(String username) {
        boolean verified = verifiedUsers.contains(username);
        if (!verified) {
            System.err.println("⚠️ User '" + username + "' not in verified list: " + verifiedUsers);
        }
        return verified;
    }
    
    @After
    public void tearDown(Scenario scenario) {
        try {
            WebDriver driver = DriverManager.getDriver();
            
            if (driver != null && scenario.isFailed()) {
                try {
                    TakesScreenshot ts = (TakesScreenshot) driver;
                    byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "failure-screenshot");
                    System.out.println("📸 Screenshot captured for failed scenario: " + scenario.getName());
                } catch (Exception e) {
                    System.err.println("Failed to capture screenshot: " + e.getMessage());
                }
            }
            
            System.out.println("🧹 Cleaning up test environment...");
        } catch (Exception e) {
            System.err.println("Error in tearDown: " + e.getMessage());
        } finally {
            DriverManager.quitDriver();
        }
    }
}