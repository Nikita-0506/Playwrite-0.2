# QA-Automation-Repo

BDD test automation framework using **Playwright Java + Cucumber 7 + TestNG 7 + Maven**, targeting **Java 21 LTS**.

---

## Tech Stack

| Component | Version |
|---|---|
| Java | 21 LTS |
| Playwright | 1.48.0 |
| Cucumber | 7.15.0 |
| TestNG | 7.7.0 |
| Maven | 3.9+ |
| Extent Reports | 5.0.7 (Spark + Cucumber adapter) |
| Log4j2 | 2.20.0 |

---

## Prerequisites

- **JDK 21** installed and set as `JAVA_HOME`
- **Maven 3.9+** on `PATH`
- Internet access to the target environment (QA/DEV/PROD)

---

## Configuration

Edit `src/test/resources/config.properties` to change runtime settings:

```properties
browser=Chrome          # Chrome | Firefox | Edge | WebKit
env=QA                  # QA | DEV | PROD
qaURL=https://...
devURL=https://...
prodURL=https://...
```

---

## Running Tests

**All tagged tests (default `@SMOKE`):**
```bash
mvn clean test
```

**Override browser or environment at runtime:**
```bash
mvn clean test -Dbrowser=Firefox -Denv=DEV
```

**Generate coverage report:**
```bash
mvn clean verify
# Report: target/site/jacoco/index.html
```

---

## Test Tags

| Tag | Purpose |
|---|---|
| `@SMOKE` | Critical path — runs in CI by default |

Configure active tags in `src/test/java/cucumberConfig/TestNGTestRunner.java` (`tags` attribute of `@CucumberOptions`).

---

## Reports

| Report | Location |
|---|---|
| Extent Spark HTML | `test-output/SparkReports <timestamp>/Report/TestRunReport.html` |
| Cucumber JSON | `target/cucumber-reports/Cucumber.json` |
| Surefire XML | `target/surefire-reports/` |
| JaCoCo Coverage | `target/site/jacoco/index.html` (after `mvn verify`) |
| Log file | `logs/test.log` |

---

## Project Structure

```
src/test/java/
  cucumberConfig/     # TestNG runner
  features/           # .feature files (BDD scenarios)
  pageObjects/        # Page object classes
  stepDefinitions/    # Cucumber step bindings
  utilities/          # BaseClass, DriverManager, Hooks, ConfigReader, WaitUtils ...
src/test/resources/
  config.properties   # Runtime configuration
  repository/         # locators.properties (element locators)
  log4j2.xml          # Logging configuration
```

---

## Jenkins Pipeline

The `Jenkinsfile` supports:
- Browser selection via parameter (`Chrome` / `Firefox`)
- Environment selection (`QA` / `DEV` / `PROD`)
- Automatic Playwright browser install stage
- JUnit XML + Cucumber HTML report publishing
- SCM polling every 5 minutes
