package com.demoblaze.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();
    private static ConfigManager instance;
    private static final String DEFAULT_ENV = "default";

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
        String env = System.getProperty("test.env", DEFAULT_ENV);
        String configFile = env.equals(DEFAULT_ENV) ? "config.properties" : "config-" + env + ".properties";
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                System.out.println("Using default config.properties as " + configFile + " not found");
                try (InputStream defaultInput = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                    properties.load(defaultInput);
                }
            } else {
                properties.load(input);
                System.out.println("Loaded configuration from " + configFile);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        String systemProperty = System.getProperty(key);
        return systemProperty != null ? systemProperty : properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    public static String get(String key) {
        return getInstance().getProperty(key);
    }
    
    public static String get(String key, String defaultValue) {
        return getInstance().getProperty(key, defaultValue);
    }
}