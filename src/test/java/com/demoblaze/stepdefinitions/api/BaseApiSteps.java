package com.demoblaze.stepdefinitions.api;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseApiSteps {
    protected static Response response;
    
    @Then("the API response status code should be {int}")
    public void theAPIResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(response.getStatusCode())
            .as("API response status code")
            .isEqualTo(expectedStatusCode);
    }
}
