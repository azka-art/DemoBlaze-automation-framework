package com.demoblaze.api.clients;

import com.demoblaze.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Client class for making API requests to the Demoblaze API
 */
public class ApiClient {
    private RequestSpecification request;
    private Response response;
    
    /**
     * Initializes a new API client with default headers
     */
    public ApiClient() {
        setupRequestSpec();
    }
    
    /**
     * Sets up the base request specification with common headers
     */
    private void setupRequestSpec() {
        request = RestAssured.given()
            .baseUri(ConfigManager.get("api.base.url"))
            .contentType(ContentType.JSON)
            .log().ifValidationFails();
    }
    
    /**
     * Adds a custom header to the request
     * 
     * @param name Header name
     * @param value Header value
     * @return This ApiClient instance for method chaining
     */
    public ApiClient withHeader(String name, String value) {
        request.header(name, value);
        return this;
    }
    
    /**
     * Sets the request body
     * 
     * @param body The object to be serialized as the request body
     * @return This ApiClient instance for method chaining
     */
    public ApiClient withBody(Object body) {
        request.body(body);
        return this;
    }
    
    /**
     * Executes a POST request to the specified endpoint
     * 
     * @param endpoint The API endpoint path
     * @return The Response object
     */
    public Response post(String endpoint) {
        response = request.post(endpoint);
        return response;
    }
    
    /**
     * Executes a GET request to the specified endpoint
     * 
     * @param endpoint The API endpoint path
     * @return The Response object
     */
    public Response get(String endpoint) {
        response = request.get(endpoint);
        return response;
    }
    
    /**
     * Returns the current response
     * 
     * @return The current Response object
     */
    public Response getResponse() {
        return response;
    }
}
