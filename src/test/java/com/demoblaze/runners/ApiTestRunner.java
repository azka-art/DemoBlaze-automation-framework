package com.demoblaze.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/api",
    glue = {"com.demoblaze.stepdefinitions.api", "com.demoblaze.hooks"},
    plugin = {
        "pretty",
        "html:build/reports/cucumber/api/index.html",
        "json:build/reports/cucumber/api/cucumber.json"
    },
    tags = "@api",
    monochrome = true
)
public class ApiTestRunner {
    // This class is intentionally empty.
    // All configurations are via annotations
}