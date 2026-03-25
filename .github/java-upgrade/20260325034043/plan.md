

# Upgrade Plan: bdd (20260325034043)

- **Generated**: 2026-03-25 03:40
- **HEAD Branch**: appmod/java-upgrade-20260324165944
- **HEAD Commit ID**: 2e39f5f

## Available Tools

**JDKs**
- JDK 21.0.9: `C:\Program Files\Java\jdk-21\bin` (JAVA_HOME; used for all steps)

**Build Tools**
- Maven 3.9.13: `C:\Program Files\apache-maven-3.9.13\bin` (no wrapper present; 3.9.x is compatible with Java 21)


## Guidelines

- Validate pre-applied pom.xml changes (compiler source/target/release → 21, Lombok → 1.18.30)
- Validate Jenkinsfile already uses `jdk 'Java21'`
- Minimal changes: only upgrade what is necessary for Java 21 compatibility
- Framework: BDD test automation (Playwright + Cucumber 7.15 + TestNG 7.7 + Maven)

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260325034043
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java from 17 to 21 LTS

### Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java (compiler source/target/release) | 21 (pre-applied) | 21 | - |
| Maven | 3.9.13 | 3.9.0 | - |
| maven-compiler-plugin | 3.11.0 | 3.11.0 | - |
| maven-surefire-plugin | 3.0.0-M9 | 3.1.0 | Milestone release; 3.1+ GA recommended for stable Java 21 forked-JVM test execution |
| Lombok annotation processor | 1.18.30 (pre-applied) | 1.18.20 | - |
| cucumber-java/testng | 7.15.0 | 7.0.0 | - |
| testng | 7.7.0 | 7.5.0 | - |
| playwright | 1.48.0 | 1.20.0 | - |
| logback-classic | 1.4.14 | 1.2.0 | - |
| log4j-core/api | 2.20.0 | 2.17.0 | - |
| jackson-core/databind/annotations | 2.15.2 | 2.12.0 | - |
| rest-assured | 5.3.0 | 4.0.0 | - |
| Jenkinsfile JDK tool | Java21 (pre-applied) | Java21 | - |

### Derived Upgrades

- Upgrade `maven-surefire-plugin` from `3.0.0-M9` to `3.2.5` — Milestone releases are not recommended for production; 3.1+ GA is the minimum for reliable forked-JVM execution under Java 21

## Upgrade Steps

### Step 1: Setup Environment

- **Rationale**: Verify JDK 21 and Maven 3.9.13 are available; no installs needed.
- **Changes to Make**:
  - [ ] Confirm JDK 21.0.9 at `C:\Program Files\Java\jdk-21\bin` (JAVA_HOME)
  - [ ] Confirm Maven 3.9.13 is on PATH
- **Verification**:
  - Command: `mvn -version`
  - JDK: `C:\Program Files\Java\jdk-21\bin`
  - Expected: Maven 3.9.13 running on Java 21

---

### Step 2: Setup Baseline

- **Rationale**: Verify the pre-applied Java 21 pom.xml changes compile successfully before making any further modifications.
- **Changes to Make**:
  - [ ] Run `mvn clean test-compile` with the pre-applied Java 21 settings
  - [ ] Document result (success or failure) as acceptance baseline
- **Verification**:
  - Command: `mvn clean test-compile`
  - JDK: `C:\Program Files\Java\jdk-21\bin`
  - Expected: BUILD SUCCESS — all source and test classes compile with Java 21

---

### Step 3: Upgrade maven-surefire-plugin to 3.2.5

- **Rationale**: `3.0.0-M9` is a milestone; 3.2.5 is the stable GA that properly handles JVM argument propagation and forked process lifecycle under Java 21.
- **Changes to Make**:
  - [ ] In `pom.xml`, bump `maven-surefire-plugin` version from `3.0.0-M9` → `3.2.5`
- **Verification**:
  - Command: `mvn clean test-compile`
  - JDK: `C:\Program Files\Java\jdk-21\bin`
  - Expected: BUILD SUCCESS — no regression introduced

---

### Step 4: Final Validation

- **Rationale**: Confirm all Java 21 upgrade goals are met, project compiles, and all tests pass.
- **Changes to Make**:
  - [ ] Verify `maven.compiler.source/target` = 21 and `release` = 21 in pom.xml
  - [ ] Verify Lombok annotation processor = 1.18.30
  - [ ] Verify maven-surefire-plugin = 3.2.5
  - [ ] Run full test suite; fix ALL failures iteratively
- **Verification**:
  - Command: `mvn clean test`
  - JDK: `C:\Program Files\Java\jdk-21\bin`
  - Expected: BUILD SUCCESS + 100% test pass rate

## Key Challenges

- **maven-surefire-plugin Milestone vs GA**
  - **Challenge**: `3.0.0-M9` is the final milestone before the 3.0.0 GA. Milestone builds can have subtle forked-JVM lifecycle bugs under Java 21 (e.g., surefire forks a new JVM for tests and passes `--add-opens` arguments; milestone behaviour here is less tested than GA).
  - **Strategy**: Upgrade to `3.2.5` (stable GA), which is a drop-in replacement requiring no configuration changes.

## Plan Review

All pre-applied changes are valid and correctly formed:
- `maven.compiler.source/target=21` in `<properties>` ✅
- `<source>21</source><target>21</target><release>21</release>` in `maven-compiler-plugin` 3.11.0 config ✅
- Lombok annotation processor bumped from 1.18.26 → 1.18.30 ✅
- Jenkinsfile `jdk 'Java21'` ✅

One additional change is required: `maven-surefire-plugin` must be upgraded from milestone `3.0.0-M9` to stable GA `3.2.5`. All other dependencies (Cucumber 7.15, TestNG 7.7, Playwright 1.48, Logback 1.4, Log4j 2.20, Jackson 2.15, RestAssured 5.3) are fully compatible with Java 21 and require no changes. No source code modifications are needed.

