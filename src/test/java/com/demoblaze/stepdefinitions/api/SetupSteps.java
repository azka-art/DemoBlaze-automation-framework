package com.demoblaze.stepdefinitions.api;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.demoblaze.config.ConfigManager;
import io.cucumber.java.Before;

public class SetupSteps {
    
    @Before("@needsTestUser")
    public void createTestUser() {
        ApiClient apiClient = new ApiClient();
        String username = ConfigManager.get("test.username", "testuser2025");
        String password = ConfigManager.get("test.password", "testpassword2025");
        UserModel testUser = new UserModel(username, password);
        apiClient.withBody(testUser).post("/signup");
    }
}