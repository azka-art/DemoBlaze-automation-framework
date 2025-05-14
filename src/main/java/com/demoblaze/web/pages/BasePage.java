package com.demoblaze.web.pages;

import com.demoblaze.config.ConfigManager;
import com.demoblaze.web.utils.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    private static final int MAX_RETRIES = 3;
    
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
    
    public void goToBaseUrl() {
        int retries = 0;
        Exception lastException = null;
        
        while (retries < MAX_RETRIES) {
            try {
                if (driver == null) {
                    throw new RuntimeException("WebDriver is not initialized");
                }
                
                System.out.println("Attempting to load URL: " + baseUrl + " (attempt " + (retries + 1) + ")");
                driver.get(baseUrl);
                
                // Wait for page to be ready
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.id("nava")),
                    ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
                ));
                
                // If we get here, the page loaded successfully
                System.out.println("Successfully loaded URL: " + baseUrl);
                return;
                
            } catch (Exception e) {
                lastException = e;
                retries++;
                System.err.println("Failed to load URL (attempt " + retries + "): " + e.getMessage());
                
                if (retries < MAX_RETRIES) {
                    try {
                        Thread.sleep(5000); // Wait 5 seconds before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to load base URL after " + MAX_RETRIES + " attempts", lastException);
    }
    
    protected void clickElement(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
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