package com.demoblaze.web.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class LoginPage extends BasePage {

    // Specific locators for better reliability
    private By loginNavLinkLocator = By.id("login2");
    private By loginModalLocator = By.id("logInModal");
    private By modalDialogLocator = By.cssSelector("#logInModal .modal-dialog");
    private By modalContentLocator = By.cssSelector("#logInModal .modal-content");
    private By usernameFieldLocator = By.id("loginusername");
    private By passwordFieldLocator = By.id("loginpassword");
    private By loginButtonLocator = By.xpath("//button[contains(@onclick,'logIn()')]");
    private By welcomeTextLocator = By.id("nameofuser");
    private By inlineErrorLocator = By.cssSelector("#logInModal .text-danger, .modal-body .text-danger");
    private By modalBackdropLocator = By.className("modal-backdrop");
    
    public void clickLoginNavLink() {
        try {
            System.out.println("Waiting for login nav link to be clickable...");
            
            // Wait for page to be ready and login link to be clickable
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
                
            WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(loginNavLinkLocator));
            System.out.println("Login link found and clickable");
            
            // Try standard click first
            try {
                loginLink.click();
            } catch (Exception e) {
                System.out.println("Standard click failed, trying JavaScript click");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginLink);
            }
            
            System.out.println("Clicked login link, waiting for modal...");
            
            // Wait for modal to be visible - multiple conditions
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(modalDialogLocator),
                ExpectedConditions.visibilityOfElementLocated(modalContentLocator),
                ExpectedConditions.attributeContains(loginModalLocator, "class", "show"),
                ExpectedConditions.attributeContains(loginModalLocator, "style", "display: block")
            ));
            
            // Additional wait for modal animation to complete
            wait.until(ExpectedConditions.visibilityOfElementLocated(usernameFieldLocator));
            wait.until(ExpectedConditions.elementToBeClickable(usernameFieldLocator));
            
            System.out.println("Login modal is now open and ready");
        } catch (Exception e) {
            System.err.println("Failed to open login modal: " + e.getMessage());
            throw new RuntimeException("Failed to open login modal", e);
        }
    }

    public void enterUsername(String username) {
        try {
            // Verify modal is open first
            if (!isModalOpen()) {
                throw new RuntimeException("Login modal is not open - cannot enter username");
            }
            
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameFieldLocator));
            wait.until(ExpectedConditions.elementToBeClickable(usernameField));
            
            // Clear field using multiple methods
            usernameField.clear();
            usernameField.sendKeys(Keys.CONTROL + "a");
            usernameField.sendKeys(Keys.DELETE);
            
            usernameField.sendKeys(username);
            System.out.println("Entered username: " + username);
        } catch (Exception e) {
            System.err.println("Error entering username: " + e.getMessage());
            throw new RuntimeException("Failed to enter username", e);
        }
    }

    public void enterPassword(String password) {
        try {
            // Verify modal is open first
            if (!isModalOpen()) {
                throw new RuntimeException("Login modal is not open - cannot enter password");
            }
            
            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordFieldLocator));
            wait.until(ExpectedConditions.elementToBeClickable(passwordField));
            
            // Clear field using multiple methods
            passwordField.clear();
            passwordField.sendKeys(Keys.CONTROL + "a");
            passwordField.sendKeys(Keys.DELETE);
            
            passwordField.sendKeys(password);
            System.out.println("Entered password");
        } catch (Exception e) {
            System.err.println("Error entering password: " + e.getMessage());
            throw new RuntimeException("Failed to enter password", e);
        }
    }

    public boolean isModalOpen() {
        try {
            WebElement modal = driver.findElement(loginModalLocator);
            String displayStyle = modal.getCssValue("display");
            String classAttribute = modal.getAttribute("class");
            
            return displayStyle.equals("block") || classAttribute.contains("show");
        } catch (Exception e) {
            return false;
        }
    }

    public void clickLoginButton() {
        try {
            if (!isModalOpen()) {
                throw new RuntimeException("Login modal is not open - cannot click login button");
            }
            
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator));
            
            // Scroll to button if needed
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
            
            try {
                loginButton.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
            }
        } catch (Exception e) {
            System.err.println("Error clicking login button: " + e.getMessage());
            throw new RuntimeException("Failed to click login button", e);
        }
    }
    
    public void waitForLoginResponse() {
        try {
            // Wait for response - either modal closes or error appears
            wait.until(ExpectedConditions.or(
                ExpectedConditions.invisibilityOfElementLocated(modalBackdropLocator),
                ExpectedConditions.presenceOfElementLocated(inlineErrorLocator),
                ExpectedConditions.alertIsPresent(),
                ExpectedConditions.textToBePresentInElementLocated(welcomeTextLocator, "Welcome")
            ));
        } catch (Exception e) {
            System.err.println("Timeout waiting for login response: " + e.getMessage());
        }
    }

    public void login(String username, String password) {
        clickLoginNavLink();
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }

    public boolean isLoggedIn() {
        try {
            // Check if modal closed
            List<WebElement> modalBackdrops = driver.findElements(modalBackdropLocator);
            boolean modalClosed = modalBackdrops.isEmpty() || modalBackdrops.stream().noneMatch(WebElement::isDisplayed);
            
            if (!modalClosed) {
                System.out.println("Login modal still visible - login failed");
                return false;
            }
            
            // Wait for welcome text and verify it matches expected pattern
            WebElement userBanner = wait.until(ExpectedConditions.presenceOfElementLocated(welcomeTextLocator));
            String text = userBanner.getText();
            System.out.println("Welcome text found: " + text);
            
            // Check if text matches "Welcome username" pattern
            Pattern welcomePattern = Pattern.compile("Welcome\\s+\\w+");
            return welcomePattern.matcher(text).find();
        } catch (Exception e) {
            System.out.println("Not logged in: " + e.getMessage());
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            // First check for alerts
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            try {
                Alert alert = shortWait.until(ExpectedConditions.alertIsPresent());
                String errorText = alert.getText();
                System.out.println("Alert error: " + errorText);
                alert.accept();
                return errorText;
            } catch (TimeoutException e) {
                // No alert, check inline errors
            }
            
            // Check for inline error messages in modal
            List<WebElement> errorElements = driver.findElements(inlineErrorLocator);
            for (WebElement errorElement : errorElements) {
                if (errorElement.isDisplayed()) {
                    String errorText = errorElement.getText();
                    System.out.println("Inline error: " + errorText);
                    return errorText;
                }
            }
            
            return "";
        } catch (Exception e) {
            System.err.println("Error getting error message: " + e.getMessage());
            return "";
        }
    }
    
    public void clearAndVerifyUsername() {
        try {
            WebElement usernameField = driver.findElement(usernameFieldLocator);
            usernameField.clear();
            usernameField.sendKeys("");
            
            // Verify field is empty
            wait.until(ExpectedConditions.attributeToBe(usernameField, "value", ""));
        } catch (Exception e) {
            System.err.println("Error clearing username: " + e.getMessage());
        }
    }
    
    public void clearAndVerifyPassword() {
        try {
            WebElement passwordField = driver.findElement(passwordFieldLocator);
            passwordField.clear();
            passwordField.sendKeys("");
            
            // Verify field is empty
            wait.until(ExpectedConditions.attributeToBe(passwordField, "value", ""));
        } catch (Exception e) {
            System.err.println("Error clearing password: " + e.getMessage());
        }
    }
}