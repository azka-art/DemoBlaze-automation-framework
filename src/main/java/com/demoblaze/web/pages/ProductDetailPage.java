package com.demoblaze.web.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class ProductDetailPage extends BasePage {
    
    @FindBy(css = ".name")
    private WebElement productName;
    
    @FindBy(css = ".price-container")
    private WebElement productPrice;
    
    public void clickAddToCart() {
        try {
            // Multiple selectors to try
            By[] selectors = {
                By.xpath("//a[contains(text(),'Add to cart')]"),
                By.cssSelector("a.btn.btn-success.btn-lg"),
                By.xpath("//a[contains(@onclick,'addToCart')]"),
                By.linkText("Add to cart")
            };
            
            WebElement button = null;
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Try each selector
            for (By selector : selectors) {
                try {
                    button = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (button != null) break;
                } catch (Exception e) {
                    // Try next selector
                }
            }
            
            if (button != null) {
                // Scroll into view
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
                Thread.sleep(500); // Small wait for scroll
                button.click();
            } else {
                throw new RuntimeException("Add to cart button not found");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Could not click add to cart button", e);
        }
    }
    
    public void acceptProductAddedAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            System.out.println("Alert message: " + alertText);
            alert.accept();
        } catch (Exception e) {
            System.err.println("No alert found: " + e.getMessage());
        }
    }
    
    public String getProductName() {
        wait.until(ExpectedConditions.visibilityOf(productName));
        return getElementText(productName);
    }
    
    public String getProductPrice() {
        wait.until(ExpectedConditions.visibilityOf(productPrice));
        return getElementText(productPrice);
    }
}