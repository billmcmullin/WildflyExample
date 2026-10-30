package com.app.selenium;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
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
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            driver = null;
            if (chromeCoverageConfig != null) {
                try {
                    chromeCoverageConfig.close();
                } finally {
                    chromeCoverageConfig = null;
                }
            }
        }
    }

    private void openAuthenticatedHome() {
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
    }

    @Test
    public void loginAndOpenCalculatorFromHome() {
        openAuthenticatedHome();
        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
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

    @Test
    public void homeDashboardContainsEveryNavigationAndQuickLink() {
        openAuthenticatedHome();
        Map<String, String> navigation = Map.of("Calculator", "/calculator", "Number Guess", "/numberguess",
                "Snake", "/snake.html", "Calendar", "/calendar", "Swagger API", "/swagger-ui.html", "Manage Users", "/admin/users");
        WebElement menu = driver.findElement(By.cssSelector(".page-content nav"));
        Assert.assertEquals(menu.findElements(By.tagName("a")).size(), navigation.size(), "Update dashboard coverage when links are added.");
        navigation.forEach((label, path) -> assertHomeLink(menu, label, path));
        WebElement quickLinks = driver.findElement(By.cssSelector(".page-content section"));
        Map<String, String> quick = Map.of("Calculator", "/calculator", "Number Guess", "/numberguess",
                "Snake", "/snake.html", "Calendar", "/calendar", "Raw Calculator API (GET example)", "/api/calc");
        Assert.assertEquals(quickLinks.findElements(By.tagName("a")).size(), quick.size());
        quick.forEach((label, path) -> assertHomeLink(quickLinks, label, path));
    }

    private void assertHomeLink(WebElement container, String label, String path) {
        WebElement link = container.findElement(By.linkText(label));
        Assert.assertTrue(link.isDisplayed(), "Expected visible dashboard link: " + label + "; use ADMIN credentials for full dashboard coverage.");
        Assert.assertEquals(link.getAttribute("href"), baseUrl + path, "Dashboard links must stay in the configured application.");
    }

    @DataProvider
    public Object[][] quickLinks() {
        return new Object[][] {{"Calculator", "/calculator", "Calculator"}, {"Number Guess", "/numberguess", "Number Guess"},
                {"Snake", "/snake.html", "Snake"}, {"Calendar", "/calendar", "Calendar"},
                {"Raw Calculator API (GET example)", "/api/calc", ""}};
    }

    @Test(dataProvider = "quickLinks")
    public void quickLinkOpensItsFeatureAndReturnsHome(String label, String path, String title) {
        openAuthenticatedHome();
        driver.findElement(By.cssSelector(".page-content section")).findElement(By.linkText(label)).click();
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + path);
        if (path.equals("/api/calc")) {
            Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("Missing 'op' parameter"));
            driver.navigate().back();
        } else {
            Assert.assertEquals(driver.getTitle(), title);
            driver.findElement(By.linkText("Home")).click();
        }
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/home");
        Assert.assertEquals(driver.getTitle(), "App Home");
    }

    @DataProvider
    public Object[][] calculations() {
        return new Object[][] {{"2.5", "3.25", "add", "Result: 5.75"}, {"2", "5", "sub", "Result: -3"},
                {"-4", "3", "mul", "Result: -12"}, {"7", "2", "div", "Result: 3.5"},
                {"7", "0", "div", "Error: Division by zero"}, {"abc", "3", "add", "Error: Invalid number format"}};
    }

    @Test(dataProvider = "calculations")
    public void calculatorOperationsAndValidation(String first, String second, String operation, String expected) {
        openAuthenticatedHome();
        driver.findElement(By.linkText("Calculator")).click();
        fill("a", first);
        fill("b", second);
        new Select(driver.findElement(By.name("op"))).selectByValue(operation);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        new WebDriverWait(driver, PAGE_TIMEOUT).until(ExpectedConditions.textToBe(By.cssSelector(".result"), expected));
        Assert.assertEquals(driver.findElement(By.name("a")).getAttribute("value"), first);
        Assert.assertEquals(driver.findElement(By.name("b")).getAttribute("value"), second);
        Assert.assertEquals(new Select(driver.findElement(By.name("op"))).getFirstSelectedOption().getAttribute("value"), operation);
    }

    @Test
    public void snakeRendersAndSupportsStartPauseResumeAndRestart() {
        openAuthenticatedHome();
        driver.findElement(By.linkText("Snake")).click();
        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("snake-toggle")));
        WebElement canvas = driver.findElement(By.cssSelector("#snake-board canvas"));
        Assert.assertEquals(canvas.getAttribute("width"), "480");
        Assert.assertEquals(canvas.getAttribute("height"), "480");
        Assert.assertEquals(driver.findElement(By.id("snake-status")).getText(), "Ready");
        Assert.assertEquals(driver.findElements(By.cssSelector("button[data-direction]")).size(), 4);
        new Select(driver.findElement(By.id("snake-speed"))).selectByValue("200");
        driver.findElement(By.id("snake-toggle")).click();
        wait.until(ExpectedConditions.textToBe(By.id("snake-status"), "Playing"));
        driver.findElement(By.id("snake-toggle")).click();
        wait.until(ExpectedConditions.textToBe(By.id("snake-status"), "Paused"));
        Assert.assertEquals(driver.findElement(By.id("snake-toggle")).getText(), "Resume");
        driver.findElement(By.id("snake-toggle")).click();
        wait.until(ExpectedConditions.textToBe(By.id("snake-status"), "Playing"));
        driver.findElement(By.id("snake-restart")).click();
        Assert.assertEquals(driver.findElement(By.id("snake-score")).getText(), "0");
        wait.until(ExpectedConditions.textToBe(By.id("snake-status"), "Playing"));
    }

    @Test
    public void swaggerLoadsAndCalculatorApiReturnsSuccessAndValidationErrors() {
        openAuthenticatedHome();
        driver.findElement(By.linkText("Swagger API")).click();
        new WebDriverWait(driver, PAGE_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#swagger-ui .opblock-summary")));
        Assert.assertTrue(driver.findElement(By.id("swagger-ui")).getText().contains("/api/calc"));
        Map<?, ?> success = browserGetJson("/api/calc?a=2&b=3&op=add", 200);
        Assert.assertEquals(success.get("success"), Boolean.TRUE);
        Assert.assertEquals(success.get("result"), "5");
        Map<?, ?> error = browserGetJson("/api/calc?a=2&b=0&op=div", 400);
        Assert.assertEquals(error.get("success"), Boolean.FALSE);
        Assert.assertEquals(error.get("error"), "Division by zero");
        driver.findElement(By.linkText("Home")).click();
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/home");
    }

    @Test
    public void adminRequiresFieldsAndRejectsDuplicateUser() {
        openAuthenticatedHome();
        driver.findElement(By.linkText("Manage Users")).click();
        Assert.assertEquals(driver.getTitle(), "Create User");
        Select roles = new Select(driver.findElement(By.name("role")));
        Assert.assertEquals(roles.getOptions().stream().map(WebElement::getText).toList(), List.of("USER", "ADMIN"));
        clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
        Assert.assertEquals(driver.findElement(By.cssSelector(".result")).getText(), "Username and password are required.");
        fill("username", System.getProperty("app.username", "admin"));
        fill("password", "duplicate-user-must-not-be-created");
        clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
        Assert.assertEquals(driver.getTitle(), "Create User", "Duplicate usernames must show validation, not an HTTP 500 error page.");
        Assert.assertEquals(driver.findElement(By.cssSelector(".result")).getText(), "Failed to create user (maybe already exists).");
        driver.findElement(By.linkText("Back to Home")).click();
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/home");
    }

    @Test
    public void numberGuessValidatesInputAndResetsGame() {
        openAuthenticatedHome();
        driver.findElement(By.linkText("Number Guess")).click();
        for (String[] invalid : new String[][] {{"", "Please enter a number."}, {"abc", "Invalid number format."},
                {"0", "Guess must be between 1 and 100."}, {"101", "Guess must be between 1 and 100."}}) {
            fill("guess", invalid[0]);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/numberguess", "Guess submission must remain inside app.base.url.");
            Assert.assertEquals(driver.findElement(By.cssSelector(".result")).getText(), invalid[1]);
            Assert.assertEquals(driver.findElement(By.cssSelector(".page-content > p strong")).getText(), "10");
        }
        fill("guess", "50");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/numberguess");
        Assert.assertEquals(driver.findElement(By.cssSelector(".page-content > p strong")).getText(), "9");
        Assert.assertTrue(driver.findElement(By.cssSelector(".history-text")).getText().contains("Guess 50"));
        driver.findElement(By.linkText("Start New Game")).click();
        Assert.assertEquals(driver.findElement(By.cssSelector(".page-content > p strong")).getText(), "10");
        Assert.assertEquals(driver.findElement(By.cssSelector(".history-text")).getText(), "");
        Assert.assertEquals(driver.findElement(By.name("guess")).getAttribute("value"), "");
    }

    @Test(groups = "calendar")
    public void calendarViewsAndMonthNavigation() {
        openAuthenticatedHome();
        clickAndWait(driver.findElement(By.linkText("Calendar")));
        clickAndWait(driver.findElement(By.cssSelector(".calendar-tabs")).findElement(By.linkText("Month")));
        Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-grid tbody td")).size(), 42);
        Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-grid [aria-current='date']")).size(), 1);
        String currentMonth = driver.findElement(By.id("calendar-month")).getText();
        clickAndWait(driver.findElement(By.cssSelector("a[aria-label='Next month']")));
        Assert.assertNotEquals(driver.findElement(By.id("calendar-month")).getText(), currentMonth);
        clickAndWait(driver.findElement(By.cssSelector("a[aria-label='Previous month']")));
        Assert.assertEquals(driver.findElement(By.id("calendar-month")).getText(), currentMonth);
        clickAndWait(driver.findElement(By.linkText("Today")));
        Assert.assertEquals(driver.findElement(By.id("calendar-month")).getText(), currentMonth);
        for (String tab : List.of("Agenda", "History", "Notifications", "Preferences", "Import", "Month")) {
            clickAndWait(driver.findElement(By.cssSelector(".calendar-tabs")).findElement(By.linkText(tab)));
            Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-tabs [aria-current='page']")).getText(), tab);
            Assert.assertEquals(driver.getTitle(), "Calendar");
        }
    }

    @DataProvider
    public Object[][] recurrenceModes() {
        return new Object[][] {{"NONE", 1}, {"DAILY", 3}};
    }

    @Test(dataProvider = "recurrenceModes", groups = "calendar")
    public void calendarEventCreateSearchEditExportDeleteAndHistory(String frequency, int occurrences) {
        openAuthenticatedHome();
        clickAndWait(driver.findElement(By.linkText("Calendar")));
        String title = "selenium-calendar-" + UUID.randomUUID();
        String updated = title + "-edited";
        try {
            clickAndWait(driver.findElement(By.linkText("New event")));
            fill("title", title);
            fill("description", "Browser regression <b>literal text</b>");
            driver.findElement(By.name("allDay")).click();
            new Select(driver.findElement(By.name("frequency"))).selectByValue(frequency);
            if (!frequency.equals("NONE")) {
                fill("count", Integer.toString(occurrences));
            }
            clickAndWait(driver.findElement(By.cssSelector("#event-editor button[type='submit']")));
            Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-message")).getText(), "Event saved");
            searchCalendar(title);
            Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-list tbody tr")).size(), occurrences);
            WebElement row = calendarRow(title);
            Assert.assertEquals(row.findElement(By.cssSelector(".calendar-description")).getDomProperty("textContent"), "Browser regression <b>literal text</b>");
            Assert.assertTrue(row.findElements(By.cssSelector(".calendar-description b")).isEmpty(), "Description must render as text.");
            clickAndWait(row.findElement(By.linkText(frequency.equals("NONE") ? "Edit" : "Edit series")));
            Assert.assertEquals(driver.findElement(By.name("title")).getAttribute("value"), title);
            fill("title", updated);
            clickAndWait(driver.findElement(By.cssSelector("#event-editor button[type='submit']")));
            searchCalendar(updated);
            Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-list tbody tr")).size(), occurrences);
            String exportUrl = calendarRow(updated).findElement(By.linkText("Export")).getAttribute("href");
            Object exported = driver.executeAsyncScript("""
                    const done = arguments[arguments.length - 1];
                    fetch(arguments[0]).then(async response => done({status: response.status, body: await response.text()}))
                        .catch(error => done({error: String(error)}));
                    """, exportUrl);
            Assert.assertTrue(exported instanceof Map);
            Map<?, ?> calendar = (Map<?, ?>) exported;
            Assert.assertEquals(calendar.get("status"), 200L);
            String text = String.valueOf(calendar.get("body"));
            Assert.assertTrue(text.contains("BEGIN:VCALENDAR") && text.contains("SUMMARY:" + updated));
            if (!frequency.equals("NONE")) {
                Assert.assertTrue(text.contains("RRULE:FREQ=DAILY;COUNT=3"));
            }
            deleteCalendarEvents(updated);
            searchCalendar(updated);
            Assert.assertTrue(driver.findElements(By.cssSelector(".calendar-list tbody tr")).isEmpty());
            clickAndWait(driver.findElement(By.cssSelector(".calendar-tabs")).findElement(By.linkText("History")));
            Assert.assertTrue(driver.findElements(By.cssSelector(".calendar-history summary")).stream()
                    .anyMatch(entry -> entry.getText().contains(updated) && entry.getText().contains("DELETE")));
        } finally {
            deleteCalendarEvents(title);
            deleteCalendarEvents(updated);
        }
    }

    @Test(groups = "calendar")
    public void calendarPreferencesPersistAndAreRestored() {
        openAuthenticatedHome();
        clickAndWait(driver.findElement(By.linkText("Calendar")));
        clickAndWait(driver.findElement(By.cssSelector(".calendar-tabs")).findElement(By.linkText("Preferences")));
        Map<String, String> original = Map.of("zoneId", selected("zoneId"), "weekStart", selected("weekStart"), "defaultView", selected("defaultView"));
        Map<String, String> changed = Map.of("zoneId", original.get("zoneId").equals("UTC") ? "Europe/Paris" : "UTC",
                "weekStart", original.get("weekStart").equals("1") ? "7" : "1",
                "defaultView", original.get("defaultView").equals("month") ? "agenda" : "month");
        try {
            savePreferences(changed);
            driver.navigate().refresh();
            changed.forEach((name, value) -> Assert.assertEquals(selected(name), value));
            driver.get(baseUrl + "/calendar");
            Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-tabs [aria-current='page']")).getText(),
                    changed.get("defaultView").equals("month") ? "Month" : "Agenda");
        } finally {
            driver.get(baseUrl + "/calendar?view=settings");
            savePreferences(original);
            original.forEach((name, value) -> Assert.assertEquals(selected(name), value, "Calendar preferences must be restored."));
        }
    }

    @Test(groups = "calendar")
    public void calendarImportPreviewCanBeCancelledWithoutCreatingEvents() throws IOException {
        openAuthenticatedHome();
        clickAndWait(driver.findElement(By.linkText("Calendar")));
        LocalDate date = LocalDate.parse(driver.findElement(By.cssSelector(".calendar-date time")).getAttribute("datetime"));
        String title = "selenium-import-" + UUID.randomUUID();
        Path upload = Files.createTempFile("calendar-browser-", ".ics");
        try {
            Files.writeString(upload, "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Browser Tests//EN\r\nBEGIN:VEVENT\r\nUID:" + UUID.randomUUID()
                    + "\r\nDTSTAMP:20260101T000000Z\r\nDTSTART;VALUE=DATE:" + date.format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "\r\nDTEND;VALUE=DATE:" + date.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "\r\nSUMMARY:" + title + "\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n");
            clickAndWait(driver.findElement(By.cssSelector(".calendar-tabs")).findElement(By.linkText("Import")));
            driver.findElement(By.name("file")).sendKeys(upload.toAbsolutePath().toString());
            clickAndWait(driver.findElement(By.cssSelector("form[enctype='multipart/form-data'] button")));
            String preview = driver.findElement(By.cssSelector("main section")).getDomProperty("textContent");
            Assert.assertTrue(preview.contains(title), "Expected import preview for " + title + ": " + preview);
            clickAndWait(driver.findElement(By.xpath("//form[input[@name='action' and @value='cancelImport']]//button")));
            Assert.assertTrue(driver.findElements(By.name("previewId")).isEmpty());
            searchCalendar(title);
            Assert.assertTrue(driver.findElements(By.cssSelector(".calendar-list tbody tr")).isEmpty());
        } finally {
            Files.deleteIfExists(upload);
            deleteCalendarEvents(title);
        }
    }

    private String selected(String name) {
        return new Select(driver.findElement(By.name(name))).getFirstSelectedOption().getAttribute("value");
    }

    private void savePreferences(Map<String, String> preferences) {
        preferences.forEach((name, value) -> new Select(driver.findElement(By.name(name))).selectByValue(value));
        clickAndWait(driver.findElement(By.xpath("//form[input[@name='action' and @value='preferences']]//button")));
        Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-message")).getText(), "Preferences saved");
    }

    private void searchCalendar(String title) {
        driver.get(baseUrl + "/calendar?view=agenda&q=" + URLEncoder.encode(title, StandardCharsets.UTF_8));
        LocalDate date = LocalDate.parse(driver.findElement(By.cssSelector(".calendar-date time")).getAttribute("datetime"));
        fill("from", date.toString());
        fill("to", date.plusDays(14).toString());
        clickAndWait(driver.findElement(By.cssSelector(".calendar-search button")));
    }

    private WebElement calendarRow(String title) {
        return driver.findElements(By.cssSelector(".calendar-list tbody tr")).stream()
            .filter(row -> row.findElement(By.tagName("strong")).getDomProperty("textContent").equals(title)).findFirst()
                .orElseThrow(() -> new AssertionError("Expected calendar event: " + title));
    }

    private void deleteCalendarEvents(String title) {
        searchCalendar(title);
        List<WebElement> matching = driver.findElements(By.cssSelector(".calendar-list tbody tr")).stream()
            .filter(row -> row.findElement(By.tagName("strong")).getDomProperty("textContent").equals(title)).toList();
        if (!matching.isEmpty()) {
            WebElement row = matching.get(0);
            row.findElement(By.tagName("summary")).click();
            List<WebElement> scopes = row.findElements(By.name("scope"));
            if (!scopes.isEmpty()) {
                new Select(scopes.get(0)).selectByValue("series");
            }
            row.findElement(By.name("confirm")).click();
            clickAndWait(row.findElement(By.cssSelector("button[type='submit']")));
            Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-message")).getText(), "Event deleted");
        }
    }

    private void clickAndWait(WebElement element) {
        WebElement document = driver.findElement(By.tagName("html"));
        element.click();
        new WebDriverWait(driver, PAGE_TIMEOUT).until(browser -> !document.equals(browser.findElement(By.tagName("html"))));
        new WebDriverWait(driver, PAGE_TIMEOUT).until(browser -> "complete".equals(driver.executeScript("return document.readyState")));
    }

    private void fill(String name, String value) {
        WebElement input = driver.findElement(By.name(name));
        if (input.getAttribute("type").equals("date")) {
            driver.executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input, value);
            return;
        }
        input.clear();
        input.sendKeys(value);
    }

    private Map<?, ?> browserGetJson(String path, int expectedStatus) {
        Object response = driver.executeAsyncScript("""
                const done = arguments[arguments.length - 1];
                fetch(arguments[0], {credentials: 'same-origin'})
                    .then(async response => done({status: response.status,
                        contentType: response.headers.get('content-type'), body: await response.json()}))
                    .catch(error => done({error: String(error)}));
                """, baseUrl + path);
        Assert.assertTrue(response instanceof Map, "Expected browser JSON response: " + response);
        Map<?, ?> result = (Map<?, ?>) response;
        Assert.assertEquals(result.get("status"), Long.valueOf(expectedStatus), "Unexpected API response: " + result);
        Assert.assertTrue(String.valueOf(result.get("contentType")).contains("application/json"));
        Assert.assertTrue(result.get("body") instanceof Map);
        return (Map<?, ?>) result.get("body");
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
