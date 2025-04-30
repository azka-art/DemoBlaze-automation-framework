package com.demoblaze.web.pages;

import com.demoblaze.config.ConfigManager;
import com.demoblaze.web.utils.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for all Page Objects with common methods
 */
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    
    /**
     * Constructor
     */
    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.baseUrl = ConfigManager.get("web.base.url");
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigates to the base URL
     */
    public void goToBaseUrl() {
        driver.get(baseUrl);
    }
    
    /**
     * Navigates to a specific page path
     *
     * @param path URL path to navigate to
     */
    public void navigateTo(String path) {
        driver.get(baseUrl + path);
    }
    
    /**
     * Waits for an element to be clickable and clicks it
     *
     * @param element WebElement to click
     */
    protected void clickElement(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }
    
    /**
     * Enters text into an input field
     *
     * @param element WebElement to enter text into
     * @param text Text to enter
     */
    protected void enterText(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }
    
    /**
     * Waits for an element to be visible
     *
     * @param locator By locator for the element
     * @return The WebElement once visible
     */
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Checks if an element is displayed
     *
     * @param element WebElement to check
     * @return true if the element is displayed, false otherwise
     */
    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the text from an element
     *
     * @param element WebElement to get text from
     * @return The text content of the element
     */
    protected String getElementText(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        return element.getText();
    }
}
