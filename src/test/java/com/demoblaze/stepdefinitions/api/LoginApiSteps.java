package com.demoblaze.stepdefinitions.api;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.github.javafaker.Faker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginApiSteps extends BaseApiSteps {
    private final ApiClient apiClient = new ApiClient();
    private UserModel userCredentials;
    private static final Faker faker = new Faker();
    
    @Given("the API base URL is configured")
    public void theAPIBaseURLIsConfigured() {
        // Base URL is configured in ApiClient constructor
    }
    
    @Given("I have valid user credentials")
    public void iHaveValidUserCredentials() {
        userCredentials = new UserModel("testuser2025", "testpassword2025");
    }
    
    @Given("I have user credentials with wrong password")
    public void iHaveUserCredentialsWithWrongPassword() {
        userCredentials = new UserModel("testuser2025", "wrongpassword");
    }
    
    @Given("I have non-existent user credentials")
    public void iHaveNonExistentUserCredentials() {
        userCredentials = new UserModel(
            faker.name().username() + System.currentTimeMillis(),
            faker.internet().password()
        );
    }
    
    @Given("I have empty user credentials")
    public void iHaveEmptyUserCredentials() {
        userCredentials = new UserModel("", "");
    }
    
    @Given("I have user credentials with special characters")
    public void iHaveUserCredentialsWithSpecialCharacters() {
        userCredentials = new UserModel("user@#$%", "pass!@#");
    }
    
    @When("I send a login request to the API")
    public void iSendLoginRequestToAPI() {
        response = apiClient
            .withBody(userCredentials)
            .post("/login");
        
        response.then().log().all();
    }
    
    @Then("the API response should contain auth token")
    public void theAPIResponseShouldContainAuthToken() {
        String token = response.jsonPath().getString("Auth_token");
        assertThat(token)
            .as("Auth token in response")
            .isNotNull()
            .isNotEmpty();
    }
    
    @Then("the API response should contain error message")
    public void theAPIResponseShouldContainErrorMessage() {
        String errorMsg = response.getBody().asString();
        assertThat(errorMsg)
            .as("Error message in response")
            .isNotNull()
            .isNotEmpty();
    }
    
    @Then("the API response should contain error message {string}")
    public void theAPIResponseShouldContainErrorMessage(String expectedError) {
        String errorMsg = response.getBody().asString();
        assertThat(errorMsg)
            .as("Error message in response")
            .contains(expectedError);
    }
}
