package com.demoblaze.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages configuration properties for the automation framework
 */
public class ConfigManager {
    private static final Properties properties = new Properties();
    private static ConfigManager instance;

    private ConfigManager() {
        loadProperties();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        String systemProperty = System.getProperty(key);
        return systemProperty != null ? systemProperty : properties.getProperty(key);
    }
    
    // Static helper to get property directly
    public static String get(String key) {
        return getInstance().getProperty(key);
    }
}
