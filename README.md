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
- `-Dapp.skip.reachability.check=true`
- `-Dchrome.args=--headless=new,--disable-gpu,--window-size=1600,900,--no-sandbox,--disable-dev-shm-usage`

### Troubleshooting

- If PowerShell parses `-D` values unexpectedly, quote the argument (example: `-D"app.base.url=https://your-host/app"`).
- `Application endpoint is not reachable`: host/port/path is not reachable from the machine or container running Gradle.
- `expected [App Home] but found [Login]`: credentials are invalid or target URL is not this sample app.
- Coverage shows tests but `0%` line coverage: use one header mode only, set valid `envId`, and use the validated `cdp` + explicit `coverage.baggage.header` command above.
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
