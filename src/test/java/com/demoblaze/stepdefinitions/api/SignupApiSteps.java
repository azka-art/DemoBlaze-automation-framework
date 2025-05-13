package com.demoblaze.stepdefinitions.api;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.github.javafaker.Faker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class SignupApiSteps extends BaseApiSteps {
    private final ApiClient apiClient = new ApiClient();
    private UserModel signupCredentials;
    private static final Faker faker = new Faker();
    
    @Given("I have new user signup credentials")
    public void iHaveNewUserSignupCredentials() {
        String uniqueUsername = faker.name().username() + System.currentTimeMillis();
        signupCredentials = new UserModel(uniqueUsername, faker.internet().password());
    }
    
    @Given("I have existing user credentials for signup")
    public void iHaveExistingUserCredentialsForSignup() {
        signupCredentials = new UserModel("testuser2025", "testpassword2025");
    }
    
    @Given("I have signup credentials with empty username")
    public void iHaveSignupCredentialsWithEmptyUsername() {
        signupCredentials = new UserModel("", faker.internet().password());
    }
    
    @Given("I have signup credentials with empty password")
    public void iHaveSignupCredentialsWithEmptyPassword() {
        signupCredentials = new UserModel(faker.name().username(), "");
    }
    
    @Given("I have signup credentials with long username")
    public void iHaveSignupCredentialsWithLongUsername() {
        String longUsername = faker.lorem().characters(100);
        signupCredentials = new UserModel(longUsername, faker.internet().password());
    }
    
    @When("I send a signup request to the API")
    public void iSendSignupRequestToAPI() {
        response = apiClient
            .withBody(signupCredentials)
            .post("/signup");
        
        response.then().log().all();
    }
    
    @Then("the API response should contain success message")
    public void theAPIResponseShouldContainSuccessMessage() {
        // The actual response from signup is empty string on success
        // So we just check the status code is 200
        assertThat(response.getStatusCode())
            .as("Signup success")
            .isEqualTo(200);
    }
}