package com.demoblaze.web.utils;

import com.demoblaze.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    public static synchronized WebDriver getDriver() {
        if (driver.get() == null) {
            setupDriver();
        }
        return driver.get();
    }
    
    private static void setupDriver() {
        String browser = ConfigManager.get("browser", "chrome").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigManager.get("headless", "false"));
        
        System.out.println("🚀 Initializing WebDriver: " + browser + " (headless: " + headless + ")");
        
        try {
            WebDriver webDriver = createWebDriver(browser, headless);
            
            if (webDriver != null) {
                configureTimeouts(webDriver);
                driver.set(webDriver);
                System.out.println("✅ WebDriver initialized successfully");
            } else {
                throw new RuntimeException("Failed to create WebDriver instance");
            }
            
        } catch (Exception e) {
            System.err.println("❌ WebDriver initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }
    
    private static WebDriver createWebDriver(String browser, boolean headless) {
        switch (browser) {
            case "chrome":
                return createChromeDriver(headless);
            case "firefox":
                return createFirefoxDriver(headless);
            default:
                System.out.println("⚠️ Unknown browser '" + browser + "', defaulting to Chrome");
                return createChromeDriver(headless);
        }
    }
    
    private static WebDriver createChromeDriver(boolean headless) {
        try {
            System.out.println("📦 Setting up Chrome WebDriver (auto-detect version)...");
            WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--window-size=1920,1080");
            
            if (headless) {
                options.addArguments("--headless=new");
                System.out.println("🔇 Running in headless mode");
            }
            
            return new ChromeDriver(options);
            
        } catch (Exception e) {
            System.err.println("❌ Chrome setup failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static WebDriver createFirefoxDriver(boolean headless) {
        try {
            System.out.println("📦 Setting up Firefox WebDriver...");
            WebDriverManager.firefoxdriver().setup();
            
            FirefoxOptions options = new FirefoxOptions();
            
            if (headless) {
                options.addArguments("--headless");
            }
            
            return new FirefoxDriver(options);
            
        } catch (Exception e) {
            System.err.println("❌ Firefox setup failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void configureTimeouts(WebDriver webDriver) {
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        webDriver.manage().window().maximize();
        
        System.out.println("⏱️ Timeouts configured: implicit=10s, pageLoad=45s, script=30s");
    }
    
    public static synchronized void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            try {
                webDriver.quit();
                System.out.println("✅ WebDriver closed successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Error closing WebDriver: " + e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
}