package com.app.selenium.cucumber.steps;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
    private final List<String> calendarTitles = new ArrayList<>();
    private String calendarTitle;

    @After(order = 1)
    public void cleanUpCalendarEvents() {
        for (String title : calendarTitles) {
            deleteCalendarEvent(title);
        }
    }

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
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            if (chromeCoverageConfig != null) {
                chromeCoverageConfig.close();
            }
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

    @Then("all dashboard navigation and Quick links are correct")
    public void assertDashboardLinks() {
        assertLinks(".page-content nav", Map.of("Calculator", "/calculator", "Number Guess", "/numberguess", "Snake", "/snake.html",
                "Calendar", "/calendar", "Swagger API", "/swagger-ui.html", "Manage Users", "/admin/users"));
        assertLinks(".page-content section", Map.of("Calculator", "/calculator", "Number Guess", "/numberguess", "Snake", "/snake.html",
                "Calendar", "/calendar", "Raw Calculator API (GET example)", "/api/calc"));
    }

    private void assertLinks(String selector, Map<String, String> expected) {
        WebElement container = driver.findElement(By.cssSelector(selector));
        Assert.assertEquals(container.findElements(By.tagName("a")).size(), expected.size());
        expected.forEach((label, path) -> {
            WebElement link = container.findElement(By.linkText(label));
            Assert.assertTrue(link.isDisplayed(), "Full dashboard coverage requires ADMIN credentials: " + label);
            Assert.assertEquals(link.getAttribute("href"), baseUrl + path);
        });
    }

    @When("the user opens {string} from dashboard navigation")
    public void openNavigation(String label) {
        clickAndWait(driver.findElement(By.cssSelector(".page-content nav")).findElement(By.linkText(label)));
    }

    @When("the user opens Quick link {string}")
    public void openQuickLink(String label) {
        clickAndWait(driver.findElement(By.cssSelector(".page-content section")).findElement(By.linkText(label)));
    }

    @Then("the application page {string} has title {string}")
    public void assertApplicationPage(String path, String title) {
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + path);
        Assert.assertEquals(driver.getTitle(), title);
    }

    @When("the user returns through the Home link")
    public void returnHome() {
        clickAndWait(driver.findElement(By.linkText("Home")));
    }

    @When("the user returns to the preceding page")
    public void returnToPrecedingPage() {
        driver.navigate().back();
    }

    @Then("the raw API reports its missing operation")
    public void assertMissingApiOperation() {
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/api/calc");
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().contains("Missing 'op' parameter"));
    }

    @When("the user calculates {string} and {string} using {string}")
    public void calculate(String first, String second, String operation) {
        fill("a", first);
        fill("b", second);
        new Select(driver.findElement(By.name("op"))).selectByValue(operation);
        clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
    }

    @Then("the form reports {string}")
    public void assertFormResult(String expected) {
        Assert.assertFalse(driver.findElements(By.cssSelector(".result")).isEmpty(),
                "Expected form validation, not title='" + driver.getTitle() + "' at " + driver.getCurrentUrl());
        Assert.assertEquals(driver.findElement(By.cssSelector(".result")).getText(), expected);
    }

    @When("the user exercises Snake start pause resume and restart")
    public void exerciseSnakeControls() {
        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("snake-toggle")));
        WebElement canvas = driver.findElement(By.cssSelector("#snake-board canvas"));
        Assert.assertTrue(canvas.isDisplayed());
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

    @Then("Swagger renders the calculator operations")
    public void assertSwaggerOperations() {
        new WebDriverWait(driver, PAGE_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#swagger-ui .opblock-summary")));
        Assert.assertTrue(driver.findElement(By.id("swagger-ui")).getText().contains("/api/calc"));
    }

    @Then("the calculator API returns success and rejects division by zero")
    public void assertCalculatorApi() {
        Map<?, ?> success = browserJson("/api/calc?a=2&b=3&op=add", 200);
        Assert.assertEquals(success.get("success"), Boolean.TRUE);
        Assert.assertEquals(success.get("result"), "5");
        Map<?, ?> error = browserJson("/api/calc?a=2&b=0&op=div", 400);
        Assert.assertEquals(error.get("success"), Boolean.FALSE);
        Assert.assertEquals(error.get("error"), "Division by zero");
    }

    private Map<?, ?> browserJson(String path, int expectedStatus) {
        Map<?, ?> response = browserFetch(baseUrl + path);
        Assert.assertEquals(response.get("status"), Long.valueOf(expectedStatus));
        Assert.assertTrue(String.valueOf(response.get("contentType")).contains("application/json"));
        Object body = driver.executeScript("return JSON.parse(arguments[0]);", response.get("body"));
        Assert.assertTrue(body instanceof Map);
        return (Map<?, ?>) body;
    }

    private Map<?, ?> browserFetch(String url) {
        Object response = driver.executeAsyncScript("""
                const done = arguments[arguments.length - 1];
                fetch(arguments[0], {credentials: 'same-origin'}).then(async response => done({status: response.status,
                    contentType: response.headers.get('content-type'), body: await response.text()}))
                    .catch(error => done({error: String(error)}));
                """, url);
        Assert.assertTrue(response instanceof Map, "Expected browser HTTP response: " + response);
        return (Map<?, ?>) response;
    }

    @When("the user submits the empty create-user form")
    public void submitEmptyUserForm() {
        Assert.assertEquals(new Select(driver.findElement(By.name("role"))).getOptions().stream().map(WebElement::getText).toList(), List.of("USER", "ADMIN"));
        clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
    }

    @When("the user attempts to create the already configured username")
    public void submitDuplicateUser() {
        fill("username", System.getProperty("app.username", "admin"));
        fill("password", "duplicate-user-must-not-be-created");
        clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
    }

    @When("the user submits invalid Number Guess inputs")
    public void submitInvalidGuesses() {
        for (String[] invalid : new String[][] {{"", "Please enter a number."}, {"abc", "Invalid number format."},
                {"0", "Guess must be between 1 and 100."}, {"101", "Guess must be between 1 and 100."}}) {
            fill("guess", invalid[0]);
            clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
            Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/numberguess", "Guess submission must remain inside app.base.url.");
            assertFormResult(invalid[1]);
            Assert.assertEquals(driver.findElement(By.cssSelector(".page-content > p strong")).getText(), "10");
        }
    }

    @When("the user guesses fifty and starts a new game")
    public void guessAndReset() {
        fill("guess", "50");
        clickAndWait(driver.findElement(By.cssSelector("button[type='submit']")));
        Assert.assertEquals(driver.getCurrentUrl(), baseUrl + "/numberguess");
        Assert.assertEquals(driver.findElement(By.cssSelector(".page-content > p strong")).getText(), "9");
        Assert.assertTrue(driver.findElement(By.cssSelector(".history-text")).getText().contains("Guess 50"));
        clickAndWait(driver.findElement(By.linkText("Start New Game")));
        Assert.assertEquals(driver.findElement(By.cssSelector(".page-content > p strong")).getText(), "10");
        Assert.assertEquals(driver.findElement(By.cssSelector(".history-text")).getText(), "");
        Assert.assertEquals(driver.findElement(By.name("guess")).getAttribute("value"), "");
    }

    @When("the user visits every calendar view and navigates months")
    public void visitCalendarViews() {
        clickCalendarTab("Month");
        Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-grid tbody td")).size(), 42);
        Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-grid [aria-current='date']")).size(), 1);
        String month = driver.findElement(By.id("calendar-month")).getText();
        clickAndWait(driver.findElement(By.cssSelector("a[aria-label='Next month']")));
        Assert.assertNotEquals(driver.findElement(By.id("calendar-month")).getText(), month);
        clickAndWait(driver.findElement(By.cssSelector("a[aria-label='Previous month']")));
        Assert.assertEquals(driver.findElement(By.id("calendar-month")).getText(), month);
        clickAndWait(driver.findElement(By.linkText("Today")));
        Assert.assertEquals(driver.findElement(By.id("calendar-month")).getText(), month);
        for (String view : List.of("Agenda", "History", "Notifications", "Preferences", "Import", "Month")) {
            clickCalendarTab(view);
            Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-tabs [aria-current='page']")).getText(), view);
        }
    }

    @When("the user creates a unique {string} calendar event with {int} occurrences")
    public void createCalendarEvent(String frequency, int occurrences) {
        calendarTitle = "cucumber-calendar-" + UUID.randomUUID();
        calendarTitles.add(calendarTitle);
        clickAndWait(driver.findElement(By.linkText("New event")));
        fill("title", calendarTitle);
        fill("description", "Browser regression <b>literal text</b>");
        driver.findElement(By.name("allDay")).click();
        new Select(driver.findElement(By.name("frequency"))).selectByValue(frequency);
        if (!frequency.equals("NONE")) {
            fill("count", Integer.toString(occurrences));
        }
        clickAndWait(driver.findElement(By.cssSelector("#event-editor button[type='submit']")));
        Assert.assertEquals(driver.findElement(By.cssSelector(".calendar-message")).getText(), "Event saved");
    }

    @Then("calendar search shows {int} occurrences of that event")
    public void assertCalendarOccurrences(int count) {
        searchCalendar(calendarTitle);
        Assert.assertEquals(driver.findElements(By.cssSelector(".calendar-list tbody tr")).size(), count);
        WebElement row = calendarRow(calendarTitle);
        Assert.assertEquals(row.findElement(By.cssSelector(".calendar-description")).getDomProperty("textContent"), "Browser regression <b>literal text</b>");
        Assert.assertTrue(row.findElements(By.cssSelector(".calendar-description b")).isEmpty());
    }

    @When("the user edits that {string} calendar event and exports it")
    public void editAndExportCalendarEvent(String frequency) {
        clickAndWait(calendarRow(calendarTitle).findElement(By.linkText(frequency.equals("NONE") ? "Edit" : "Edit series")));
        Assert.assertEquals(driver.findElement(By.name("title")).getAttribute("value"), calendarTitle);
        calendarTitle += "-edited";
        calendarTitles.add(calendarTitle);
        fill("title", calendarTitle);
        clickAndWait(driver.findElement(By.cssSelector("#event-editor button[type='submit']")));
        searchCalendar(calendarTitle);
        Map<?, ?> exported = browserFetch(calendarRow(calendarTitle).findElement(By.linkText("Export")).getAttribute("href"));
        Assert.assertEquals(exported.get("status"), 200L);
        String text = String.valueOf(exported.get("body"));
        Assert.assertTrue(text.contains("BEGIN:VCALENDAR") && text.contains("SUMMARY:" + calendarTitle));
        if (!frequency.equals("NONE")) {
            Assert.assertTrue(text.contains("RRULE:FREQ=DAILY;COUNT=3"));
        }
    }

    @When("the user deletes that event or its entire series")
    public void deleteScenarioCalendarEvent() {
        deleteCalendarEvent(calendarTitle);
    }

    @Then("that event is absent and its deletion is in calendar history")
    public void assertCalendarEventDeleted() {
        searchCalendar(calendarTitle);
        Assert.assertTrue(driver.findElements(By.cssSelector(".calendar-list tbody tr")).isEmpty());
        clickCalendarTab("History");
        Assert.assertTrue(driver.findElements(By.cssSelector(".calendar-history summary")).stream()
                .anyMatch(entry -> entry.getText().contains(calendarTitle) && entry.getText().contains("DELETE")));
    }

    private void clickCalendarTab(String label) {
        clickAndWait(driver.findElement(By.cssSelector(".calendar-tabs")).findElement(By.linkText(label)));
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

    private void deleteCalendarEvent(String title) {
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

    private void fill(String name, String value) {
        WebElement input = driver.findElement(By.name(name));
        if (input.getAttribute("type").equals("date")) {
            driver.executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input, value);
        } else {
            input.clear();
            input.sendKeys(value);
        }
    }

    private void clickAndWait(WebElement element) {
        WebElement document = driver.findElement(By.tagName("html"));
        element.click();
        new WebDriverWait(driver, PAGE_TIMEOUT).until(browser -> !document.equals(browser.findElement(By.tagName("html"))));
        new WebDriverWait(driver, PAGE_TIMEOUT).until(browser -> "complete".equals(driver.executeScript("return document.readyState")));
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
