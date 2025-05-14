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
    
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    public static synchronized WebDriver getDriver() {
        if (driver.get() == null) {
            setupDriver();
        }
        return driver.get();
    }
    
    private static void setupDriver() {
        String browser = ConfigManager.get("browser").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigManager.get("headless"));
        WebDriver webDriver = null;
        
        try {
            switch (browser) {
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (headless) {
                        firefoxOptions.addArguments("--headless");
                    }
                    // Increase connection timeout
                    firefoxOptions.setCapability("pageLoadStrategy", "eager");
                    webDriver = new FirefoxDriver(firefoxOptions);
                    break;
                case "chrome":
                default:
                    WebDriverManager.chromedriver().driverVersion("136.0.7103.92").setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--remote-allow-origins=*");
                    if (headless) {
                        chromeOptions.addArguments("--headless=new");
                    }
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                    chromeOptions.addArguments("--disable-gpu");
                    chromeOptions.addArguments("--window-size=1920,1080");
                    // Add network timeout settings
                    chromeOptions.addArguments("--disable-web-security");
                    chromeOptions.addArguments("--allow-running-insecure-content");
                    chromeOptions.setCapability("pageLoadStrategy", "eager");
                    webDriver = new ChromeDriver(chromeOptions);
                    break;
            }
            
            if (webDriver != null) {
                int timeout = Integer.parseInt(ConfigManager.get("timeout"));
                // Increase timeouts for slow connections
                webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
                webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
                webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(60));
                webDriver.manage().window().maximize();
                
                driver.set(webDriver);
            }
        } catch (Exception e) {
            System.err.println("Error setting up driver: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }
    
    public static synchronized void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
    
    public static synchronized void forceQuitDriver() {
        try {
            WebDriver webDriver = driver.get();
            if (webDriver != null) {
                webDriver.quit();
            }
        } catch (Exception e) {
            // Ignore exceptions during force quit
        } finally {
            driver.remove();
        }
    }
}