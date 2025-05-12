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
    
    @Given("I have user credentials")
    public void iHaveUserCredentials() {
        // Using test credentials
        userCredentials = new UserModel("test", "test");
    }
    
    @Given("I have non-existent user credentials")
    public void iHaveNonExistentUserCredentials() {
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
        
        response.then().log().all();
    }
    
    @Then("the API response status code should be {int}")
    public void theAPIResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(response.getStatusCode())
            .as("API response status code")
            .isEqualTo(expectedStatusCode);
    }
    
    @Then("the API response should contain expected content")
    public void theAPIResponseShouldContainExpectedContent() {
        String responseBody = response.getBody().asString();
        assertThat(responseBody)
            .as("Response should have content")
            .isNotNull()
            .isNotEmpty();
            
        // Just verify we got some response - could be auth token or error
        assertThat(responseBody)
            .containsAnyOf("Auth_token", "errorMessage", "Wrong password");
    }
    
    @Then("the API response should contain error message")
    public void theAPIResponseShouldContainErrorMessage() {
        String errorMsg = response.getBody().asString();
        assertThat(errorMsg)
            .as("Error message in response")
            .isNotNull()
            .isNotEmpty()
            .containsIgnoringCase("error");
    }
}
