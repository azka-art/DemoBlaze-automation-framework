package com.demoblaze.web.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ProductDetailPage extends BasePage {
    
    @FindBy(xpath = "//a[contains(@class,'btn-success') and contains(text(),'Add to cart')]")
    private WebElement addToCartButton;
    
    @FindBy(css = ".name")
    private WebElement productName;
    
    @FindBy(css = ".price-container")
    private WebElement productPrice;
    
    public void clickAddToCart() {
        try {
            Thread.sleep(2000);
            wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
            clickElement(addToCartButton);
        } catch (Exception e) {
            System.err.println("Error clicking add to cart: " + e.getMessage());
            // Try alternative methods
            try {
                driver.navigate().refresh();
                Thread.sleep(2000);
                clickElement(addToCartButton);
            } catch (Exception ex) {
                throw new RuntimeException("Could not click add to cart button", ex);
            }
        }
    }
    
    public void acceptProductAddedAlert() {
        try {
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
        return getElementText(productName);
    }
    
    public String getProductPrice() {
        return getElementText(productPrice);
    }
}