---
agent: Agent_Core
task_ref: Task 2.2
status: Completed
ad_hoc_delegation: false
compatibility_issues: false
important_findings: false
---

# Task Log: Task 2.2 - Update logback-core POM dependencies and plugin configuration

## Summary
Updated logback-core POM to replace all Jakarta dependencies with javax equivalents, fixed compiler configuration for Java 8, removed JPMS surefire arguments, and updated parent POM dependencyManagement to replace angus-mail with javax.mail.

## Details
- **Compiler fix**: Replaced `<release>${jdk.version}</release>` with `<source>`/`<target>` in `default-compile` execution
- **Jakarta → javax dependencies** in logback-core:
  - `jakarta.mail:jakarta.mail-api` → `javax.mail:javax.mail-api`
  - `jakarta.servlet:jakarta.servlet-api` → `javax.servlet:javax.servlet-api`
  - `org.eclipse.angus:angus-mail` (test) → `com.sun.mail:javax.mail` (test)
- **Surefire cleanup**: Removed entire `<argLine>` block containing `--add-opens` and `--add-reads` JPMS flags, plus associated commented-out argLine blocks
- **Bundle plugin**: Changed `jakarta.*;resolution:=optional` to `javax.*;resolution:=optional` in Import-Package
- **Parent POM updates**:
  - Replaced `org.eclipse.angus:angus-mail` with `com.sun.mail:javax.mail:1.6.2` in dependencyManagement
  - Removed `<jakarta.angus-mail.version>` property (no longer needed)

## Output
- Modified files:
  - `logback-core/pom.xml` (dependencies, compiler, surefire, bundle plugin)
  - `pom.xml` (dependencyManagement entry + property removal)
- Verification: zero jakarta references in logback-core POM, zero `<release>` config, zero JPMS args

## Issues
None

## Next Steps
None
