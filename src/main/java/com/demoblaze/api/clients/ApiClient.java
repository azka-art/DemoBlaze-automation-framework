package com.demoblaze.api.clients;

import com.demoblaze.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiClient {
    private Response response;
    private Object requestBody;
    private String baseUrl;
    private static int requestCounter = 0;
    
    public ApiClient() {
        this.baseUrl = ConfigManager.get("api.base.url");
        System.out.println("ApiClient initialized with base URL: " + baseUrl);
    }
    
    private RequestSpecification createRequestSpec() {
        requestCounter++;
        System.out.println("\n=== API Request #" + requestCounter + " ===");
        
        return RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .header("User-Agent", "Mozilla/5.0")
            .log().all();
    }
    
    public ApiClient withBody(Object body) {
        this.requestBody = body;
        return this;
    }
    
    public Response post(String endpoint) {
        try {
            RequestSpecification request = createRequestSpec();
            
            if (requestBody != null) {
                request.body(requestBody);
            }
            
            System.out.println("POST to: " + baseUrl + endpoint);
            response = request.post(endpoint);
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody().asString());
            System.out.println("=== End Request #" + requestCounter + " ===\n");
            
            return response;
            
        } catch (Exception e) {
            System.err.println("API request failed: " + e.getMessage());
            throw new RuntimeException("API request failed", e);
        }
    }
    
    public Response get(String endpoint) {
        try {
            RequestSpecification request = createRequestSpec();
            
            System.out.println("GET from: " + baseUrl + endpoint);
            response = request.get(endpoint);
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody().asString());
            System.out.println("=== End Request #" + requestCounter + " ===\n");
            
            return response;
            
        } catch (Exception e) {
            System.err.println("API request failed: " + e.getMessage());
            throw new RuntimeException("API request failed", e);
        }
    }
    
    public Response getResponse() {
        return response;
    }
}