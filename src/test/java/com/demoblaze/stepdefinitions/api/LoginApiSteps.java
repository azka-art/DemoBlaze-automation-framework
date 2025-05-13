package com.demoblaze.stepdefinitions.api;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.github.javafaker.Faker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginApiSteps extends BaseApiSteps {
    private final ApiClient apiClient = new ApiClient();
    private UserModel userCredentials;
    private static final Faker faker = new Faker();
    private static String validUsername;
    private static String validPassword;
    
    @Given("the API base URL is configured")
    public void theAPIBaseURLIsConfigured() {
        // Base URL is configured in ApiClient constructor
    }
    
    @Given("I have valid user credentials")
    public void iHaveValidUserCredentials() {
        // Create a new user for testing
        if (validUsername == null) {
            validUsername = "testuser_" + System.currentTimeMillis();
            validPassword = "testpass123";
            
            // First create the user
            UserModel newUser = new UserModel(validUsername, validPassword);
            ApiClient signupClient = new ApiClient();
            signupClient.withBody(newUser).post("/signup");
        }
        
        userCredentials = new UserModel(validUsername, validPassword);
    }
    
    @Given("I have user credentials with wrong password")
    public void iHaveUserCredentialsWithWrongPassword() {
        userCredentials = new UserModel("anyuser", "wrongpassword");
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
        String responseBody = response.getBody().asString();
        System.out.println("Response body: " + responseBody);
        
        // Check if the response contains an auth token
        // The API returns a string like "Auth_token: xyz"
        assertThat(responseBody)
            .as("Response should contain auth token")
            .isNotNull()
            .isNotEmpty()
            .contains("Auth_token:");
        
        // Extract the token value
        String token = responseBody.replaceAll("\"", "").split(":")[1].trim();
        assertThat(token)
            .as("Auth token value")
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