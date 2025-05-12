package com.demoblaze.web.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ProductDetailPage extends BasePage {
    
    @FindBy(css = "a.btn.btn-success.btn-lg")
    private WebElement addToCartButton;
    
    @FindBy(css = ".name")
    private WebElement productName;
    
    @FindBy(css = ".price-container")
    private WebElement productPrice;
    
    public void clickAddToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
        clickElement(addToCartButton);
    }
    
    public void acceptProductAddedAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        System.out.println("Alert message: " + alertText);
        alert.accept();
    }
    
    public String getProductName() {
        return getElementText(productName);
    }
    
    public String getProductPrice() {
        return getElementText(productPrice);
    }
}
