package com.demoblaze.stepdefinitions.api;

import io.cucumber.java.en.Then;
import static org.assertj.core.api.Assertions.assertThat;

public class CommonApiSteps extends BaseApiSteps {
    
    @Then("the API response status code should be {int}")
    public void theAPIResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(response.getStatusCode())
            .as("API response status code")
            .isEqualTo(expectedStatusCode);
    }
    
    @Then("the API response should contain error message")
    public void theAPIResponseShouldContainErrorMessage() {
        String errorMsg = response.getBody().asString();
        assertThat(errorMsg)
            .as("Error message in response")
            .isNotNull()
            .isNotEmpty();
    }
}