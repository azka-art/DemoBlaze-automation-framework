package com.demoblaze.config;

import java.util.ArrayList;
import java.util.List;

public class UserSetupConfig {
    
    public static class TestUser {
        private final String username;
        private final String password;
        
        public TestUser(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    public static List<TestUser> getTestUsers() {
        List<TestUser> users = new ArrayList<>();
        int index = 0;
        
        while (true) {
            String username = ConfigManager.get("test.users[" + index + "].username");
            String password = ConfigManager.get("test.users[" + index + "].password");
            
            if (username == null || password == null) {
                break;
            }
            
            users.add(new TestUser(username, password));
            index++;
        }
        
        // Fallback to hardcoded if config is empty
        if (users.isEmpty()) {
            users.add(new TestUser("testuser2025", "testpassword2025"));
            users.add(new TestUser("checkout_stable_user", "Test123"));
            users.add(new TestUser("test_user_basic", "password123"));
        }
        
        return users;
    }
    
    public static int getSetupTimeoutSeconds() {
        return Integer.parseInt(ConfigManager.get("user.setup.timeout.seconds", "30"));
    }
    
    public static int getPollIntervalSeconds() {
        return Integer.parseInt(ConfigManager.get("user.setup.poll.interval.seconds", "2"));
    }
    
    public static boolean isParallelSafe() {
        return Boolean.parseBoolean(ConfigManager.get("env.parallel.safe", "false"));
    }
}