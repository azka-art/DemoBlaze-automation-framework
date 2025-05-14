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
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // Wait for page to be fully loaded
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Multiple selectors for Add to cart button
            By[] selectors = {
                By.linkText("Add to cart"),
                By.partialLinkText("Add to cart"),
                By.xpath("//a[contains(text(),'Add to cart')]"),
                By.xpath("//a[contains(@class,'btn-success') and contains(text(),'Add to cart')]"),
                By.cssSelector("a.btn.btn-success.btn-lg"),
                By.xpath("//a[contains(@onclick,'addToCart')]"),
                By.xpath("//div[@class='col-sm-12 col-md-6 col-lg-6']//a[contains(@class,'btn-success')]")
            };
            
            WebElement button = null;
            Exception lastException = null;
            
            // Try each selector
            for (By selector : selectors) {
                try {
                    button = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (button != null && button.isDisplayed()) {
                        System.out.println("Found button with selector: " + selector);
                        break;
                    }
                } catch (Exception e) {
                    lastException = e;
                    System.out.println("Selector failed: " + selector);
                }
            }
            
            if (button != null) {
                // Scroll the button into view
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
                Thread.sleep(1000);
                
                try {
                    button.click();
                } catch (Exception e) {
                    // If regular click fails, try JavaScript click
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                }
                
                System.out.println("Successfully clicked Add to cart button");
            } else {
                throw new RuntimeException("Add to cart button not found after trying all selectors", lastException);
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
            System.out.println("Alert text: " + alertText);
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