package com.app.selenium.cucumber.steps;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginAndCalculatorConfigurationTest {
    @Test
    public void normalizesApplicationAndLoginUrls() {
        String[][] cases = {
                { "http://wildfly:8080/app", "http://wildfly:8080/app" },
                { "http://wildfly:8080/app/", "http://wildfly:8080/app" },
                { "http://wildfly:8080/app/login", "http://wildfly:8080/app" },
                { "http://wildfly:8080/app/login/", "http://wildfly:8080/app" },
                { "https://example.org/custom/app/login", "https://example.org/custom/app" },
                { "http://localhost:8080/login", "http://localhost:8080" },
                { "http://localhost:8080/", "http://localhost:8080" }
        };
        for (String[] scenario : cases) {
            Assert.assertEquals(LoginAndCalculatorSteps.normalizeBaseUrl(scenario[0]), scenario[1]);
        }
    }

    @Test
    public void rejectsInvalidApplicationUrls() {
        for (String value : new String[] { "/app", "ftp://example.org/app", "http://example.org/app?x=1",
                "http://example.org/app#login", "http://user:password@example.org/app", "" }) {
            Assert.expectThrows(IllegalArgumentException.class, () -> LoginAndCalculatorSteps.normalizeBaseUrl(value));
        }
    }
}