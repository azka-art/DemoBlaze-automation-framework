package com.demoblaze.web.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class LoginPage extends BasePage {

    // Multiple selectors for login button - more robust
    private By[] loginNavLinkSelectors = {
        By.id("login2"),
        By.linkText("Log in"),
        By.xpath("//a[contains(text(),'Log in')]"),
        By.xpath("//a[@id='login2']"),
        By.cssSelector("a#login2"),
        By.xpath("//nav//a[contains(text(),'Log in')]")
    };
    
    private By loginModalLocator = By.id("logInModal");
    private By usernameFieldLocator = By.id("loginusername");
    private By passwordFieldLocator = By.id("loginpassword");
    private By loginButtonLocator = By.xpath("//button[contains(@onclick,'logIn()')]");
    private By welcomeTextLocator = By.id("nameofuser");
    
    public void clickLoginNavLink() {
        try {
            System.out.println("🔍 Looking for login button with multiple selectors...");
            
            // Wait for page to be ready
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            WebElement loginButton = null;
            
            // Try each selector until we find one that works
            for (int i = 0; i < loginNavLinkSelectors.length; i++) {
                try {
                    System.out.println("Trying selector " + (i+1) + ": " + loginNavLinkSelectors[i]);
                    loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginNavLinkSelectors[i]));
                    if (loginButton != null && loginButton.isDisplayed()) {
                        System.out.println("✅ Found login button with selector: " + loginNavLinkSelectors[i]);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Selector " + (i+1) + " failed: " + e.getMessage());
                    if (i == loginNavLinkSelectors.length - 1) {
                        // Last attempt - try to find any link with "log" in it
                        try {
                            List<WebElement> allLinks = driver.findElements(By.tagName("a"));
                            for (WebElement link : allLinks) {
                                String linkText = link.getText().toLowerCase();
                                if (linkText.contains("log") && link.isDisplayed()) {
                                    loginButton = link;
                                    System.out.println("✅ Found login button by text search: " + linkText);
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Text search also failed: " + ex.getMessage());
                        }
                    }
                }
            }
            
            if (loginButton == null) {
                throw new RuntimeException("Could not find login button with any selector");
            }
            
            // Click the button
            try {
                loginButton.click();
                System.out.println("✅ Clicked login button successfully");
            } catch (Exception e) {
                System.out.println("Regular click failed, trying JavaScript click");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
            }
            
            // Wait for modal to appear
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(loginModalLocator),
                ExpectedConditions.visibilityOfElementLocated(usernameFieldLocator)
            ));
            
            System.out.println("✅ Login modal opened successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to open login modal: " + e.getMessage());
            
            // Debug: print current page source to understand what's available
            System.out.println("🔍 Current page title: " + driver.getTitle());
            System.out.println("🔍 Current URL: " + driver.getCurrentUrl());
            
            throw new RuntimeException("Failed to open login modal", e);
        }
    }

    public void enterUsername(String username) {
        try {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameFieldLocator));
            usernameField.clear();
            usernameField.sendKeys(username);
            System.out.println("Entered username: " + username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enter username", e);
        }
    }

    public void enterPassword(String password) {
        try {
            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordFieldLocator));
            passwordField.clear();
            passwordField.sendKeys(password);
            System.out.println("Entered password");
        } catch (Exception e) {
            throw new RuntimeException("Failed to enter password", e);
        }
    }

    public boolean isModalOpen() {
        try {
            WebElement modal = driver.findElement(loginModalLocator);
            return modal.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickLoginButton() {
        try {
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator));
            loginButton.click();
        } catch (Exception e) {
            throw new RuntimeException("Failed to click login button", e);
        }
    }
    
    public void waitForLoginResponse() {
        try {
            // Wait for either success (welcome message) or error (alert)
            wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElementLocated(welcomeTextLocator, "Welcome"),
                ExpectedConditions.alertIsPresent()
            ));
        } catch (Exception e) {
            System.err.println("Timeout waiting for login response: " + e.getMessage());
        }
    }

    public boolean isLoggedIn() {
        try {
            WebElement userBanner = wait.until(ExpectedConditions.presenceOfElementLocated(welcomeTextLocator));
            String text = userBanner.getText();
            System.out.println("Welcome text found: " + text);
            
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
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            try {
                Alert alert = shortWait.until(ExpectedConditions.alertIsPresent());
                String errorText = alert.getText();
                System.out.println("✅ Alert error found: " + errorText);
                alert.accept();
                return errorText;
            } catch (Exception e) {
                System.out.println("No alert found, checking other error sources...");
            }

            // Check for inline error messages
            By[] errorSelectors = {
                By.cssSelector(".text-danger"),
                By.cssSelector(".alert-danger"),
                By.cssSelector(".error"),
                By.xpath("//*[contains(@class,'error')]"),
                By.xpath("//*[contains(text(),'does not exist')]"),
                By.xpath("//*[contains(text(),'Wrong password')]")
            };

            for (By selector : errorSelectors) {
                try {
                    List<WebElement> errorElements = driver.findElements(selector);
                    for (WebElement errorElement : errorElements) {
                        if (errorElement.isDisplayed() && !errorElement.getText().trim().isEmpty()) {    
                            String errorText = errorElement.getText();
                            System.out.println("✅ Inline error found: " + errorText);
                            return errorText;
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            System.out.println("⚠️ No error message found");
            return "";

        } catch (Exception e) {
            System.err.println("❌ Error getting error message: " + e.getMessage());
            return "";
        }
    }
    
    public void clearAndVerifyUsername() {
        try {
            WebElement usernameField = driver.findElement(usernameFieldLocator);
            usernameField.clear();
        } catch (Exception e) {
            System.err.println("Error clearing username: " + e.getMessage());
        }
    }
    
    public void clearAndVerifyPassword() {
        try {
            WebElement passwordField = driver.findElement(passwordFieldLocator);
            passwordField.clear();
        } catch (Exception e) {
            System.err.println("Error clearing password: " + e.getMessage());
        }
    }
}