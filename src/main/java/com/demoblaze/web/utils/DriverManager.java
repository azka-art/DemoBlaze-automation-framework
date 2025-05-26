package com.demoblaze.web.utils;

import com.demoblaze.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.io.File;

public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static boolean cacheCleared = false;
    
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
                setupGlobalErrorHandler(webDriver);
                driver.set(webDriver);
                System.out.println("✅ WebDriver initialized successfully");
            } else {
                throw new RuntimeException("Failed to create WebDriver instance");
            }
            
        } catch (Exception e) {
            System.err.println("❌ WebDriver initialization failed: " + e.getMessage());
            
            // Try cache clear and retry once
            if (!cacheCleared) {
                System.out.println("🧹 Clearing WebDriverManager cache and retrying...");
                clearWebDriverCache();
                cacheCleared = true;
                
                try {
                    WebDriver webDriver = createWebDriver(browser, headless);
                    if (webDriver != null) {
                        configureTimeouts(webDriver);
                        setupGlobalErrorHandler(webDriver);
                        driver.set(webDriver);
                        System.out.println("✅ WebDriver initialized successfully after cache clear");
                        return;
                    }
                } catch (Exception retryException) {
                    System.err.println("❌ Retry after cache clear also failed: " + retryException.getMessage());
                    throw new RuntimeException("Failed to initialize WebDriver after retry", retryException);
                }
            }
            
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }
    
    private static void clearWebDriverCache() {
        try {
            System.out.println("🗑️ Clearing WebDriverManager cache...");
            
            // Clear WebDriverManager cache
            WebDriverManager.chromedriver().clearDriverCache();
            WebDriverManager.chromedriver().clearResolutionCache();
            
            // Also clear system cache directories if they exist
            String userHome = System.getProperty("user.home");
            String[] cachePaths = {
                userHome + "\\.cache\\selenium",
                userHome + "\\.wdm",
                userHome + "\\AppData\\Local\\Temp\\selenium",
                System.getProperty("java.io.tmpdir") + "\\selenium"
            };
            
            for (String cachePath : cachePaths) {
                try {
                    File cacheDir = new File(cachePath);
                    if (cacheDir.exists()) {
                        deleteDirectory(cacheDir);
                        System.out.println("🗑️ Cleared cache: " + cachePath);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Could not clear cache: " + cachePath + " - " + e.getMessage());
                }
            }
            
            System.out.println("✅ Cache clearing completed");
        } catch (Exception e) {
            System.err.println("⚠️ Cache clear failed: " + e.getMessage());
        }
    }
    
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
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
            System.out.println("📦 Setting up Chrome WebDriver...");
            
            // Setup WebDriverManager with fresh cache
            WebDriverManager chromeManager = WebDriverManager.chromedriver();
            if (cacheCleared) {
                chromeManager.clearDriverCache();
                chromeManager.clearResolutionCache();
            }
            chromeManager.setup();
            
            ChromeOptions options = new ChromeOptions();
            
            // Essential stability options
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--window-size=1920,1080");
            
            // Additional stability improvements
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=TranslateUI");
            options.addArguments("--disable-ipc-flooding-protection");
            options.addArguments("--disable-hang-monitor");
            options.addArguments("--disable-client-side-phishing-detection");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-default-apps");
            options.addArguments("--disable-prompt-on-repost");
            options.addArguments("--disable-sync");
            
            // Network and performance
            options.addArguments("--aggressive-cache-discard");
            options.addArguments("--memory-pressure-off");
            
            // Logging preferences
            options.addArguments("--log-level=3"); // Suppress INFO, WARNING, ERROR
            options.addArguments("--silent");
            
            if (headless) {
                options.addArguments("--headless=new");
                options.addArguments("--disable-logging");
                options.addArguments("--disable-gpu-logging");
                System.out.println("🔇 Running in headless mode");
            }
            
            // Performance preferences
            options.setExperimentalOption("useAutomationExtension", false);
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            
            System.out.println("✅ Chrome options configured");
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
            
            // Firefox specific preferences
            options.addPreference("dom.webnotifications.enabled", false);
            options.addPreference("media.volume_scale", "0.0");
            
            return new FirefoxDriver(options);
            
        } catch (Exception e) {
            System.err.println("❌ Firefox setup failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void configureTimeouts(WebDriver webDriver) {
        try {
            // Use config-driven timeouts with fallbacks
            int implicitWait = Integer.parseInt(ConfigManager.get("implicit.wait", "5"));
            int pageLoadTimeout = Integer.parseInt(ConfigManager.get("page.load.timeout", "45"));
            int scriptTimeout = Integer.parseInt(ConfigManager.get("script.timeout", "30"));
            
            webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(scriptTimeout));
            
            // Window management
            boolean shouldMaximize = Boolean.parseBoolean(ConfigManager.get("browser.window.maximize", "true"));
            if (shouldMaximize) {
                webDriver.manage().window().maximize();
            }
            
            System.out.println("⏱️ Timeouts configured: implicit=" + implicitWait + "s, pageLoad=" + pageLoadTimeout + "s, script=" + scriptTimeout + "s");
        } catch (Exception e) {
            System.err.println("⚠️ Error configuring timeouts: " + e.getMessage());
            // Use default timeouts as fallback
            webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
            webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            webDriver.manage().window().maximize();
            System.out.println("⏱️ Using default timeouts due to config error");
        }
    }
    
    private static void setupGlobalErrorHandler(WebDriver webDriver) {
        try {
            // Set up global JavaScript error handler
            ((JavascriptExecutor) webDriver).executeScript(
                "window.addEventListener('error', function(e) { " +
                "  console.warn('JS Error caught:', e.error ? e.error.message : e.message); " +
                "});" +
                "window.addEventListener('unhandledrejection', function(e) { " +
                "  console.warn('Promise rejection caught:', e.reason); " +
                "});"
            );
            System.out.println("🛡️ Global error handler configured");
        } catch (Exception e) {
            System.err.println("⚠️ Could not setup global error handler: " + e.getMessage());
        }
    }
    
    public static synchronized void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            try {
                System.out.println("🧹 Cleaning up WebDriver...");
                
                // Clear browser state before quitting
                try {
                    webDriver.manage().deleteAllCookies();
                    ((JavascriptExecutor) webDriver).executeScript("localStorage.clear(); sessionStorage.clear();");
                } catch (Exception e) {
                    System.out.println("⚠️ Could not clear browser state: " + e.getMessage());
                }
                
                webDriver.quit();
                System.out.println("✅ WebDriver closed successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Error closing WebDriver: " + e.getMessage());
            } finally {
                driver.remove();
            }
        }
    }
    
    public static boolean isDriverActive() {
        try {
            WebDriver webDriver = driver.get();
            if (webDriver != null) {
                // Try to get current URL to test if driver is responsive
                webDriver.getCurrentUrl();
                return true;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Driver health check failed: " + e.getMessage());
        }
        return false;
    }
    
    public static void refreshDriver() {
        System.out.println("🔄 Refreshing WebDriver...");
        quitDriver();
        // Next getDriver() call will create a new instance
    }
}