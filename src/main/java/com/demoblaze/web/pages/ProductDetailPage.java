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
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // Wait for page to be fully loaded
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Wait for product details to be visible
            wait.until(ExpectedConditions.visibilityOf(productName));
            
            // Additional wait for dynamic content
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while waiting for page load");
            }
            
            // Enhanced selectors for Add to cart button
            By[] selectors = {
                By.linkText("Add to cart"),
                By.xpath("//a[text()='Add to cart']"),
                By.xpath("//a[contains(text(),'Add to cart')]"),
                By.cssSelector("a.btn.btn-success.btn-lg"),
                By.xpath("//a[contains(@class,'btn-success') and contains(text(),'Add to cart')]"),
                By.cssSelector("div.col-sm-12.col-md-6.col-lg-6 a.btn-success"),
                By.xpath("//div[contains(@class,'col-sm')]//a[contains(@class,'btn-success')]"),
                By.cssSelector("a[onclick*='addToCart']"),
                By.xpath("//a[@onclick[contains(.,'addToCart')]]")
            };
            
            WebElement button = null;
            Exception lastException = null;
            
            // Debug: Print page source to understand structure
            System.out.println("Looking for Add to cart button...");
            
            // Try each selector with explicit wait
            for (By selector : selectors) {
                try {
                    System.out.println("Trying selector: " + selector);
                    button = driver.findElement(selector);
                    
                    if (button != null && button.isDisplayed() && button.isEnabled()) {
                        System.out.println("Found button with selector: " + selector);
                        System.out.println("Button text: " + button.getText());
                        System.out.println("Button class: " + button.getAttribute("class"));
                        System.out.println("Button onclick: " + button.getAttribute("onclick"));
                        break;
                    }
                } catch (Exception e) {
                    lastException = e;
                    System.out.println("Selector failed: " + selector + " - " + e.getMessage());
                }
            }
            
            if (button == null) {
                // Last resort: find all links and check text
                List<WebElement> allLinks = driver.findElements(By.tagName("a"));
                for (WebElement link : allLinks) {
                    String linkText = link.getText();
                    System.out.println("Found link: " + linkText);
                    if (linkText.toLowerCase().contains("add to cart")) {
                        button = link;
                        System.out.println("Found button via text search: " + linkText);
                        break;
                    }
                }
            }
            
            if (button != null) {
                // Scroll into view
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Try regular click first
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(button));
                    button.click();
                    System.out.println("Successfully clicked Add to cart button with regular click");
                } catch (Exception e) {
                    System.out.println("Regular click failed, trying JavaScript click");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                    System.out.println("Successfully clicked Add to cart button with JavaScript click");
                }
                
            } else {
                // Debug information
                System.err.println("Could not find Add to cart button. Page title: " + driver.getTitle());
                System.err.println("Current URL: " + driver.getCurrentUrl());
                throw new RuntimeException("Add to cart button not found after trying all selectors", lastException);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not click add to cart button", e);
        }
    }
    
    public void acceptProductAddedAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            System.out.println("Alert text: " + alertText);
            alert.accept();
            System.out.println("Alert accepted successfully");
        } catch (Exception e) {
            System.err.println("No alert found or error accepting: " + e.getMessage());
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