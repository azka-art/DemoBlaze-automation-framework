package com.demoblaze.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/web",
    glue = {"com.demoblaze.stepdefinitions.web", "com.demoblaze.hooks"},
    plugin = {
        "pretty",
        "html:build/reports/cucumber/web/index.html",
        "json:build/reports/cucumber/web/cucumber.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@web",
    monochrome = true
)
public class WebTestRunner {
    // This class is intentionally empty.
    // All configurations are via annotations
}