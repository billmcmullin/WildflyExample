package com.app.selenium.cucumber.steps;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.parasoft.coverage.integration.selenium.SeleniumCoverageIntegration;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/*
 * Separate Selenium + Cucumber(TestNG) step-definition example.
 *
 * PARASOFT COVERAGE PARTS IN THIS CLASS
 * - Uses SeleniumCoverageIntegration to inject baggage headers into browser traffic.
 * - Reads -Dcoverage.browser.header.mode (off, proxy, cdp, auto).
 * - Reads optional -Dcoverage.baggage.header for an explicit baggage value.
 * - Wires coverage in createDriverWithCoverage(...) and applies per-scenario header behavior.
 */
public class LoginAndCalculatorSteps {

    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_CHROME_ARGS =
            "--headless=new,--disable-gpu,--window-size=1600,900,--no-sandbox,--disable-dev-shm-usage";

    private ChromeDriver driver;

    // Holds proxy-mode Parasoft coverage config and proxy lifecycle for this browser session.
    private SeleniumCoverageIntegration.ChromeCoverageConfig chromeCoverageConfig;

    private String baseUrl;

    // Controls where the Parasoft baggage header is injected: off, proxy, cdp, or auto.
    private String browserHeaderMode;

    // Optional command-line override for a fixed baggage header value.
    private String explicitBaggageHeader;

    private boolean usingProxyHeaderInjection;

    @Before(order = 0)
    public void setUpScenario() {
        baseUrl = normalizeBaseUrl(System.getProperty("app.base.url", "http://localhost:8080/app"));
        boolean acceptInsecureCerts = Boolean.parseBoolean(System.getProperty("app.accept.insecure.certs", "true"));

        // Command-line properties used by this test to control baggage header behavior:
        // -Dcoverage.browser.header.mode=cdp|proxy|auto|off
        // -Dcoverage.baggage.header=test-operator-id=jonnytest
        browserHeaderMode = System.getProperty("coverage.browser.header.mode", "auto")
                .toLowerCase(Locale.ROOT)
                .trim();
        explicitBaggageHeader = System.getProperty("coverage.baggage.header", "").trim();

        ChromeOptions options = createChromeOptions(acceptInsecureCerts);
        driver = createDriverWithCoverage(options, browserHeaderMode, explicitBaggageHeader, acceptInsecureCerts);
        driver.manage().timeouts().pageLoadTimeout(PAGE_TIMEOUT);
    }

    @After(order = 0)
    public void tearDownScenario() {
        if (driver != null) {
            driver.quit();
        }
        if (chromeCoverageConfig != null) {
            chromeCoverageConfig.close();
        }
    }

    @Given("the user opens the login page")
    public void openLoginPage() {
        applyCoverageHeaderForCurrentScenario();
        new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(System.getProperty("app.startup.timeout.seconds", "60"))))
            .ignoring(WebDriverException.class)
            .withMessage(() -> "Login form did not become ready at " + baseUrl + "/login; current URL=" + driver.getCurrentUrl())
            .until(browser -> {
                browser.get(baseUrl + "/login");
                return !browser.findElements(By.name("username")).isEmpty()
                    && !browser.findElements(By.name("password")).isEmpty();
            });
    }

    @When("the user logs in with configured credentials")
    public void loginWithConfiguredCredentials() {
        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(System.getProperty("app.username", "admin"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys(System.getProperty("app.password", "admin"));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();
    }

    @Then("the home page is displayed")
    public void assertHomePageDisplayed() {
        new WebDriverWait(driver, PAGE_TIMEOUT)
            .withMessage(() -> "Expected " + baseUrl + "/home after login; current URL=" + driver.getCurrentUrl() + ", title=" + driver.getTitle())
            .until(ExpectedConditions.and(ExpectedConditions.urlToBe(baseUrl + "/home"), ExpectedConditions.titleIs("App Home")));
    }

    @When("the user opens the calculator page")
    public void openCalculatorPage() {
        new WebDriverWait(driver, PAGE_TIMEOUT).until(ExpectedConditions.elementToBeClickable(By.linkText("Calculator"))).click();
    }

    @Then("the calculator page is displayed")
    public void assertCalculatorPageDisplayed() {
        new WebDriverWait(driver, PAGE_TIMEOUT)
                .until(ExpectedConditions.and(ExpectedConditions.urlToBe(baseUrl + "/calculator"), ExpectedConditions.titleIs("Calculator")));
        Assert.assertTrue(driver.findElement(By.name("a")).isDisplayed(), "Calculator form should be displayed.");
    }

    @When("the user adds two and three")
    public void calculateSum() {
        WebElement firstOperand = driver.findElement(By.name("a"));
        firstOperand.clear();
        firstOperand.sendKeys("2");
        WebElement secondOperand = driver.findElement(By.name("b"));
        secondOperand.clear();
        secondOperand.sendKeys("3");
        new Select(driver.findElement(By.name("op"))).selectByValue("add");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Then("the calculator result is five")
    public void assertCalculationResult() {
        new WebDriverWait(driver, PAGE_TIMEOUT).until(ExpectedConditions.textToBe(By.cssSelector(".result"), "Result: 5"));
        Assert.assertTrue(driver.getCurrentUrl().startsWith(baseUrl + "/calculator?"), "Calculation must remain in the configured application.");
    }

    @When("the user signs out")
    public void signOut() {
        new WebDriverWait(driver, PAGE_TIMEOUT).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".signed-in a"))).click();
    }

    @Then("the login page is displayed")
    public void assertLoginPageDisplayed() {
        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        wait.until(ExpectedConditions.urlToBe(baseUrl + "/login"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    // Parasoft-specific helper: chooses baggage-header injection strategy (off/proxy/cdp/auto).
    private ChromeDriver createDriverWithCoverage(
            ChromeOptions options,
            String mode,
            String baggageHeader,
            boolean acceptInsecureCerts) {
        switch (mode) {
            case "off":
                usingProxyHeaderInjection = false;
                return new ChromeDriver(options);
            case "proxy":
                // Proxy mode injects baggage headers through a local proxy wrapper.
                chromeCoverageConfig = SeleniumCoverageIntegration.configureProxyBaggageHeader(options);
                usingProxyHeaderInjection = true;
                return new ChromeDriver(chromeCoverageConfig.getChromeOptions());
            case "cdp":
                usingProxyHeaderInjection = false;
                return createDriverWithCdpCoverage(options, baggageHeader);
            case "auto":
            default:
                try {
                    usingProxyHeaderInjection = false;
                    return createDriverWithCdpCoverage(options, baggageHeader);
                } catch (RuntimeException cdpError) {
                    ChromeOptions proxyOptions = createChromeOptions(acceptInsecureCerts);
                    chromeCoverageConfig = SeleniumCoverageIntegration.configureProxyBaggageHeader(proxyOptions);
                    usingProxyHeaderInjection = true;
                    return new ChromeDriver(chromeCoverageConfig.getChromeOptions());
                }
        }
    }

    // Parasoft-specific helper: applies baggage header behavior before scenario browser activity.
    private void applyCoverageHeaderForCurrentScenario() {
        if (usingProxyHeaderInjection && chromeCoverageConfig != null && chromeCoverageConfig.getProxy() != null) {
            if (explicitBaggageHeader.isBlank()) {
                chromeCoverageConfig.getProxy().useCurrentTestBaggageHeader();
            } else {
                chromeCoverageConfig.getProxy().setBaggageHeader(explicitBaggageHeader);
            }
            return;
        }

        if ("off".equals(browserHeaderMode)) {
            return;
        }

        if (explicitBaggageHeader.isBlank()) {
            SeleniumCoverageIntegration.configureCdpBaggageHeader(driver);
        } else {
            SeleniumCoverageIntegration.configureCdpBaggageHeader(driver, explicitBaggageHeader);
        }
    }

    // Parasoft-specific helper: wires CDP header injection on a newly created driver.
    private ChromeDriver createDriverWithCdpCoverage(ChromeOptions options, String baggageHeader) {
        ChromeDriver cdpDriver = new ChromeDriver(options);
        try {
            if (baggageHeader.isBlank()) {
                SeleniumCoverageIntegration.configureCdpBaggageHeader(cdpDriver);
            } else {
                SeleniumCoverageIntegration.configureCdpBaggageHeader(cdpDriver, baggageHeader);
            }
            return cdpDriver;
        } catch (RuntimeException ex) {
            cdpDriver.quit();
            throw ex;
        }
    }

    static String normalizeBaseUrl(String configuredUrl) {
        URI configured = URI.create(configuredUrl.trim());
        if (!("http".equalsIgnoreCase(configured.getScheme()) || "https".equalsIgnoreCase(configured.getScheme()))
                || configured.getHost() == null || configured.getUserInfo() != null
                || configured.getRawQuery() != null || configured.getRawFragment() != null) {
            throw new IllegalArgumentException("app.base.url must be an HTTP(S) application URL without credentials, query, or fragment");
        }
        String normalized = configured.toString().replaceFirst("/+$", "");
        if (normalized.endsWith("/login")) {
            normalized = normalized.substring(0, normalized.length() - "/login".length());
        }
        return normalized;
    }

    private ChromeOptions createChromeOptions(boolean acceptInsecureCerts) {
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(acceptInsecureCerts);

        String argsProperty = System.getProperty("chrome.args", DEFAULT_CHROME_ARGS);
        for (String arg : argsProperty.split(",")) {
            String trimmed = arg.trim();
            if (!trimmed.isEmpty()) {
                options.addArguments(trimmed);
            }
        }

        return options;
    }
}
