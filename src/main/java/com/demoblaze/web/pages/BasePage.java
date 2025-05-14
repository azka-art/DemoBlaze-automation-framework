package com.demoblaze.web.pages;

import com.demoblaze.config.ConfigManager;
import com.demoblaze.web.utils.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY = 3000;
    
    public BasePage() {
        try {
            this.driver = DriverManager.getDriver();
            this.baseUrl = ConfigManager.get("web.base.url");
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            PageFactory.initElements(driver, this);
        } catch (Exception e) {
            System.err.println("Error initializing page: " + e.getMessage());
            throw new RuntimeException("Failed to initialize page", e);
        }
    }
    
    private boolean isUrlReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            System.err.println("URL not reachable: " + e.getMessage());
            return false;
        }
    }
    
    public void goToBaseUrl() {
        // Pre-flight check
        if (!isUrlReachable(baseUrl)) {
            System.err.println("URL is not reachable: " + baseUrl);
            throw new RuntimeException("Cannot reach base URL: " + baseUrl);
        }
        
        int retries = 0;
        Exception lastException = null;
        
        while (retries < MAX_RETRIES) {
            try {
                System.out.println("Loading URL: " + baseUrl + " (attempt " + (retries + 1) + ")");
                
                // Set page load timeout
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
                
                // Navigate to URL
                driver.get(baseUrl);
                
                // Wait for page to be loaded
                wait.until(driver -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").equals("complete"));
                
                // Wait for key elements to be present
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.id("nava")),
                    ExpectedConditions.presenceOfElementLocated(By.tagName("nav")),
                    ExpectedConditions.presenceOfElementLocated(By.className("navbar")),
                    ExpectedConditions.presenceOfElementLocated(By.id("navbarExample"))
                ));
                
                // Additional wait for dynamic content
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.className("card")),
                    ExpectedConditions.presenceOfElementLocated(By.id("tbodyid"))
                ));
                
                System.out.println("Successfully loaded URL: " + baseUrl);
                return;
                
            } catch (Exception e) {
                lastException = e;
                retries++;
                System.err.println("Failed to load URL (attempt " + retries + "): " + e.getMessage());
                
                if (retries < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Try to refresh or navigate again
                    try {
                        driver.navigate().refresh();
                    } catch (Exception refreshError) {
                        System.err.println("Refresh failed, trying new navigation");
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to load base URL after " + MAX_RETRIES + " attempts", lastException);
    }
    
    protected void clickElement(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        
        try {
            element.click();
        } catch (Exception e) {
            // Try JavaScript click if regular click fails
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
    
    protected void enterText(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }
    
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    protected String getElementText(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        return element.getText();
    }
}