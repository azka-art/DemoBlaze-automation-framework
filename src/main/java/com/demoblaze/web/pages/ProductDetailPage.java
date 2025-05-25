package com.demoblaze.web.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class ProductDetailPage extends BasePage {
    
    @FindBy(css = ".name")
    private WebElement productName;
    
    @FindBy(css = ".price-container")
    private WebElement productPrice;
    
    public void clickAddToCart() {
        try {
            System.out.println("🔍 Looking for Add to cart button...");
            
            // Wait for page to be fully loaded
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Multiple selectors for Add to cart button
            By[] addToCartSelectors = {
                By.linkText("Add to cart"),
                By.xpath("//a[text()='Add to cart']"),
                By.xpath("//a[contains(text(),'Add to cart')]"),
                By.cssSelector("a.btn.btn-success.btn-lg"),
                By.xpath("//a[contains(@class,'btn-success')]"),
                By.xpath("//a[contains(@onclick,'addToCart')]"),
                By.cssSelector("a[onclick*='addToCart']")
            };
            
            WebElement addToCartButton = null;
            
            // Try each selector
            for (int i = 0; i < addToCartSelectors.length; i++) {
                try {
                    System.out.println("Trying selector " + (i+1) + ": " + addToCartSelectors[i]);
                    addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(addToCartSelectors[i]));
                    if (addToCartButton != null && addToCartButton.isDisplayed()) {
                        System.out.println("✅ Found Add to cart button with selector: " + addToCartSelectors[i]);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Selector " + (i+1) + " failed: " + e.getMessage());
                }
            }
            
            // Fallback: search all buttons/links for "cart" text
            if (addToCartButton == null) {
                System.out.println("🔍 Fallback: searching all buttons for 'cart' text...");
                List<WebElement> allButtons = driver.findElements(By.tagName("a"));
                for (WebElement button : allButtons) {
                    try {
                        String buttonText = button.getText().toLowerCase();
                        if (buttonText.contains("cart") && button.isDisplayed()) {
                            addToCartButton = button;
                            System.out.println("✅ Found button via text search: " + buttonText);
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            
            if (addToCartButton != null) {
                // Scroll into view and click
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", addToCartButton);
                Thread.sleep(1000);
                
                try {
                    addToCartButton.click();
                    System.out.println("✅ Successfully clicked Add to cart button");
                } catch (Exception e) {
                    System.out.println("Regular click failed, trying JavaScript click");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartButton);
                    System.out.println("✅ Successfully clicked with JavaScript");
                }
            } else {
                System.err.println("❌ Could not find Add to cart button anywhere");
                throw new RuntimeException("Add to cart button not found");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in clickAddToCart: " + e.getMessage());
            throw new RuntimeException("Could not click add to cart button", e);
        }
    }
    
    public void acceptProductAddedAlert() {
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            System.out.println("✅ Alert text: " + alertText);
            alert.accept();
            System.out.println("✅ Alert accepted successfully");
        } catch (Exception e) {
            System.err.println("⚠️ No alert found or error accepting: " + e.getMessage());
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