package com.demoblaze.stepdefinitions.web;

import com.demoblaze.web.pages.LoginPage;
import com.demoblaze.web.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginWebSteps {
    private LoginPage loginPage;
    
    @Before
    public void setUp() {
        loginPage = new LoginPage();
    }
    
    @Given("I am on the Demoblaze homepage")
    public void iAmOnDemoblazeHomepage() {
        loginPage.goToBaseUrl();
    }
    
    @When("I click on the login button in the navigation bar")
    public void iClickOnLoginButtonInNav() {
        loginPage.clickLoginNavLink();
    }
    
    @When("I enter valid username {string} and password {string}")
    public void iEnterValidCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }
    
    @When("I enter invalid username {string} and password {string}")
    public void iEnterInvalidCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }
    
    @When("I click the login button")
    public void iClickLoginButton() {
        loginPage.clickLoginButton();
    }
    
    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        assertThat(loginPage.isLoggedIn())
            .as("User should be logged in")
            .isTrue();
    }
    
    @Then("I should see {string} message")
    public void iShouldSeeMessage(String expectedMessage) {
        String actualMessage = loginPage.getLoggedInText();
        assertThat(actualMessage)
            .as("Welcome message")
            .contains(expectedMessage);
    }
    
    @Then("I should see an error message")
    public void iShouldSeeErrorMessage() {
        assertThat(loginPage.isLoggedIn())
            .as("User should not be logged in")
            .isFalse();
        
        // Note: In a real implementation, you'd verify the specific error message
        // This is a simplified example
    }
    
    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            // Take screenshot if scenario fails
            TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
            byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }
        
        // Cleanup driver after scenario
        DriverManager.quitDriver();
    }
}
