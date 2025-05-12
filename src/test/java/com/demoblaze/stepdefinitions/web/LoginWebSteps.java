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
    
    @When("I log in with valid credentials")
    public void iLogInWithValidCredentials() {
        loginPage.login("test", "test");
    }

    @When("I enter username {string} and password {string}")
    public void iEnterCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("I click the login button")
    public void iClickLoginButton() {
        loginPage.clickLoginButton();
    }

    @Then("I should see a login result")
    public void iShouldSeeLoginResult() {
        // Just verify something happens after login attempt
        boolean loginProcessed = loginPage.isLoginProcessed();
        assertThat(loginProcessed)
            .as("Login should be processed")
            .isTrue();
    }

    @Then("I should see login feedback")
    public void iShouldSeeLoginFeedback() {
        // Check if any feedback is shown (alert or page change)
        boolean feedbackShown = loginPage.isFeedbackShown();
        assertThat(feedbackShown)
            .as("Login feedback should be shown")
            .isTrue();
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
            byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }

        DriverManager.quitDriver();
    }
}
