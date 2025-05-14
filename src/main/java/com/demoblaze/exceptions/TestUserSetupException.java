package com.demoblaze.exceptions;

public class TestUserSetupException extends RuntimeException {
    public TestUserSetupException(String message) {
        super(message);
    }
    
    public TestUserSetupException(String message, Throwable cause) {
        super(message, cause);
    }
}