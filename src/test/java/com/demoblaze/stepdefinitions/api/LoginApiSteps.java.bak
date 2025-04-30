package com.demoblaze.stepdefinitions.api;

import com.demoblaze.api.clients.ApiClient;
import com.demoblaze.api.models.UserModel;
import com.github.javafaker.Faker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginApiSteps {
    private final ApiClient apiClient = new ApiClient();
    private UserModel userCredentials;
    private Response response;
    private static final Faker faker = new Faker();
    
    @Given("I have valid user credentials")
    public void iHaveValidUserCredentials() {
        // In a real scenario, you might want to use existing credentials
        // Here we're using some known test credentials
        userCredentials = new UserModel("testuser", "testpassword");
    }
    
    @Given("I have invalid user credentials")
    public void iHaveInvalidUserCredentials() {
        // Generate random credentials that are likely to be invalid
        userCredentials = new UserModel(
            faker.name().username() + System.currentTimeMillis(),
            faker.internet().password()
        );
    }
    
    @When("I send a login request to the API")
    public void iSendLoginRequestToAPI() {
        response = apiClient
            .withBody(userCredentials)
            .post("/login");
        
        // Log the response for debugging
        response.then().log().ifError();
    }
    
    @Then("the API response status code should be {int}")
    public void theAPIResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(response.getStatusCode())
            .as("API response status code")
            .isEqualTo(expectedStatusCode);
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
        String errorMsg = response.jsonPath().getString("errorMessage");
        assertThat(errorMsg)
            .as("Error message in response")
            .isNotNull()
            .isNotEmpty();
    }
}
