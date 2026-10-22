package com.app.selenium;

import java.net.HttpURLConnection;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
//Parasoft Coverage Requirement
import com.parasoft.coverage.integration.selenium.SeleniumCoverageIntegration;

/*
 * Working Selenium + Parasoft coverage example for this repository.
 *
 * PARASOFT COVERAGE PARTS IN THIS CLASS
 * - Uses SeleniumCoverageIntegration to inject a baggage header used for coverage correlation.
 * - Reads -Dcoverage.browser.header.mode (off, proxy, cdp, auto).
 * - Reads optional -Dcoverage.baggage.header for an explicit baggage value.
 * - Sets up coverage wiring in createDriverWithCoverage(...) and createDriverWithCdpCoverage(...).
 * - Applies per-test baggage header behavior in applyCoverageHeaderForCurrentTest().
 * - Cleans up proxy coverage resources in tearDown() when proxy mode is used.
 *
 * NORMAL TEST PARTS (NOT PARASOFT-SPECIFIC)
 * - App reachability checks, login assertions, and page navigation.
 * - Standard Chrome options and SSL reachability helpers for local/self-signed environments.
 */
public class LoginAndNavigationTest {

    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(30);
    private static final SSLSocketFactory TRUST_ALL_SOCKET_FACTORY = createTrustAllSocketFactory();
    private static final HostnameVerifier TRUST_ALL_HOSTNAME_VERIFIER = (hostname, session) -> true;
    private static final String DEFAULT_CHROME_ARGS =
            "--headless=new,--disable-gpu,--window-size=1600,900,--no-sandbox,--disable-dev-shm-usage";

    private ChromeDriver driver;
    // Holds proxy-mode Parasoft coverage config and proxy lifecycle for this browser session.
    private SeleniumCoverageIntegration.ChromeCoverageConfig chromeCoverageConfig;
    private String baseUrl;
    // Controls where the Parasoft baggage header is injected: off, proxy, cdp, or auto.
    private String browserHeaderMode;
    // Optional command-line override for a fixed baggage header value.
    // Example: -Dcoverage.baggage.header=test-operator-id=jonnytest
    // If blank, the coverage library generates/uses the current test baggage header.
    private String explicitBaggageHeader;
    private boolean usingProxyHeaderInjection;

    // Mixed setup: normal Selenium initialization plus Parasoft coverage mode/header configuration.
    private void setUp() {
        // Base application URL under test. Override for other environments with:
        // -Dapp.base.url=http://your-host:8080/app
        baseUrl = normalizeBaseUrl(System.getProperty("app.base.url", "http://localhost:8080/app"));
        boolean acceptInsecureCerts = Boolean.parseBoolean(System.getProperty("app.accept.insecure.certs", "true"));
        boolean skipReachabilityCheck = Boolean.parseBoolean(System.getProperty("app.skip.reachability.check", "false"));

        if (!skipReachabilityCheck) {
            new FluentWait<>(baseUrl + "/login")
                .withTimeout(startupTimeout())
                .pollingEvery(Duration.ofSeconds(1))
                .withMessage("Login endpoint did not become ready: " + baseUrl + "/login")
                .until(url -> isReachable(url, acceptInsecureCerts));
        }

        try {
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
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to start Chrome browser for Selenium test: " + ex.getMessage(), ex);
        }
    }

    // Mixed cleanup: normal driver shutdown plus Parasoft proxy coverage resource shutdown.
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (chromeCoverageConfig != null) {
            chromeCoverageConfig.close();
        }
    }

    // Main functional UI test; only the first line is Parasoft-specific baggage header application.
    @Test
    public void loginAndOpenCalculatorFromHome() {
        setUp();
        applyCoverageHeaderForCurrentTest();
        new WebDriverWait(driver, startupTimeout())
            .ignoring(WebDriverException.class)
            .withMessage(() -> "Login form did not become ready at " + baseUrl + "/login; current URL=" + driver.getCurrentUrl())
            .until(browser -> {
                browser.get(baseUrl + "/login");
                return !browser.findElements(By.name("username")).isEmpty()
                    && !browser.findElements(By.name("password")).isEmpty();
            });

        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        username.sendKeys(System.getProperty("app.username", "admin"));
        password.sendKeys(System.getProperty("app.password", "admin"));
        submit.click();

        try {
            wait.until(ExpectedConditions.and(ExpectedConditions.urlToBe(baseUrl + "/home"), ExpectedConditions.titleIs("App Home")));
        } catch (TimeoutException ex) {
            String currentUrl = driver.getCurrentUrl();
            List<WebElement> messages = driver.findElements(By.cssSelector(".result"));
            String loginMessage = messages.isEmpty() ? "(none)" : messages.get(0).getText();
            Assert.fail("Login should redirect to " + baseUrl + "/home, but stayed on title='" + driver.getTitle()
                + "', url='" + currentUrl + "', message='" + loginMessage
                + "'. Verify app.base.url points to this sample app and credentials are valid.", ex);
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Calculator"))).click();
        wait.until(ExpectedConditions.and(ExpectedConditions.urlToBe(baseUrl + "/calculator"), ExpectedConditions.titleIs("Calculator")));
        WebElement firstOperand = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("a")));
        firstOperand.clear();
        firstOperand.sendKeys("2");
        WebElement secondOperand = driver.findElement(By.name("b"));
        secondOperand.clear();
        secondOperand.sendKeys("3");
        new Select(driver.findElement(By.name("op"))).selectByValue("add");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.textToBe(By.cssSelector(".result"), "Result: 5"));
        Assert.assertTrue(driver.getCurrentUrl().startsWith(baseUrl + "/calculator?"), "Calculation must remain in the configured application.");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".signed-in a"))).click();
        wait.until(ExpectedConditions.urlToBe(baseUrl + "/login"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private static Duration startupTimeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty("app.startup.timeout.seconds", "60")));
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

    // Normal helper only: verifies endpoint reachability before tests start.
    private boolean isReachable(String url, boolean acceptInsecureCerts) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            if (acceptInsecureCerts && connection instanceof HttpsURLConnection httpsConnection) {
                httpsConnection.setSSLSocketFactory(TRUST_ALL_SOCKET_FACTORY);
                httpsConnection.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
            }
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
            connection.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception ex) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Parasoft-specific helper: chooses baggage-header injection strategy (off/proxy/cdp/auto).
    // Also sets whether per-test header updates should go through proxy or CDP.
    private ChromeDriver createDriverWithCoverage(
            ChromeOptions options,
            String browserHeaderMode,
            String explicitBaggageHeader,
            boolean acceptInsecureCerts) {
        switch (browserHeaderMode) {
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
                return createDriverWithCdpCoverage(options, explicitBaggageHeader);
            case "auto":
            default:
                try {
                    usingProxyHeaderInjection = false;
                    return createDriverWithCdpCoverage(options, explicitBaggageHeader);
                } catch (RuntimeException cdpError) {
                    ChromeOptions proxyOptions = createChromeOptions(acceptInsecureCerts);
                    chromeCoverageConfig = SeleniumCoverageIntegration.configureProxyBaggageHeader(proxyOptions);
                    usingProxyHeaderInjection = true;
                    return new ChromeDriver(chromeCoverageConfig.getChromeOptions());
                }
        }
    }

    // Parasoft-specific helper: applies the baggage header before each test action sequence.
    // If -Dcoverage.baggage.header is set, that exact value is used; otherwise test-scoped default is used.
    private void applyCoverageHeaderForCurrentTest() {
        if (usingProxyHeaderInjection && chromeCoverageConfig != null && chromeCoverageConfig.getProxy() != null) {
            if (explicitBaggageHeader.isBlank()) {
                // No explicit -Dcoverage.baggage.header provided: use test-scoped header value.
                chromeCoverageConfig.getProxy().useCurrentTestBaggageHeader();
            } else {
                // Explicit header from -Dcoverage.baggage.header is applied here.
                chromeCoverageConfig.getProxy().setBaggageHeader(explicitBaggageHeader);
            }
            return;
        }

        if ("off".equals(browserHeaderMode)) {
            return;
        }

        if (explicitBaggageHeader.isBlank()) {
            // CDP mode with auto-managed header value.
            SeleniumCoverageIntegration.configureCdpBaggageHeader(driver);
        } else {
            // CDP mode with explicit header from -Dcoverage.baggage.header.
            SeleniumCoverageIntegration.configureCdpBaggageHeader(driver, explicitBaggageHeader);
        }
    }

    // Parasoft-specific helper: wires CDP header injection on a newly created driver.
    private ChromeDriver createDriverWithCdpCoverage(ChromeOptions options, String explicitBaggageHeader) {
        ChromeDriver cdpDriver = new ChromeDriver(options);
        try {
            if (explicitBaggageHeader.isBlank()) {
                SeleniumCoverageIntegration.configureCdpBaggageHeader(cdpDriver);
            } else {
                SeleniumCoverageIntegration.configureCdpBaggageHeader(cdpDriver, explicitBaggageHeader);
            }
            return cdpDriver;
        } catch (RuntimeException ex) {
            cdpDriver.quit();
            throw ex;
        }
    }

    // Normal helper only: builds Chrome runtime options and optional command-line browser args.
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

    // Normal helper only: used for HTTPS reachability checks in test environments with self-signed certs.
    private static SSLSocketFactory createTrustAllSocketFactory() {
        try {
            TrustManager[] trustAll = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            // Trust all certs for test reachability checks.
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            // Trust all certs for test reachability checks.
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAll, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize SSL context for test reachability checks", ex);
        }
    }
}
