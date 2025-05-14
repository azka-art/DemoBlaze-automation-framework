package com.demoblaze.utils;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;

public class TestUserSeeder {
    
    private static final String[][] TEST_USERS = {
        {"testuser2025", "testpassword2025"},
        {"checkout_stable_user", "Test123"},
        {"test_user_basic", "password123"}
    };
    
    private static boolean usersSeeded = false;
    
    @BeforeAll
    public static void seedTestUsers() {
        if (!usersSeeded) {
            System.out.println("Seeding test users...");
            
            for (String[] user : TEST_USERS) {
                try {
                    ApiClient apiClient = new ApiClient();
                    UserModel userModel = new UserModel(user[0], user[1]);
                    
                    // Try to create user
                    apiClient.withBody(userModel).post("/signup");
                    System.out.println("Created/verified user: " + user[0]);
                    
                    // Small delay between users
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("User might already exist: " + user[0]);
                }
            }
            
            usersSeeded = true;
            System.out.println("Test users seeded successfully");
        }
    }
}