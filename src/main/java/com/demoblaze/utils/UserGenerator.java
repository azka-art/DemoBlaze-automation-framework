package com.demoblaze.utils;

import com.github.javafaker.Faker;

public class UserGenerator {
    private static final Faker faker = new Faker();
    
    public static String generateUniqueUsername() {
        return "testuser_" + System.currentTimeMillis() + "_" + faker.number().randomDigit();
    }
    
    public static String generatePassword() {
        return "Test@" + faker.internet().password(8, 12, true, true, true);
    }
}