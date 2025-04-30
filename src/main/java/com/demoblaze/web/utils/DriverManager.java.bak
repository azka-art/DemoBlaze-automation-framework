package com.demoblaze.web.utils;

import com.demoblaze.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Manages WebDriver instances for UI testing
 */
public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the current WebDriver instance or creates a new one if none exists
     *
     * @return WebDriver instance
     */
    public static WebDriver getDriver() {
        if (driver.get() == null) {
            setupDriver();
        }
        return driver.get();
    }
    
    /**
     * Sets up a new WebDriver instance based on configuration
     */
    private static void setupDriver() {
        String browser = ConfigManager.get("browser").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigManager.get("headless"));
        WebDriver webDriver;
        
        switch (browser) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                webDriver = new FirefoxDriver(firefoxOptions);
                break;
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless");
                }
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                webDriver = new ChromeDriver(chromeOptions);
                break;
        }
        
        int timeout = Integer.parseInt(ConfigManager.get("timeout"));
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        webDriver.manage().window().maximize();
        
        driver.set(webDriver);
    }
    
    /**
     * Quits the WebDriver and removes the instance
     */
    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
