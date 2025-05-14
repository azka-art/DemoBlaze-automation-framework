# Fix ProductDetailPage click issue
Write-Host "Fixing ProductDetailPage click issue..." -ForegroundColor Yellow

$content = @"
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
            
            // Wait for page to be ready
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Try multiple selectors
            By[] selectors = {
                By.xpath("//a[contains(text(),'Add to cart')]"),
                By.cssSelector("a.btn.btn-success.btn-lg"),
                By.xpath("//a[contains(@onclick,'addToCart')]")
            };
            
            WebElement button = null;
            for (By selector : selectors) {
                try {
                    button = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (button != null) break;
                } catch (Exception e) {
                    continue;
                }
            }
            
            if (button != null) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true);", button);
                Thread.sleep(500);
                
                try {
                    button.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                }
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
"@

$content | Out-File -FilePath "src/main/java/com/demoblaze/web/pages/ProductDetailPage.java" -Encoding UTF8
Write-Host "✓ ProductDetailPage fixed!" -ForegroundColor Green
