# WildflyExample

Simple WildFly web application plus a dedicated Selenium Browser Test module using TestNG and Parasoft coverage integration.

## Modules

- `app`: WAR module for the WildFly-deployable application.
- `selenium-testng-tests`: Browser test module using Selenium + TestNG with Parasoft coverage listeners.

## Prerequisites

- Java 21
- WildFly (for deploying the `app` WAR)

## Build

Build all modules:

```sh
mvnw.cmd clean test
```

Build only the web app WAR:

```sh
mvnw.cmd -pl app clean package
```

## Run Browser Tests (Selenium + TestNG + Parasoft)

1. Deploy `app/target/app.war` to WildFly.
2. Ensure the app is reachable at `http://localhost:8080/app` (or override `app.base.url`).
3. Update `selenium-testng-tests/src/test/resources/coverage-integration.properties` for your CTP environment.
4. Run the Selenium module tests using profile `run-selenium-tests`:

```sh
mvnw.cmd -pl selenium-testng-tests -Prun-selenium-tests test
```

Optional overrides:

```sh
mvnw.cmd -pl selenium-testng-tests -Prun-selenium-tests test -Dapp.base.url=http://localhost:8080/app -Dapp.username=admin -Dapp.password=admin
```

Optional HTTPS and precheck flags:

```sh
mvnw.cmd -pl selenium-testng-tests -Prun-selenium-tests test -D"app.base.url=https://heavyarms/app" -D"app.username=admin" -D"app.password=admin" -D"app.accept.insecure.certs=true" -D"app.skip.reachability.check=false"
```

- `app.accept.insecure.certs` (default: `true`): allow self-signed/untrusted HTTPS certificates in the URL precheck and Chrome session.
- `app.skip.reachability.check` (default: `false`): bypass the initial `/login` reachability check if your environment blocks or rewrites the probe request.

### Change Target App URL

You can point Selenium tests to a different deployed app by overriding `app.base.url`:

```sh
mvnw.cmd -pl selenium-testng-tests test -Dapp.base.url=http://your-host:8080/app
```

Use the app root context URL (for example, `http://your-host:8080/app`), not a page URL such as `/login`, because the test appends route paths internally.

The default value is defined in `selenium-testng-tests/pom.xml` and can be changed there if you want a permanent default.

### Troubleshooting

- `No plugin found for prefix '.base.url=https'`: quote `-D` properties in PowerShell (for example `-D"app.base.url=https://heavyarms/app"`).
- `Application endpoint is not reachable`: the host/port/path is not reachable from the machine running Maven, or DNS name resolution is different between host and container networks.
- `expected [App Home] but found [Login]`: credentials are invalid for that target app, or the URL points to a different login flow.
- Selenium tests not running in reactor build: this is expected unless profile `run-selenium-tests` is enabled.

If the Parasoft artifacts are not yet in your local Maven cache, install them first from the sibling project:

```sh
cd ..\coverage-integration
mvn -DskipTests install
cd ..\WildflyExample
```

## Access Application

- URL: `http://localhost:8080/app`
- Default login:
	- Username: `admin`
	- Password: `admin`