package com.demoblaze.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model class representing a User in the Demoblaze API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel {
    private String username;
    private String password;

    // Default constructor for deserialization
    public UserModel() {
    }
    
    public UserModel(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
