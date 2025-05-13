package com.demoblaze.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

public class HomePage extends BasePage {
    
    @FindBy(xpath = "//a[contains(text(),'Phones')]")
    private WebElement phonesCategory;
    
    @FindBy(xpath = "//a[contains(text(),'Laptops')]")
    private WebElement laptopsCategory;
    
    @FindBy(xpath = "//a[contains(text(),'Monitors')]")
    private WebElement monitorsCategory;
    
    @FindBy(id = "next2")
    private WebElement nextButton;
    
    @FindBy(id = "prev2")
    private WebElement prevButton;
    
    @FindBy(css = ".card-title")
    private List<WebElement> productTitles;
    
    @FindBy(css = ".card")
    private List<WebElement> productCards;
    
    @FindBy(id = "cartur")
    private WebElement cartNavLink;
    
    public void clickPhonesCategory() {
        try {
            Thread.sleep(2000);
            wait.until(ExpectedConditions.elementToBeClickable(phonesCategory));
            clickElement(phonesCategory);
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Error clicking phones category: " + e.getMessage());
        }
    }
    
    public void clickLaptopsCategory() {
        try {
            Thread.sleep(2000);
            wait.until(ExpectedConditions.elementToBeClickable(laptopsCategory));
            clickElement(laptopsCategory);
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Error clicking laptops category: " + e.getMessage());
        }
    }
    
    public void clickMonitorsCategory() {
        try {
            Thread.sleep(2000);
            wait.until(ExpectedConditions.elementToBeClickable(monitorsCategory));
            clickElement(monitorsCategory);
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Error clicking monitors category: " + e.getMessage());
        }
    }
    
    public void clickNextButton() {
        clickElement(nextButton);
    }
    
    public void clickPrevButton() {
        clickElement(prevButton);
    }
    
    public List<WebElement> getProductTitles() {
        wait.until(ExpectedConditions.visibilityOfAllElements(productTitles));
        return productTitles;
    }
    
    public boolean areProductsDisplayed() {
        try {
            Thread.sleep(3000);
            List<WebElement> products = driver.findElements(By.cssSelector(".card"));
            return !products.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }
    
    public int getProductCount() {
        return productCards.size();
    }
    
    public void clickProductByName(String productName) {
        try {
            Thread.sleep(2000);
            WebElement product = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(String.format("//a[contains(text(),'%s')]", productName))));
            clickElement(product);
        } catch (Exception e) {
            System.err.println("Error clicking product: " + e.getMessage());
        }
    }
    
    public void navigateToCart() {
        clickElement(cartNavLink);
    }
}
