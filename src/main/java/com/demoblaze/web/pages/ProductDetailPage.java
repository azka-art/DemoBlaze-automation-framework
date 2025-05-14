package com.demoblaze.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import java.time.Duration;

public class ProductDetailPage extends BasePage {
    
    @FindBy(css = ".name")
    private WebElement productName;
    
    @FindBy(css = ".price-container")
    private WebElement productPrice;
    
    public void clickAddToCart() {
        try {
            Thread.sleep(2000);
            WebElement button = driver.findElement(By.xpath("//a[contains(text(),'Add to cart')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        } catch (Exception e) {
            throw new RuntimeException("Could not click add to cart button", e);
        }
    }
    
    public void acceptProductAddedAlert() {
        try {
            Thread.sleep(1000);
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            System.err.println("No alert found");
        }
    }
    
    public String getProductName() {
        return productName.getText();
    }
    
    public String getProductPrice() {
        return productPrice.getText();
    }
}