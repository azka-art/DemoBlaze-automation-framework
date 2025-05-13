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
            
            // Try multiple methods to click the button
            try {
                wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
                addToCartButton.click();
            } catch (Exception e) {
                // Try JavaScript click
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartButton);
            }
            
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Error clicking add to cart: " + e.getMessage());
            // Try to find button by different selector
            try {
                WebElement button = driver.findElement(By.cssSelector("a.btn.btn-success"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            } catch (Exception ex) {
                throw new RuntimeException("Could not click add to cart button", ex);
            }
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