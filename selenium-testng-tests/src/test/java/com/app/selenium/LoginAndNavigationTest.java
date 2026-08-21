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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.parasoft.coverage.integration.selenium.SeleniumCoverageIntegration;

public class LoginAndNavigationTest {

    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(10);
    private static final SSLSocketFactory TRUST_ALL_SOCKET_FACTORY = createTrustAllSocketFactory();
    private static final HostnameVerifier TRUST_ALL_HOSTNAME_VERIFIER = (hostname, session) -> true;
        private static final String DEFAULT_CHROME_ARGS =
            "--headless=new,--disable-gpu,--window-size=1600,900,--no-sandbox,--disable-dev-shm-usage";

    private ChromeDriver driver;
        private SeleniumCoverageIntegration.ChromeCoverageConfig chromeCoverageConfig;
    private String baseUrl;

    @BeforeClass
    public void setUp() {
        baseUrl = System.getProperty("app.base.url", "http://localhost:8080/app");
        boolean acceptInsecureCerts = Boolean.parseBoolean(System.getProperty("app.accept.insecure.certs", "true"));
        boolean skipReachabilityCheck = Boolean.parseBoolean(System.getProperty("app.skip.reachability.check", "false"));

        if (!skipReachabilityCheck && !isReachable(baseUrl + "/login", acceptInsecureCerts)) {
            throw new SkipException("Application endpoint is not reachable: " + baseUrl
                    + ". Start WildFly with the app deployed, then rerun tests. "
                    + "If this endpoint uses a self-signed certificate, keep app.accept.insecure.certs=true "
                    + "or bypass this check with -Dapp.skip.reachability.check=true.");
        }

        try {
            String browserHeaderMode = System.getProperty("coverage.browser.header.mode", "auto")
                    .toLowerCase(Locale.ROOT)
                    .trim();
            String explicitBaggageHeader = System.getProperty("coverage.baggage.header", "").trim();

            ChromeOptions options = createChromeOptions(acceptInsecureCerts);
            driver = createDriverWithCoverage(options, browserHeaderMode, explicitBaggageHeader, acceptInsecureCerts);
            driver.manage().timeouts().pageLoadTimeout(PAGE_TIMEOUT);
        } catch (Exception ex) {
            throw new SkipException("Unable to start Chrome browser for Selenium test: " + ex.getMessage(), ex);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (chromeCoverageConfig != null) {
            chromeCoverageConfig.close();
        }
    }

    @Test
    public void loginAndOpenCalculatorFromHome() {
        driver.get(baseUrl + "/login");

        WebElement username = driver.findElement(By.name("username"));
        WebElement password = driver.findElement(By.name("password"));
        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys(System.getProperty("app.username", "admin"));
        password.sendKeys(System.getProperty("app.password", "admin"));
        submit.click();

        String titleAfterLogin = driver.getTitle();
        if (!"App Home".equals(titleAfterLogin)) {
            String currentUrl = driver.getCurrentUrl();
            List<WebElement> messages = driver.findElements(By.cssSelector(".result"));
            String loginMessage = messages.isEmpty() ? "(none)" : messages.get(0).getText();
            Assert.fail("Login should redirect to the home page, but stayed on title='" + titleAfterLogin
                + "', url='" + currentUrl + "', message='" + loginMessage
                + "'. Verify app.base.url points to this sample app and credentials are valid.");
        }

        driver.findElement(By.linkText("Calculator")).click();
        Assert.assertEquals(driver.getTitle(), "Calculator", "Home link should navigate to calculator page.");

        Assert.assertTrue(driver.getPageSource().contains("Calculator"), "Calculator page should render content.");
    }

    private boolean isReachable(String url, boolean acceptInsecureCerts) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            if (acceptInsecureCerts && connection instanceof HttpsURLConnection httpsConnection) {
                httpsConnection.setSSLSocketFactory(TRUST_ALL_SOCKET_FACTORY);
                httpsConnection.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
            }
            connection.setRequestMethod("GET");
            connection.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
            connection.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
            return connection.getResponseCode() < 500;
        } catch (Exception ex) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ChromeDriver createDriverWithCoverage(
            ChromeOptions options,
            String browserHeaderMode,
            String explicitBaggageHeader,
            boolean acceptInsecureCerts) {
        switch (browserHeaderMode) {
            case "off":
                return new ChromeDriver(options);
            case "proxy":
                chromeCoverageConfig = SeleniumCoverageIntegration.configureProxyBaggageHeader(options);
                return new ChromeDriver(chromeCoverageConfig.getChromeOptions());
            case "cdp":
                return createDriverWithCdpCoverage(options, explicitBaggageHeader);
            case "auto":
            default:
                try {
                    return createDriverWithCdpCoverage(options, explicitBaggageHeader);
                } catch (RuntimeException cdpError) {
                    ChromeOptions proxyOptions = createChromeOptions(acceptInsecureCerts);
                    chromeCoverageConfig = SeleniumCoverageIntegration.configureProxyBaggageHeader(proxyOptions);
                    return new ChromeDriver(chromeCoverageConfig.getChromeOptions());
                }
        }
    }

    private ChromeDriver createDriverWithCdpCoverage(ChromeOptions options, String explicitBaggageHeader) {
        ChromeDriver cdpDriver = new ChromeDriver(options);
        if (explicitBaggageHeader.isBlank()) {
            SeleniumCoverageIntegration.configureCdpBaggageHeader(cdpDriver);
        } else {
            SeleniumCoverageIntegration.configureCdpBaggageHeader(cdpDriver, explicitBaggageHeader);
        }
        return cdpDriver;
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
