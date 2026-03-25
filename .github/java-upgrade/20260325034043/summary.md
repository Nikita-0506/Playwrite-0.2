
# Upgrade Summary: bdd (20260325034043)

- **Completed**: 2026-03-25 09:30
- **Plan Location**: `.github/java-upgrade/20260325034043/plan.md`
- **Progress Location**: `.github/java-upgrade/20260325034043/progress.md`

## Upgrade Result


| Metric     | Baseline | Final | Status |
| ---------- | -------- | ----- | ------ |
| Compile    | ✅ SUCCESS (Java 21, pre-applied) | ✅ SUCCESS | ✅ |
| Tests      | N/A (first run on Java 21) | 1/1 passed (100%) | ✅ |
| JDK        | JDK 21 (pre-applied) | JDK 21.0.9 | ✅ |
| Build Tool | Maven 3.9.13 | Maven 3.9.13 | ✅ |

**Upgrade Goals Achieved**:
- ✅ Java 17 → 21 (compiler source/target/release)

## Tech Stack Changes

## Tech Stack Changes


| Dependency | Before | After | Reason |
| ---------- | ------ | ----- | ------ |
| Java compiler source/target | 17 | 21 | User requested (pre-applied) |
| Java compiler release | 17 | 21 | User requested (pre-applied) |
| Lombok annotation processor | 1.18.26 | 1.18.30 | Java 21 annotation processing support (pre-applied) |
| maven-surefire-plugin | 3.0.0-M9 | 3.2.5 | Milestone → GA stable release for Java 21 test execution |

## Commits


| Commit | Message |
| ------ | ------- |
| b719417 | Step 3: Upgrade maven-surefire-plugin to 3.2.5 - Compile: SUCCESS (includes pre-applied Java 21 changes) |
| f946ceb | Add upgrade plan and progress tracking for session 20260325034043 |
| 9c0ccdc | Step 4: Final Validation - Compile: SUCCESS, Tests: 1/1 passed |

## Challenges

- **maven-surefire-plugin milestone build**
  - **Issue**: Project was using `3.0.0-M9` (milestone), which has inconsistent forked-JVM behaviour under Java 21.
  - **Resolution**: Upgraded to `3.2.5` stable GA — drop-in, no configuration changes needed.

- **Pre-applied pom.xml changes not committed**
  - **Issue**: User had already updated compiler settings but changes were unstaged in working tree.
  - **Resolution**: Changes were validated, the surefire upgrade was added, and all changes committed together on the new upgrade branch.

## Limitations

None. All upgrade goals were achieved and all tests passed.

## Review Code Changes Summary

**Review Status**: ✅ All Passed

**Sufficiency**: ✅ All required upgrade changes are present — compiler 21, Lombok 1.18.30, surefire 3.2.5
**Necessity**: ✅ All changes are strictly necessary — no refactoring or extraneous modifications
- Functional Behavior: ✅ Preserved — BDD step definitions, page objects, and Playwright driver code unchanged
- Security Controls: ✅ Preserved — this is a test automation framework; no authentication, authorization, or password handling in source code

## CVE Scan Results

<!--
  Document the results of the post-upgrade CVE vulnerability scan.
  Run `#validate_cves_for_java(sessionId)` to scan dependencies for known vulnerabilities.
  List any remaining CVEs with severity, affected dependency, and recommended action.

  SAMPLE (no CVEs):
  **Scan Status**: ✅ No known CVE vulnerabilities detected

  **Scanned**: 85 dependencies | **Vulnerabilities Found**: 0

  SAMPLE (with CVEs):
  **Scan Status**: ⚠️ Vulnerabilities detected

  **Scanned**: 85 dependencies | **Vulnerabilities Found**: 3

  | Severity | CVE ID         | Dependency                  | Version | Fixed In | Recommendation                    |
  | -------- | -------------- | --------------------------- | ------- | -------- | --------------------------------- |
  | Critical | CVE-2024-1234  | org.example:vulnerable-lib  | 2.3.1   | 2.3.5    | Upgrade to 2.3.5                  |
  | High     | CVE-2024-5678  | com.example:legacy-util     | 1.0.0   | N/A      | Replace with com.example:new-util |
## CVE Scan Results

**Scan Status**: ⚠️ Vulnerabilities detected

**Scanned**: 17 direct dependencies | **Vulnerabilities Found**: 1

| Severity | Advisory | Dependency | Version | Recommendation |
| -------- | --------- | ---------- | ------- | -------------- |
| **HIGH** | [GHSA-72hv-8253-57qq](https://github.com/advisories/GHSA-72hv-8253-57qq) | `com.fasterxml.jackson.core:jackson-core` | 2.15.2 | Upgrade to 2.17.x or later |

**Note**: This CVE affects the **async (non-blocking) JSON parser only**. This project uses synchronous REST-Assured calls; the async parser is not invoked. Risk is low for this specific codebase, but the dependency should still be upgraded as a security best practice.

## Test Coverage

JaCoCo is not configured in this project's pom.xml. Coverage collection was not available.

**Recommendation**: Add the JaCoCo Maven plugin to enable coverage reporting in future builds.

## Next Steps

- [ ] **Fix CVE (High)**: Upgrade `jackson-core`/`jackson-databind`/`jackson-annotations`/`jackson-datatype-jdk8` from `2.15.2` to `2.17.x` to resolve GHSA-72hv-8253-57qq.
- [ ] Merge branch `appmod/java-upgrade-20260325034043` into your main integration branch.
- [ ] Update any local developer documentation referencing Java 17 → Java 21.
- [ ] Add JaCoCo plugin to `pom.xml` for test coverage reporting.

## Artifacts

- **Plan**: `.github/java-upgrade/20260325034043/plan.md`
- **Progress**: `.github/java-upgrade/20260325034043/progress.md`
- **Summary**: `.github/java-upgrade/20260325034043/summary.md` (this file)
- **Branch**: `appmod/java-upgrade-20260325034043`
