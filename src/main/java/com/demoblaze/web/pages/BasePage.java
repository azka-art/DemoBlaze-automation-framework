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

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    
    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.baseUrl = ConfigManager.get("web.base.url");
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
        System.out.println("ðŸ“„ BasePage initialized for: " + this.getClass().getSimpleName());
    }
    
    public void goToBaseUrl() {
        try {
            System.out.println("ðŸŒ Navigating to: " + baseUrl);
            
            driver.get(baseUrl);
            
            // Wait for page to be ready
            wait.until(driver -> 
                ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete")
            );
            
            // Wait for basic page structure - be flexible
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.tagName("body")),
                ExpectedConditions.presenceOfElementLocated(By.tagName("nav")),
                ExpectedConditions.presenceOfElementLocated(By.id("navbarExample"))
            ));
            
            System.out.println("âœ… Page loaded successfully");
            
        } catch (Exception e) {
            System.err.println("âŒ Failed to load page: " + e.getMessage());
            
            try {
                System.out.println("ðŸ”„ Retrying navigation...");
                Thread.sleep(2000);
                driver.navigate().refresh();
                
                wait.until(driver -> 
                    ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete")
                );
                
                System.out.println("âœ… Page loaded on retry");
                
            } catch (Exception retryError) {
                System.err.println("âŒ Retry failed: " + retryError.getMessage());
                throw new RuntimeException("Unable to load page: " + baseUrl, retryError);
            }
        }
    }
    
    protected void clickElement(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            System.out.println("ðŸ–±ï¸ Using JavaScript click fallback");
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