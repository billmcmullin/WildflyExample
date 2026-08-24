package com.app.selenium.cucumber;

import org.testng.annotations.DataProvider;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/*
 * Separate Selenium + Cucumber(TestNG) example runner.
 *
 * PARASOFT COVERAGE PARTS IN THIS CLASS
 * - Adds "com.parasoft.coverage.integration.cucumber" to glue so Parasoft can
 *   track Cucumber scenario lifecycle and publish coverage/test results.
 */
@CucumberOptions(
        features = "classpath:features",
        glue = {
                "com.app.selenium.cucumber.steps",
                // Required Parasoft integration glue for Cucumber scenario coverage reporting.
                "com.parasoft.coverage.integration.cucumber"
        },
        plugin = {"pretty"})
public class CucumberSeleniumTestNgRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
