# WildflyExample

Simple WildFly web application plus a dedicated Selenium Browser Test module using TestNG and Parasoft coverage integration.

The project now uses a multi-project Gradle build that mirrors the Maven parent/module structure.

## Modules

- `app`: WAR module for the WildFly-deployable application.
- `selenium-testng-tests`: Browser test module using Selenium + TestNG with Parasoft coverage listeners.
- `selenium-cucumber-testng-tests`: Separate browser-test example module using Selenium + Cucumber (running on TestNG) with Parasoft coverage integration.

## Prerequisites

- Java 21
- WildFly (for deploying the `app` WAR)

## Build

Build all modules:

Linux/macOS:

```sh
./gradlew clean verify
```

Linux/macOS (Maven wrapper):

```sh
./mvnw clean verify
```

Windows:

```powershell
gradlew.bat clean verify
```

Windows (Maven wrapper):

```powershell
mvnw.cmd clean verify
```

Build only the web app WAR:

Linux/macOS:

```sh
./gradlew :app:clean :app:package
```

Linux/macOS (Maven wrapper):

```sh
./mvnw -pl app clean package
```

Windows:

```powershell
gradlew.bat :app:clean :app:package
```

Windows (Maven wrapper):

```powershell
mvnw.cmd -pl app clean package
```

WAR output path:

- `app/build/libs/app.war`
- `app/target/app.war`

## Calendar

After signing in, select **Calendar** from the Home navigation button or Quick
links, or open `/app/calendar` with the appropriate deployment context. The page
opens to the current month and year, shows today's full date, and highlights
today in the user's preferred time zone. Use the previous/next controls, month
picker, or **Today** to navigate. Preferences persist the time zone, Monday or
Sunday week start, and default Month or Agenda view; the initial zone is the
application server's zone.

Calendar ownership is persisted in the `user_calendars` HSQLDB table, keyed by
the signed-in username with a foreign key to `users`. The table is created
automatically on startup for both existing and new databases. First access
creates the user's calendar without overwriting it on later visits. Ownership
uses the authenticated account rather than the temporary session ID, so it
survives sign-out and sign-in. Calendars are private, including for admin users;
sharing and email delivery are not included.
The month being viewed is a URL parameter, not a saved preference.

### Events, Search, and History

Create, inspect, edit, and delete events using the calendar and agenda. Events
have a title, description, dates/times, event time zone, all-day flag, optional
recurrence, and optional reminder. All-day end dates in the form are inclusive;
storage and iCalendar use an exclusive end date. Timed events retain the event's
wall-clock time and zone, while display converts to the viewer's preference.
Nonexistent daylight-saving gap times are rejected; ambiguous repeated times
use Java's earlier-offset convention. Invalid forms retain entered fields.

All mutations require a session-bound CSRF token. Event and notification IDs
are always checked against the authenticated account, never a submitted owner.
Version checks reject stale edits with HTTP 409. Creates, changes, deletions,
and imports write audit snapshots in the same transaction as the event change.
History is private, includes deleted events, and is retained indefinitely in
this version; it is displayed in pages of 50 entries.

Agenda searches match title/description case-insensitively and support inclusive
date ranges of at most 366 days. Results are sorted and paginated in groups of
25. Each account supports up to 1000 event series; a search is bounded to 10000
occurrences. Event durations must be positive and no longer than 366 days.

### Recurrence and Reminders

Daily, weekly, and monthly recurrence use iCal4j. Choose 1 to 999 occurrences or
an end date within five years. Monthly dates absent from a month are skipped,
not moved to its final day. Edit/delete one occurrence or the full series;
single-occurrence edits create a linked replacement and exclude the old
occurrence. Deleting the entire series also deletes its linked replacements.
Existing exclusions remain when a series is edited. Recurring times
in daylight-saving gaps are skipped.

Reminders are in-app only, from the event start up to seven days beforehand.
WildFly's `java:comp/DefaultManagedScheduledExecutorService` checks once per
minute; only the scheduled future is cancelled on shutdown, never the managed
executor. Calendar requests also refresh due reminders, including on Tomcat
where a managed scheduler is unavailable. Missed reminders recover for seven
days; older reminders are not generated. Notifications persist and a unique
event/occurrence key prevents duplicates. Marking one read remains effective
across later checks. This is not a guaranteed real-time delivery service.

### Import and Export

Export the whole calendar or one event as `.ics`. iCal4j writes escaped content,
timezone definitions, all-day values, supported recurrence, and exclusions.
Import uses a session-bound preview followed by explicit confirmation, with no
database writes during preview. Files are limited to 256 KiB and 200 events.
Existing UIDs are skipped rather than overwritten, and a confirmed import is
transactional. Uploads are deleted after parsing and external timezone updates
are disabled.

Edited occurrences export as separate VEVENTs plus EXDATE on the original
series. When imported into another account, those replacement VEVENTs are
independent events rather than linked series replacements.

The supported event properties are UID, dates, summary, description, recurrence,
EXDATE, standard timestamps/status/class/transparency, and application zone/
reminder extensions. Attendees, attachments, nested alarms, detached
RECURRENCE-ID overrides, arbitrary recurrence rules, and non-event calendar
components are rejected rather than silently dropped. Repeating timed exports
require UTC or an IANA zone, not a nonzero fixed-offset zone.

The existing database connection is reused. Keep the server's working-directory
`app-runtime/app-data` folder on persistent, writable storage across restarts and
deployments. The `database.directory` servlet context parameter can override
that directory. Startup fails if persistent storage or schema migration fails;
the application no longer silently falls back to an in-memory database.
Database errors while serving the calendar return HTTP 503.

### Database Migrations

Startup applies Flyway migrations from `app/src/main/resources/db/migration`
before publishing the datasource or seeding the default admin. Both Maven and
Gradle package the same migration resources and Flyway HSQLDB support module.

- V1 creates users and remember tokens if absent.
- V2 creates calendar ownership if absent.
- V3 adds events, preferences, recurrence exclusions, audit history, and notifications.
- V4 links edited occurrences to their series for cascading series deletion.
- Existing databases without migration history are adopted at baseline version
  0, so V1 and V2 still run without replacing existing rows or credentials.
- Applied versions and checksums are recorded in `flyway_schema_history`.
  Later startups validate history and apply only pending migrations.
- Add future changes as new `V5__Description.sql`, `V6__Description.sql`, etc.
  Do not edit previously applied migrations. Automatic cleanup is disabled.

Before the first migrated deployment, stop the server and back up the complete
database directory. HSQLDB schema changes are not fully transactional: after a
failed migration, investigate and restore the backup when necessary rather than
assuming every DDL statement was rolled back. Never delete or repair migration
history just to bypass a startup validation error.

## Snake

After signing in, select **Snake** from Home, or open `/app/snake.html` with the
appropriate deployment context. Steer with arrow keys, WASD, or the on-screen
direction buttons. Use Start/Pause/Resume, Restart, and the speed selector to
control the game. Space toggles play/pause when a form control is not focused.
Food adds 10 points and grows the snake; walls and self-collisions end the game.
The best score is stored in this browser, not in the server database.
Phaser is bundled in the WAR, so the game does not require a CDN connection.

## Swagger / OpenAPI

After deploying the rebuilt WAR, sign in to the application and select **Swagger API**
on the Home page. With the default deployment context:

- Swagger UI: `http://localhost:8080/app/swagger-ui.html`
- OpenAPI 3.0 JSON: `http://localhost:8080/app/openapi.json`

Replace the host, port, and `/app` context with your deployment values. Swagger UI
uses relative URLs and the browser's existing session cookie, so **Try it out**
calls the same application. Both the UI and specification retain the existing
login protection. No bearer token or manual cookie authorization is required.
Swagger UI assets are bundled in the WAR; no CDN or external validator is used.

Expand **Calculator**, select `GET /api/calc` or `POST /api/calc`, then use
**Try it out** and **Execute**. For example, `a=2`, `b=3`, `op=add` returns
`{"success":true,"result":"5"}`. POST supports JSON and form data.

Session, user administration, and number-guess form endpoints are also documented.
These return HTML or redirects rather than JSON; browsers follow redirects
automatically. User creation requires an ADMIN session and creates real accounts.
Logout invalidates the session, so sign in again before further API tests.

The contract is maintained in `app/src/main/webapp/openapi.json`; update it when
servlet request or response behavior changes. It is not generated from annotations.

## Maven to Gradle Command Mapping

- `mvn clean verify` -> `gradlew clean verify`
- `mvn clean package` -> `gradlew clean package`
- `mvn -pl app clean package` -> `gradlew :app:clean :app:package`
- `mvn -pl selenium-testng-tests verify -Prun-selenium-tests` -> `gradlew :selenium-testng-tests:verify -Prun-selenium-tests`
- `mvn -pl selenium-cucumber-testng-tests verify -Prun-cucumber-selenium-tests` -> `gradlew :selenium-cucumber-testng-tests:verify -Prun-cucumber-selenium-tests`

## Migration Notes (Maven + Gradle Side-by-Side)

For now, this repository intentionally keeps both build systems:

- Maven files/wrappers remain available (`pom.xml`, `mvnw`, `mvnw.cmd`) for existing CI jobs and local workflows.
- Gradle files/wrapper are added (`settings.gradle`, `build.gradle`, `gradlew`, `gradlew.bat`) for incremental adoption.

Team guidance:

- Prefer Gradle for new local scripts and updated automation.
- Keep Maven commands usable until all CI/CD and team workflows have been migrated.
- When changing dependencies or build behavior, keep Maven and Gradle definitions aligned.

Behavior parity notes:

- Root/module `verify` and `package` aliases are provided in Gradle to match Maven lifecycle naming.
- Selenium tests are skipped by default (equivalent to `selenium.tests.skip=true` in Maven).
- Setting `-Prun-selenium-tests` enables Selenium execution during `verify` for the `selenium-testng-tests` module.
- Cucumber Selenium tests are also skipped by default in the separate module (`selenium.cucumber.tests.skip=true`).
- Setting `-Prun-cucumber-selenium-tests` enables execution during `verify` for `selenium-cucumber-testng-tests`.
- Custom `-D` flags for `app.*`, `coverage.*`, `parasoft.*`, `org.slf4j.*`, and `chrome.args` are forwarded to the Selenium test JVM.

## Selenium Target and Readiness

Set `-Dapp.base.url=http://wildfly:8080/app` to the application root, including
its deployment context. Both suites also normalize a trailing slash or a
supplied `/login` suffix, so `/app/login/login` is never constructed.
Use a hostname reachable from the test agent, not necessarily your browser.

Both suites wait for the login form, check the exact Home and Calculator URLs,
submit `2 + 3`, verify `Result: 5`, and check sign-out returns to the configured
login URL. Startup and browser failures fail the test rather than silently skip it.
The startup wait defaults to 60 seconds and can be changed with
`-Dapp.startup.timeout.seconds=120`; page transitions wait up to 30 seconds.

For CI validation, omit `-Dmaven.test.failure.ignore=true` and
`-Dmaven.test.error.ignore=true`. A successful Maven build should not hide
browser-test failures. Existing CTP lifecycle listeners and baggage headers remain
enabled by the browser suites.

## Run Selenium + Cucumber (TestNG) + Parasoft Coverage

This is provided as a separate example module: `selenium-cucumber-testng-tests`.

Linux/macOS (`gradlew`):

```sh
./gradlew :selenium-cucumber-testng-tests:verify -Prun-cucumber-selenium-tests \
  -Dapp.base.url=http://wildfly:8080/app \
  -Dapp.username=admin \
  -Dapp.password=admin \
  -Dcoverage.browser.header.mode=cdp \
  -Dcoverage.baggage.header=test-operator-id=jonnytest \
  -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Windows (`gradlew.bat`):

```bat
gradlew.bat :selenium-cucumber-testng-tests:verify -Prun-cucumber-selenium-tests -Dapp.base.url=http://wildfly:8080/app -Dapp.username=admin -Dapp.password=admin -Dcoverage.browser.header.mode=cdp -Dcoverage.baggage.header=test-operator-id=jonnytest -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Linux/macOS (`mvnw`):

```sh
./mvnw -pl selenium-cucumber-testng-tests verify -Prun-cucumber-selenium-tests \
  -Dapp.base.url=http://wildfly:8080/app \
  -Dapp.username=admin \
  -Dapp.password=admin \
  -Dcoverage.browser.header.mode=cdp \
  -Dcoverage.baggage.header=test-operator-id=jonnytest \
  -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Windows (`mvnw.cmd`):

```bat
mvnw.cmd -pl selenium-cucumber-testng-tests verify -Prun-cucumber-selenium-tests -Dapp.base.url=http://wildfly:8080/app -Dapp.username=admin -Dapp.password=admin -Dcoverage.browser.header.mode=cdp -Dcoverage.baggage.header=test-operator-id=jonnytest -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

## Run Selenium + Parasoft Coverage

1. Deploy `app/build/libs/app.war` to WildFly.
2. Ensure the app is reachable at `http://localhost:8080/app` (or override `app.base.url`).
3. Update `selenium-testng-tests/src/test/resources/coverage-integration.properties` with your CTP URL, credentials, and environment ID.

### Selenium Coverage Run Example (verify with profile-equivalent flag)

Linux/macOS (`gradlew`):

```sh
./gradlew :selenium-testng-tests:verify -Prun-selenium-tests \
  -Dapp.base.url=http://wildfly:8080/app \
  -Dapp.username=admin \
  -Dapp.password=admin \
  -Dcoverage.browser.header.mode=cdp \
  -Dcoverage.baggage.header=test-operator-id=jonnytest \
  -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Windows (`gradlew.bat` wrapper):

```bat
gradlew.bat :selenium-testng-tests:verify -Prun-selenium-tests -Dapp.base.url=http://wildfly:8080/app -Dapp.username=admin -Dapp.password=admin -Dcoverage.browser.header.mode=cdp -Dcoverage.baggage.header=test-operator-id=jonnytest -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Linux/macOS (`mvnw` wrapper):

```sh
./mvnw -pl selenium-testng-tests verify -Prun-selenium-tests \
  -Dapp.base.url=http://wildfly:8080/app \
  -Dapp.username=admin \
  -Dapp.password=admin \
  -Dcoverage.browser.header.mode=cdp \
  -Dcoverage.baggage.header=test-operator-id=jonnytest \
  -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Windows (`mvnw.cmd` wrapper):

```bat
mvnw.cmd -pl selenium-testng-tests verify -Prun-selenium-tests -Dapp.base.url=http://wildfly:8080/app -Dapp.username=admin -Dapp.password=admin -Dcoverage.browser.header.mode=cdp -Dcoverage.baggage.header=test-operator-id=jonnytest -Dorg.slf4j.simpleLogger.log.com.parasoft.coverage.integration=debug
```

Alternative explicit task (runs browser tests directly):

- `gradlew.bat :selenium-testng-tests:runSeleniumTests ...`

### Native vs Custom Flags

Native Parasoft Coverage Integration properties (supported by the libraries directly):

- `parasoft.coverage.integration.ctp.url`
- `parasoft.coverage.integration.ctp.envId`
- `parasoft.coverage.integration.ctp.userId`
- `parasoft.coverage.integration.dtp.sessionTag`
- `parasoft.coverage.integration.parallel.test.enabled`
- `parasoft.coverage.integration.ctp.auth.username`
- `parasoft.coverage.integration.ctp.auth.password`
- `parasoft.coverage.integration.ctp.auth.token`

Project-specific flags in this repository (implemented in `LoginAndNavigationTest`):

- `coverage.browser.header.mode` (`cdp`, `proxy`, `auto`, `off`)
- `coverage.baggage.header` (explicit baggage header value)
- `chrome.args` (comma-separated Chrome options)

Working reference implementation:

- `selenium-testng-tests/src/test/java/com/app/selenium/LoginAndNavigationTest.java`
- This class shows exactly where `coverage.browser.header.mode` and `coverage.baggage.header` are read from command-line `-D` properties and where the baggage header is applied in both CDP and proxy modes.
- This file is the working code example to follow when adding Parasoft baggage-header support to other Selenium tests.

### Add Similar Flags in Your Own Tests

If you want other projects to use the same command-line pattern, implement these steps in the Selenium test code:

1. Read custom JVM properties with `System.getProperty(...)`.
2. Build browser options from `chrome.args` (or defaults).
3. Switch header mode (`cdp` or `proxy`) and call the matching Parasoft API.
4. If needed, apply explicit baggage header via `configureCdpBaggageHeader(driver, baggage)`.
5. Keep native Parasoft settings in `coverage-integration.properties`.

Minimal example:

```java
String mode = System.getProperty("coverage.browser.header.mode", "auto");
String baggage = System.getProperty("coverage.baggage.header", "").trim();

ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new", "--disable-gpu", "--window-size=1600,900");

ChromeDriver driver = new ChromeDriver(options);
if ("cdp".equalsIgnoreCase(mode)) {
    if (baggage.isBlank()) {
        SeleniumCoverageIntegration.configureCdpBaggageHeader(driver);
    } else {
        SeleniumCoverageIntegration.configureCdpBaggageHeader(driver, baggage);
    }
}
```

Optional test flags:

- `-Dapp.accept.insecure.certs=true`
- `-Dapp.startup.timeout.seconds=120`
- `-Dapp.skip.reachability.check=true` (TestNG HTTP preflight only; login-form checks still run)
- `-Dchrome.args=--headless=new,--disable-gpu,--window-size=1600,900,--no-sandbox,--disable-dev-shm-usage`

### Troubleshooting

- If PowerShell parses `-D` values unexpectedly, quote the argument (example: `-D"app.base.url=https://your-host/app"`).
- `Login endpoint did not become ready` / `Login form did not become ready`: check host, port, deployment context, and application readiness from the test agent. A 404 is not treated as ready.
- `expected [App Home] but found [Login]`: credentials are invalid or target URL is not this sample app.
- Coverage shows tests but no line coverage: check CTP's server-side coverage-processing logs as well as the client publication message. `Successfully published` can mean test results were uploaded even when coverage generation failed.
- `Unsupported coverage version detected`: align the deployed Jtest agent and CTP's Java coverage processor with compatible versions. A passing browser test or a different baggage header cannot repair an incompatible runtime archive.
- `CTP startTest response did not include baggage` / `does not support parallel tests`: use `-Dparasoft.coverage.integration.parallel.test.enabled=false`.
- `SLF4J(W): No SLF4J providers were found`: runtime logger binding is missing or version-mismatched, so `-Dorg.slf4j.simpleLogger.log...=debug` will not print integration logs. Keep `slf4j-simple` aligned with resolved `slf4j-api` (2.x in this project).

If the Parasoft artifacts are not yet in your local Maven cache, install them first from the sibling project:

```sh
git clone https://github.com/parasoft/coverage-integration.git
cd coverage-integration
mvn -DskipTests install
cd ..\WildflyExample
```

## Access Application

- URL: `http://localhost:8080/app`
- Default login:
  - Username: `admin`
  - Password: `admin`
