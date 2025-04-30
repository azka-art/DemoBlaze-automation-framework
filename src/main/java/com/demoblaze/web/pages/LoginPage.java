package com.demoblaze.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object representing the Login functionality
 */
public class LoginPage extends BasePage {
    
    // Navigation elements
    @FindBy(id = "login2")
    private WebElement loginNavLink;
    
    // Login form elements
    @FindBy(id = "loginusername")
    private WebElement usernameField;
    
    @FindBy(id = "loginpassword")
    private WebElement passwordField;
    
    @FindBy(css = "button[onclick='logIn()']")
    private WebElement loginButton;
    
    // Success/Error indicators
    @FindBy(id = "nameofuser")
    private WebElement loggedInUser;
    
    // Error message popup
    private By errorAlertLocator = By.cssSelector("div.alert.alert-danger");
    
    /**
     * Clicks the login link in the navigation to open the login modal
     */
    public void clickLoginNavLink() {
        clickElement(loginNavLink);
    }
    
    /**
     * Enters username in the login form
     *
     * @param username Username to enter
     */
    public void enterUsername(String username) {
        enterText(usernameField, username);
    }
    
    /**
     * Enters password in the login form
     *
     * @param password Password to enter
     */
    public void enterPassword(String password) {
        enterText(passwordField, password);
    }
    
    /**
     * Clicks the login button to submit the form
     */
    public void clickLoginButton() {
        clickElement(loginButton);
    }
    
    /**
     * Performs the complete login flow
     *
     * @param username Username to login with
     * @param password Password to login with
     */
    public void login(String username, String password) {
        clickLoginNavLink();
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
    
    /**
     * Checks if user is logged in
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(loggedInUser),
            ExpectedConditions.presenceOfElementLocated(errorAlertLocator)
        ));
        
        try {
            return loggedInUser.isDisplayed() && 
                   loggedInUser.getText().contains("Welcome");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the error message if login failed
     *
     * @return Error message text
     */
    public String getErrorMessage() {
        WebElement alert = waitForElement(errorAlertLocator);
        return alert.getText();
    }
    
    /**
     * Gets the logged in username text
     *
     * @return Logged in username text
     */
    public String getLoggedInText() {
        wait.until(ExpectedConditions.visibilityOf(loggedInUser));
        return loggedInUser.getText();
    }
}
