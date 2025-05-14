package com.demoblaze.utils;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import io.restassured.response.Response;

public class UserDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("=== User Diagnostic Tool ===");
        diagnoseUser("testuser2025", "testpassword2025");
    }
    
    public static void diagnoseUser(String username, String password) {
        try {
            System.out.println("\nDiagnosing user: " + username);
            ApiClient apiClient = new ApiClient();
            UserModel user = new UserModel(username, password);
            
            // Step 1: Try login
            System.out.println("\n1. Testing login...");
            Response loginResponse = apiClient.withBody(user).post("/login");
            System.out.println("Login Response: " + loginResponse.getStatusCode());
            System.out.println("Login Body: " + loginResponse.getBody().asString());
            
            // Step 2: Try signup if login failed
            if (loginResponse.getStatusCode() != 200) {
                System.out.println("\n2. Login failed, trying signup...");
                Response signupResponse = apiClient.withBody(user).post("/signup");
                System.out.println("Signup Response: " + signupResponse.getStatusCode());
                System.out.println("Signup Body: " + signupResponse.getBody().asString());
                
                // Step 3: Try login again
                System.out.println("\n3. Trying login after signup...");
                Thread.sleep(3000); // Wait for signup to propagate
                Response retryLoginResponse = apiClient.withBody(user).post("/login");
                System.out.println("Retry Login Response: " + retryLoginResponse.getStatusCode());
                System.out.println("Retry Login Body: " + retryLoginResponse.getBody().asString());
            }
            
        } catch (Exception e) {
            System.err.println("Error in diagnostic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}