package com.demoblaze.stepdefinitions.web;

import com.demoblaze.web.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class WebHooks {
    
    @After("@web")
    public void tearDown(Scenario scenario) {
        try {
            WebDriver driver = DriverManager.getDriver();
            
            if (driver != null && scenario.isFailed()) {
                try {
                    TakesScreenshot ts = (TakesScreenshot) driver;
                    byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "failure-screenshot");
                } catch (Exception e) {
                    System.err.println("Failed to capture screenshot: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in tearDown: " + e.getMessage());
        } finally {
            DriverManager.quitDriver();
        }
    }
}