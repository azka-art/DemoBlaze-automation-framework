package com.demoblaze.hooks;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.demoblaze.exceptions.TestUserSetupException;
import io.cucumber.java.BeforeAll;
import io.restassured.response.Response;
import java.util.HashSet;
import java.util.Set;

public class GlobalHooks {
    
    private static boolean initialized = false;
    private static final Set<String> verifiedUsers = new HashSet<>();
    
    @BeforeAll
    public static void setupGlobalTestData() {
        if (!initialized) {
            System.out.println("\n=== INITIALIZING TEST USERS ===");
            
            // Setup testuser2025 explicitly
            boolean userReady = setupTestUser("testuser2025", "testpassword2025");
            
            if (userReady) {
                System.out.println("✓ testuser2025 is ready");
                verifiedUsers.add("testuser2025");
            } else {
                throw new TestUserSetupException("CRITICAL: Could not setup testuser2025");
            }
            
            // Setup other users
            setupTestUser("checkout_stable_user", "Test123");
            setupTestUser("test_user_basic", "password123");
            
            initialized = true;
            System.out.println("Verified users: " + verifiedUsers);
            System.out.println("=== INITIALIZATION COMPLETE ===\n");
        }
    }
    
    private static boolean setupTestUser(String username, String password) {
        try {
            System.out.println("\nSetting up user: " + username);
            ApiClient apiClient = new ApiClient();
            UserModel user = new UserModel(username, password);
            
            // Try to login first
            System.out.println("Checking if user exists (login attempt)...");
            Response loginResponse = apiClient.withBody(user).post("/login");
            
            if (loginResponse.getStatusCode() == 200) {
                String body = loginResponse.getBody().asString();
                if (body.contains("Auth_token") || body.contains("auth_token")) {
                    System.out.println("User already exists and can login!");
                    verifiedUsers.add(username);
                    return true;
                }
            }
            
            // If login failed, create the user
            System.out.println("User doesn't exist, creating...");
            Response signupResponse = apiClient.withBody(user).post("/signup");
            System.out.println("Signup response: " + signupResponse.getStatusCode() + 
                " - " + signupResponse.getBody().asString());
            
            // Wait a bit for user creation
            Thread.sleep(3000);
            
            // Verify login works now
            System.out.println("Verifying user can login...");
            Response verifyResponse = apiClient.withBody(user).post("/login");
            
            if (verifyResponse.getStatusCode() == 200) {
                String body = verifyResponse.getBody().asString();
                if (body.contains("Auth_token") || body.contains("auth_token")) {
                    System.out.println("User verified successfully!");
                    verifiedUsers.add(username);
                    return true;
                }
            }
            
            System.err.println("Failed to verify user: " + username);
            System.err.println("Final response: " + verifyResponse.getStatusCode() + 
                " - " + verifyResponse.getBody().asString());
            return false;
            
        } catch (Exception e) {
            System.err.println("Error setting up user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isUserVerified(String username) {
        boolean verified = verifiedUsers.contains(username);
        if (!verified) {
            System.err.println("\n!!! WARNING: User '" + username + "' is NOT verified!");
            System.err.println("!!! Verified users: " + verifiedUsers);
            System.err.println("!!! This will cause test failure!\n");
        }
        return verified;
    }
}