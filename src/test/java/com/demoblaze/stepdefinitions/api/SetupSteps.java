package com.demoblaze.stepdefinitions.api;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import io.cucumber.java.Before;

public class SetupSteps {
    
    @Before("@needsTestUser")
    public void createTestUser() {
        ApiClient apiClient = new ApiClient();
        // Try to create the test user (it might already exist)
        UserModel testUser = new UserModel("testuser2025", "testpassword2025");
        apiClient.withBody(testUser).post("/signup");
        // We don't check the response because the user might already exist
    }
}