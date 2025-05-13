package com.demoblaze.web.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;

public class ProductDetailPage extends BasePage {
    
    @FindBy(xpath = "//a[contains(@class,'btn-success')]")
    private WebElement addToCartButton;
    
    @FindBy(css = ".name")
    private WebElement productName;
    
    @FindBy(css = ".price-container")
    private WebElement productPrice;
    
        public void clickAddToCart() {
        try {
            Thread.sleep(3000);
            
            // Try different selectors
            By[] selectors = {
                By.xpath("//a[contains(text(),'Add to cart')]"),
                By.xpath("//a[contains(@onclick,'addToCart')]"),
                By.cssSelector("a.btn.btn-success.btn-lg"),
                By.linkText("Add to cart")
            };
            
            WebElement button = null;
            for (By selector : selectors) {
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(selector));
                    button = driver.findElement(selector);
                    if (button != null && button.isDisplayed()) {
                        break;
                    }
                } catch (Exception e) {
                    // Try next selector
                }
            }
            
            if (button != null) {
                // Scroll to element
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
                Thread.sleep(1000);
                
                // Try normal click first
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(button));
                    button.click();
                } catch (Exception e) {
                    // Fallback to JS click
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                }
                Thread.sleep(1000);
            } else {
                throw new RuntimeException("Add to cart button not found");
            }
            
        } catch (Exception e) {
            System.err.println("Error clicking add to cart: " + e.getMessage());
            throw new RuntimeException("Could not click add to cart button", e);
        }
    }
    
    public void acceptProductAddedAlert() {
        try {
            Thread.sleep(1000);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
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