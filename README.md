# InsureCRM BDD Automation Framework

A BDD test automation framework for the **InsureCRM** web application, built with **Playwright Java**, **Cucumber**, and **TestNG**, running on **Java 25**.

---

## Tech Stack

| Tool / Library | Version | Purpose |
|---|---|---|
| Java | 25 (LTS) | Runtime |
| Maven | 3.9+ | Build & dependency management |
| Playwright Java | 1.48.0 | Browser automation |
| Cucumber Java | 7.15.0 | BDD feature parsing & step binding |
| Cucumber TestNG | 7.15.0 | TestNG integration for Cucumber |
| TestNG | 7.7.0 | Test execution & lifecycle |
| ExtentReports | 5.0.7 | HTML test reporting (Spark) |
| Log4j2 | 2.20.0 | Logging |
| Logback | 1.4.14 | SLF4J logging backend |
| Jackson | 2.15.2 | JSON serialisation |
| Rest Assured | 5.3.0 | API testing support |
| commons-io | 2.19.0 | File utilities |

---

## Project Structure

```
src/test/
├── java/
│   ├── cucumberConfig/
│   │   └── TestNGTestRunner.java     # Cucumber runner (TestNG + retry)
│   ├── features/
│   │   └── login.feature             # BDD feature files
│   ├── pageObjects/
│   │   └── CommonPage.java           # Page object helpers
│   ├── stepDefinitions/
│   │   ├── CommonSteps.java          # Shared step definitions
│   │   └── LoginSteps.java           # Login-specific steps
│   └── utilities/
│       ├── BaseClass.java            # Locator loading & helper methods
│       ├── CommonMethod.java         # Reusable action methods
│       ├── ConfigReader.java         # Reads config.properties
│       ├── DriverManager.java        # Playwright browser lifecycle
│       ├── Hooks.java                # Cucumber Before/After lifecycle
│       ├── InputUtils.java           # Input helpers
│       ├── PlaywrightHelper.java     # Playwright wrapper utilities
│       ├── RetryFailure.java         # TestNG retry analyser
│       ├── RetryListener.java        # TestNG retry listener
│       ├── ScenarioContext.java      # Cucumber scenario state sharing
│       └── WaitUtils.java            # Explicit wait helpers
└── resources/
    ├── config.properties             # Environment URLs & browser config
    ├── cucumber.properties           # Cucumber settings
    ├── extent.properties             # ExtentReports output settings
    ├── log4j2.xml                    # Log4j2 logging config
    └── repository/
        └── locators.properties       # UI element locators
```

---

## Configuration

Edit `src/test/resources/config.properties` to configure the target environment and browser:

```properties
browser=Chrome
env=QA

devURL=https://insurecrm-dev.inadev.net/#/Login
qaURL=https://insurecrm-qa.inadev.net/#/Login
prodURL=https://insurecrm.thepolicyexchange.com/
```

Locators are stored separately in `src/test/resources/repository/locators.properties`.

---

## Running Tests

### Install Playwright browsers (first-time or after updating Playwright version)

```bash
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

### Run all `@SMOKE` tagged tests (default)

```bash
mvn clean test
```

### Run with a specific browser and environment

```bash
mvn clean test -Dbrowser=Chrome -Denv=QA
mvn clean test -Dbrowser=Firefox -Denv=DEV
```

Supported browsers: `Chrome`, `Firefox`  
Supported environments: `QA`, `DEV`, `PROD`

### Run and continue on test failure (used by CI)

```bash
mvn clean verify -Dbrowser=Chrome -Denv=QA -Dmaven.test.failure.ignore=true
```

---

## Tag-based Test Execution

Tags are defined in the Cucumber feature files and filtered in `TestNGTestRunner.java`:

| Tag | Description |
|---|---|
| `@SMOKE` | Core smoke tests (default run) |
| `@REGRESSION` | Full regression suite |

To change the active tag, update the `tags` attribute in `TestNGTestRunner.java`:

```java
@CucumberOptions(tags = "@REGRESSION", ...)
```

---

## Test Reports

After a test run, reports are generated in the following locations:

| Report | Location |
|---|---|
| **ExtentReports (Spark HTML)** | `test-output/SparkReports <timestamp>/Report/TestRunReport.html` |
| **Screenshots (on failure)** | `test-output/SparkReports <timestamp>/Screenshots/` |
| **Cucumber JSON** | `target/cucumber-reports/Cucumber.json` |
| **Surefire XML / HTML** | `target/surefire-reports/` |

---

## CI/CD — Jenkins Pipeline

The `Jenkinsfile` defines a parameterised pipeline that:

1. **Checkout** — pulls latest code from GitHub (webhook-triggered)
2. **Install Playwright** — installs browser binaries via Maven
3. **Run Tests** — executes `mvn clean verify` with selected browser and environment
4. **Upload Reports to S3** — uploads Surefire, Cucumber, and Spark reports to `s3://qa-accelerator-inadev/all_reports/build-<n>/`

**Pipeline parameters** (configurable at run time):

| Parameter | Options | Default |
|---|---|---|
| `BROWSER` | `Chrome`, `Firefox` | `Chrome` |
| `ENV` | `QA`, `DEV`, `PROD` | `QA` |

---

## Retry Mechanism

Failed tests are automatically retried via `RetryFailure` (TestNG `IRetryAnalyzer`).  
The maximum retry count is configured in `RetryFailure.java` (`maxRetry`, currently `0`).

---

## Logging

Log4j2 is configured via `src/test/resources/log4j2.xml`. Log files are written to `logs/`.

---

## Prerequisites

- **Java 25** — [Eclipse Temurin](https://adoptium.net/) recommended
- **Maven 3.9+**
- **AWS CLI** — required only for the Jenkins S3 upload stage
