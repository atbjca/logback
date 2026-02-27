---
agent: Agent_Classic_SLF4J
task_ref: Task 4.2
status: Completed
ad_hoc_delegation: false
compatibility_issues: false
important_findings: false
---

# Task Log: Task 4.2 - Update logback-classic POM dependencies and plugin configuration

## Summary
Replaced all Jakarta dependencies with javax equivalents, removed all JPMS surefire arguments from both test executions, removed `useModulePath`, and updated OSGi bundle Provide-Capability to reference javax.servlet instead of jakarta.servlet.

## Details

### Step 1: Replace Jakarta dependencies with javax
- `jakarta.mail:jakarta.mail-api` → `javax.mail:javax.mail-api`
- `jakarta.activation:jakarta.activation-api` → `javax.activation:javax.activation-api`
- `org.eclipse.angus:angus-mail` (test) → `com.sun.mail:javax.mail`
- `jakarta.servlet:jakarta.servlet-api` → `javax.servlet:javax.servlet-api`

### Step 2: Remove default-test JPMS arguments
- Removed entire `<argLine>` block containing `--add-modules jakarta.mail`, `--add-modules jakarta.servlet`, and 4 `--add-opens` directives
- Removed `<useModulePath>true</useModulePath>`

### Step 3: Remove singleJVM JPMS arguments
- Removed `<argLine>` block containing `--add-opens` directive from singleJVM execution

### Additional: OSGi bundle plugin updates
- Added `javax.*;resolution:=optional` to Import-Package (kept `jakarta.*` for backward compatibility)
- Updated Provide-Capability: replaced `jakarta.servlet.ServletContainerInitializer` with `javax.servlet.ServletContainerInitializer`
- Removed SLF4J 2.x ServiceProvider OSGi registrations (no longer applicable with SLF4J 1.x)

## Output
- Modified: `logback-classic/pom.xml`

## Issues
None

## Next Steps
None
