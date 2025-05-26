package com.demoblaze.web.pages;

import com.demoblaze.config.ConfigManager;
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
        By.xpath("//nav//a[contains(text(),'Log in')]"),
        By.xpath("//a[contains(@href,'#') and contains(text(),'Log')]")
    };
    
    private By loginModalLocator = By.id("logInModal");
    private By usernameFieldLocator = By.id("loginusername");
    private By passwordFieldLocator = By.id("loginpassword");
    private By loginButtonLocator = By.xpath("//button[contains(@onclick,'logIn()')]");
    private By welcomeTextLocator = By.id("nameofuser");
    private By modalCloseButton = By.xpath("//button[@class='close']");
    private By logoutButtonLocator = By.id("logout2");
    
    // Config-driven timeouts
    private Duration getLoginModalTimeout() {
        int timeout = Integer.parseInt(ConfigManager.get("login.modal.timeout", "10"));
        return Duration.ofSeconds(timeout);
    }
    
    private Duration getLoginResponseTimeout() {
        int timeout = Integer.parseInt(ConfigManager.get("login.response.timeout", "15"));
        return Duration.ofSeconds(timeout);
    }
    
    private Duration getElementTimeout() {
        int timeout = Integer.parseInt(ConfigManager.get("element.find.timeout", "15"));
        return Duration.ofSeconds(timeout);
    }
    
    private void waitSafely(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted during wait");
        }
    }
    
    public void clickLoginNavLink() {
        try {
            System.out.println("🔐 Opening login modal...");
            
            // Wait for page to be ready
            wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Close any existing modals first
            closeExistingModals();
            
            WebElement loginButton = findLoginButton();
            
            if (loginButton == null) {
                throw new RuntimeException("Could not find login button with any selector");
            }
            
            // Click the button with retry logic
            clickLoginButtonWithRetry(loginButton);
            
            // Wait for modal to appear with config timeout
            WebDriverWait modalWait = new WebDriverWait(driver, getLoginModalTimeout());
            modalWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(loginModalLocator),
                ExpectedConditions.visibilityOfElementLocated(usernameFieldLocator)
            ));
            
            // Verify modal is actually functional
            verifyModalIsReady();
            
            System.out.println("✅ Login modal opened successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to open login modal: " + e.getMessage());
            logDebugInfo();
            throw new RuntimeException("Failed to open login modal", e);
        }
    }
    
    private void closeExistingModals() {
        try {
            // Check if any modal is already open and close it
            List<WebElement> closeButtons = driver.findElements(modalCloseButton);
            for (WebElement closeBtn : closeButtons) {
                if (closeBtn.isDisplayed()) {
                    closeBtn.click();
                    waitSafely(500);
                    System.out.println("🗂️ Closed existing modal");
                }
            }
        } catch (Exception e) {
            // Ignore - no modals to close
        }
    }
    
    private WebElement findLoginButton() {
        System.out.println("🔍 Looking for login button with multiple strategies...");
        
        WebElement loginButton = null;
        
        // Strategy 1: Try predefined selectors
        for (int i = 0; i < loginNavLinkSelectors.length; i++) {
            try {
                System.out.println("Trying selector " + (i+1) + ": " + loginNavLinkSelectors[i]);
                WebDriverWait elementWait = new WebDriverWait(driver, getElementTimeout());
                loginButton = elementWait.until(ExpectedConditions.elementToBeClickable(loginNavLinkSelectors[i]));
                if (loginButton != null && loginButton.isDisplayed()) {
                    System.out.println("✅ Found login button with selector: " + loginNavLinkSelectors[i]);
                    return loginButton;
                }
            } catch (TimeoutException e) {
                System.out.println("❌ Selector " + (i+1) + " timed out");
                continue;
            } catch (Exception e) {
                System.out.println("❌ Selector " + (i+1) + " failed: " + e.getMessage());
                continue;
            }
        }
        
        // Strategy 2: Text-based search as fallback
        System.out.println("🔍 Fallback: searching all links for login text...");
        try {
            List<WebElement> allLinks = driver.findElements(By.tagName("a"));
            for (WebElement link : allLinks) {
                try {
                    String linkText = link.getText().toLowerCase();
                    String linkId = link.getAttribute("id");
                    if ((linkText.contains("log") || "login2".equals(linkId)) && link.isDisplayed()) {
                        loginButton = link;
                        System.out.println("✅ Found login button by text/id search: " + linkText + " (id: " + linkId + ")");
                        return loginButton;
                    }
                } catch (StaleElementReferenceException e) {
                    continue; // Skip stale elements
                }
            }
        } catch (Exception e) {
            System.err.println("Text search failed: " + e.getMessage());
        }
        
        return null;
    }
    
    private void clickLoginButtonWithRetry(WebElement loginButton) {
        int maxRetries = Integer.parseInt(ConfigManager.get("login.retry.attempts", "2"));
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("🖱️ Clicking login button (attempt " + attempt + ")...");
                
                // Scroll into view first
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", loginButton);
                waitSafely(500);
                
                // Try normal click first
                loginButton.click();
                System.out.println("✅ Login button clicked successfully");
                return;
                
            } catch (ElementClickInterceptedException e) {
                System.out.println("⚠️ Click intercepted, trying JavaScript click...");
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
                    System.out.println("✅ Login button clicked with JavaScript");
                    return;
                } catch (Exception jsError) {
                    System.err.println("❌ JavaScript click also failed: " + jsError.getMessage());
                }
            } catch (Exception e) {
                System.err.println("❌ Click attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    waitSafely(1000); // Wait before retry
                }
            }
        }
        
        throw new RuntimeException("Failed to click login button after " + maxRetries + " attempts");
    }
    
    private void verifyModalIsReady() {
        try {
            // Ensure username field is actually interactable
            WebDriverWait fieldWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement usernameField = fieldWait.until(
                ExpectedConditions.elementToBeClickable(usernameFieldLocator));
            
            // Verify field is ready for input
            if (!usernameField.isEnabled()) {
                throw new RuntimeException("Username field is not enabled");
            }
            
            System.out.println("✅ Login modal is ready for input");
        } catch (Exception e) {
            System.err.println("⚠️ Modal verification failed: " + e.getMessage());
            throw new RuntimeException("Login modal not ready for input", e);
        }
    }

    public void enterUsername(String username) {
        try {
            System.out.println("👤 Entering username: " + username);
            WebDriverWait fieldWait = new WebDriverWait(driver, getElementTimeout());
            WebElement usernameField = fieldWait.until(ExpectedConditions.visibilityOfElementLocated(usernameFieldLocator));
            
            // Clear field thoroughly
            usernameField.clear();
            waitSafely(200);
            
            // Enter username
            usernameField.sendKeys(username);
            
            // Verify input was entered correctly
            String enteredValue = usernameField.getAttribute("value");
            if (!username.equals(enteredValue)) {
                // Retry input
                usernameField.clear();
                waitSafely(200);
                usernameField.sendKeys(username);
                enteredValue = usernameField.getAttribute("value");
                
                if (!username.equals(enteredValue)) {
                    throw new RuntimeException("Username input verification failed. Expected: " + username + ", Got: " + enteredValue);
                }
            }
            
            System.out.println("✅ Username entered successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to enter username: " + e.getMessage());
            throw new RuntimeException("Failed to enter username", e);
        }
    }

    public void enterPassword(String password) {
        try {
            System.out.println("🔒 Entering password...");
            WebDriverWait fieldWait = new WebDriverWait(driver, getElementTimeout());
            WebElement passwordField = fieldWait.until(ExpectedConditions.visibilityOfElementLocated(passwordFieldLocator));
            
            // Clear field thoroughly
            passwordField.clear();
            waitSafely(200);
            
            // Enter password
            passwordField.sendKeys(password);
            
            // Verify password field has content (can't verify actual value for security)
            String enteredValue = passwordField.getAttribute("value");
            if (enteredValue == null || enteredValue.length() != password.length()) {
                // Retry input
                passwordField.clear();
                waitSafely(200);
                passwordField.sendKeys(password);
            }
            
            System.out.println("✅ Password entered successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to enter password: " + e.getMessage());
            throw new RuntimeException("Failed to enter password", e);
        }
    }

    public boolean isModalOpen() {
        try {
            WebElement modal = driver.findElement(loginModalLocator);
            boolean isDisplayed = modal.isDisplayed();
            boolean hasUsernameField = !driver.findElements(usernameFieldLocator).isEmpty();
            return isDisplayed && hasUsernameField;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickLoginButton() {
        try {
            System.out.println("🚀 Submitting login...");
            
            // CRITICAL FIX: Check for existing alerts first
            try {
                Alert existingAlert = driver.switchTo().alert();
                String alertText = existingAlert.getText();
                System.out.println("⚠️ Alert already present: " + alertText);
                existingAlert.accept();
                System.out.println("✅ Existing alert handled");
                return; // Don't try to click button if alert was already there
            } catch (NoAlertPresentException e) {
                // No alert present - proceed with normal flow
                System.out.println("🔄 No existing alert - proceeding with login button click");
            }
            
            WebDriverWait buttonWait = new WebDriverWait(driver, getElementTimeout());
            WebElement loginButton = buttonWait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator));
            
            // Ensure button is visible and enabled
            if (!loginButton.isDisplayed() || !loginButton.isEnabled()) {
                throw new RuntimeException("Login button is not clickable");
            }
            
            loginButton.click();
            System.out.println("✅ Login button clicked");
        } catch (UnhandledAlertException e) {
            // Handle case where alert appears during button click
            System.out.println("⚠️ Alert appeared during login button click");
            try {
                Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                System.out.println("Alert text: " + alertText);
                alert.accept();
                System.out.println("✅ Alert handled during button click");
            } catch (Exception alertEx) {
                System.err.println("❌ Failed to handle alert: " + alertEx.getMessage());
                throw new RuntimeException("Failed to handle alert during login", alertEx);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to click login button: " + e.getMessage());
            throw new RuntimeException("Failed to click login button", e);
        }
    }
    
    // CRITICAL FIX: Enhanced waitForLoginResponse with modal closure tracking
    public void waitForLoginResponse() {
        try {
            System.out.println("⏳ Waiting for comprehensive login response...");
            WebDriverWait responseWait = new WebDriverWait(driver, getLoginResponseTimeout());
            
            // Wait for any of these conditions:
            responseWait.until(ExpectedConditions.or(
                // Success indicators (prioritized)
                ExpectedConditions.textToBePresentInElementLocated(welcomeTextLocator, "Welcome"),
                ExpectedConditions.presenceOfElementLocated(logoutButtonLocator),
                
                // Error indicators  
                ExpectedConditions.alertIsPresent(),
                
                // Modal state changes
                ExpectedConditions.invisibilityOfElementLocated(loginModalLocator),
                
                // Timeout fallback - just wait for page stability
                ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'")
            ));
            
            // Additional wait for any delayed UI updates
            waitSafely(1000);
            
            System.out.println("✅ Login response processing completed");
        } catch (TimeoutException e) {
            System.err.println("⚠️ Login response timeout - checking final state");
        } catch (Exception e) {
            System.err.println("❌ Error waiting for login response: " + e.getMessage());
        }
    }

    // CRITICAL FIX: Multi-strategy login detection with success prioritization
    public boolean isLoggedIn() {
        try {
            System.out.println("🔍 Checking login status with multiple strategies...");
            
            // Strategy 1: Check for welcome message (highest priority)
            try {
                WebDriverWait welcomeWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                WebElement userBanner = welcomeWait.until(ExpectedConditions.presenceOfElementLocated(welcomeTextLocator));
                String text = userBanner.getText();
                System.out.println("Welcome text found: " + text);
                
                if (text != null && text.toLowerCase().contains("welcome") && !text.trim().isEmpty()) {
                    System.out.println("✅ Login confirmed via welcome message");
                    return true;
                }
                
                Pattern welcomePattern = Pattern.compile("Welcome\\s+\\w+");
                if (welcomePattern.matcher(text).find()) {
                    System.out.println("✅ Login confirmed via welcome pattern match");
                    return true;
                }
            } catch (TimeoutException e) {
                System.out.println("No welcome message found within timeout");
            }
            
            // Strategy 2: Check for logout button (indicates logged in state)
            try {
                List<WebElement> logoutButtons = driver.findElements(logoutButtonLocator);
                if (!logoutButtons.isEmpty() && logoutButtons.get(0).isDisplayed()) {
                    System.out.println("✅ Login confirmed - logout button visible");
                    return true;
                }
            } catch (Exception e) {
                // Continue to next strategy
            }
            
            // Strategy 3: CRITICAL FIX - Check for recent alerts before declaring success
            try {
                // If there was a recent alert (within last few seconds), be more cautious
                Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                System.out.println("⚠️ Active alert detected: " + alertText);
                // If alert contains error messages, definitely not logged in
                if (alertText.toLowerCase().contains("wrong password") || 
                    alertText.toLowerCase().contains("does not exist") ||
                    alertText.toLowerCase().contains("fill out")) {
                    alert.accept();
                    System.out.println("❌ Login failed - error alert present");
                    return false;
                }
                alert.accept();
            } catch (NoAlertPresentException e) {
                // No alert present - continue checking
            }
            
            // Strategy 4: Check if login modal disappeared + no alerts
            try {
                WebElement modal = driver.findElement(loginModalLocator);
                boolean modalVisible = modal.isDisplayed();
                System.out.println("Modal still visible: " + modalVisible);
                
                if (!modalVisible) {
                    // Modal closed - additional verification needed
                    try {
                        driver.switchTo().alert();
                        System.out.println("Alert present - login likely failed despite modal closure");
                        return false;
                    } catch (NoAlertPresentException e) {
                        // Check if we have actual welcome content
                        try {
                            WebElement welcomeElement = driver.findElement(welcomeTextLocator);
                            String welcomeText = welcomeElement.getText();
                            if (welcomeText != null && !welcomeText.trim().isEmpty() && 
                                welcomeText.toLowerCase().contains("welcome")) {
                                System.out.println("✅ Login likely successful - modal closed, welcome text present");
                                return true;
                            }
                        } catch (Exception ex) {
                            // No welcome text
                        }
                        System.out.println("⚠️ Modal closed but no welcome text - unclear state");
                        return false;
                    }
                } else {
                    // Modal still visible - check for any login success indicators
                    try {
                        WebElement welcomeElement = driver.findElement(welcomeTextLocator);
                        String welcomeText = welcomeElement.getText();
                        if (welcomeText != null && !welcomeText.trim().isEmpty() && 
                            welcomeText.toLowerCase().contains("welcome")) {
                            System.out.println("✅ Login successful despite modal being visible");
                            return true;
                        }
                    } catch (Exception ex) {
                        // No welcome text with modal visible = not logged in
                    }
                }
            } catch (NoSuchElementException e) {
                System.out.println("✅ Login likely successful - modal not found");
                return true;
            }
            
            System.out.println("❌ Login status unclear - assuming failed");
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Error checking login status: " + e.getMessage());
            return false;
        }
    }

    // CRITICAL FIX: Enhanced error message detection with success checking
    public String getErrorMessage() {
        try {
            System.out.println("🔍 Comprehensive error message check...");
            
            // CRITICAL: Check if we're actually logged in first (no error)
            if (isLoggedIn()) {
                System.out.println("✅ User is logged in - no error");
                return "";
            }
            
            // Strategy 1: Check for alerts (most common error method)
            try {
                WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
                String errorText = alert.getText();
                System.out.println("✅ Alert error found: " + errorText);
                alert.accept();
                return errorText;
            } catch (TimeoutException e) {
                System.out.println("No alert found within timeout");
            }

            // Strategy 2: Check if modal is still open but no alert
            try {
                WebElement modal = driver.findElement(loginModalLocator);
                if (modal.isDisplayed()) {
                    // Modal open without alert could mean silent failure
                    System.out.println("⚠️ Modal still open without alert - checking field states");
                    
                    // Check if fields still have values (might indicate failed submission)
                    try {
                        WebElement usernameField = driver.findElement(usernameFieldLocator);
                        String usernameValue = usernameField.getAttribute("value");
                        if (usernameValue != null && !usernameValue.isEmpty()) {
                            System.out.println("Modal open with data - login processing might have failed silently");
                            return "Login processing failed - modal still open";
                        }
                    } catch (Exception e) {
                        // Field check failed
                    }
                }
            } catch (NoSuchElementException e) {
                // Modal not found - might be success
                System.out.println("Modal not found - likely successful");
                return "";
            }

            // Strategy 3: Look for inline error messages
            By[] errorSelectors = {
                By.cssSelector(".text-danger"),
                By.cssSelector(".alert-danger"), 
                By.cssSelector(".error"),
                By.xpath("//*[contains(@class,'error')]"),
                By.xpath("//*[contains(text(),'does not exist')]"),
                By.xpath("//*[contains(text(),'Wrong password')]"),
                By.xpath("//*[contains(text(),'User does not exist')]"),
                By.xpath("//*[contains(text(),'Please fill')]")
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

            System.out.println("⚠️ No specific error found");
            return "";

        } catch (Exception e) {
            System.err.println("❌ Error during error message check: " + e.getMessage());
            return "Error checking login status: " + e.getMessage();
        }
    }
    
    // NEW METHOD: Enhanced modal closure tracking
    public void waitForModalClosure() {
        try {
            System.out.println("⏳ Waiting for login modal to close...");
            WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Wait for modal to become invisible OR welcome message to appear
            modalWait.until(ExpectedConditions.or(
                ExpectedConditions.invisibilityOfElementLocated(loginModalLocator),
                ExpectedConditions.textToBePresentInElementLocated(welcomeTextLocator, "Welcome"),
                ExpectedConditions.alertIsPresent()
            ));
            
            System.out.println("✅ Modal state changed - login processed");
        } catch (TimeoutException e) {
            System.err.println("⚠️ Modal state timeout - checking other indicators");
        }
    }
    
    public void clearAndVerifyUsername() {
        try {
            WebElement usernameField = driver.findElement(usernameFieldLocator);
            usernameField.clear();
            String clearedValue = usernameField.getAttribute("value");
            if (clearedValue != null && !clearedValue.isEmpty()) {
                // Force clear with JavaScript
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", usernameField);
            }
            System.out.println("✅ Username field cleared");
        } catch (Exception e) {
            System.err.println("❌ Error clearing username: " + e.getMessage());
        }
    }
    
    public void clearAndVerifyPassword() {
        try {
            WebElement passwordField = driver.findElement(passwordFieldLocator);
            passwordField.clear();
            String clearedValue = passwordField.getAttribute("value");
            if (clearedValue != null && !clearedValue.isEmpty()) {
                // Force clear with JavaScript
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", passwordField);
            }
            System.out.println("✅ Password field cleared");
        } catch (Exception e) {
            System.err.println("❌ Error clearing password: " + e.getMessage());
        }
    }
    
    private void logDebugInfo() {
        try {
            System.out.println("🔍 DEBUG INFO:");
            System.out.println("Current page title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page ready state: " + 
                ((JavascriptExecutor) driver).executeScript("return document.readyState"));
        } catch (Exception e) {
            System.err.println("Failed to get debug info: " + e.getMessage());
        }
    }
}