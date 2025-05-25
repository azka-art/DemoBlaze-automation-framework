package com.demoblaze.utils;

import com.demoblaze.hooks.UnifiedHooks;

public class TestUserSetupCLI {
    public static void main(String[] args) {
        System.out.println("Setting up test users manually...");
        
        try {
            UnifiedHooks.setupGlobalTestData();
            System.out.println("Test users setup complete!");
        } catch (Exception e) {
            System.err.println("Failed to setup test users: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}