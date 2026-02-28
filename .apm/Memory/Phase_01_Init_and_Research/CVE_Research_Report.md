# CVE Research Report: Logback 1.2.13 — Unfixed Vulnerabilities

> Generated: 2026-02-28 | Branch: `branch_1.2.x-bjca-patch` (base: `2648b9e7f`)

## Scope

All CVEs affecting `ch.qos.logback:logback-core` / `logback-classic` version **1.2.13** that are **NOT** already fixed in 1.2.13 or earlier.

### Excluded CVEs (already fixed)
| CVE ID | Fixed In | Reason |
|---|---|---|
| CVE-2023-6378 | 1.2.13 | DoS via receiver — fixed in the base version |
| CVE-2023-6481 | 1.2.13 | Same underlying fix as CVE-2023-6378 |
| CVE-2021-42550 | 1.2.9 | JNDI-related RCE — fixed in earlier release |

---

## CVE-1: CVE-2024-12798 — Arbitrary Code Execution via JaninoEventEvaluator

| Field | Value |
|---|---|
| **CVE ID** | CVE-2024-12798 |
| **Published** | 2024-12-19 |
| **CVSS v4.0** | **5.9 (Medium)** |
| **CVSS Vector** | `AV:L/AC:L/AT:P/PR:L/UI:P/VC:L/VI:H/VA:L/SC:L/SI:H/SA:L/RE:L/U:Clear` |
| **CWE** | CWE-917: Improper Neutralization of Special Elements in Expression Language |
| **Discoverer** | 7asecurity |

### Affected Components
- `ch.qos.logback.classic.boolex.JaninoEventEvaluator` (logback-classic)
- `ch.qos.logback.core.boolex.JaninoEventEvaluatorBase` (logback-core)
- `ch.qos.logback.access.boolex.JaninoEventEvaluator` (logback-access)

### Affected Versions
- 0.1 through 1.3.14 and 1.4.0 through 1.5.12
- **logback 1.2.13 is affected**

### Attack Vector
An attacker who can write to a logback configuration file (or inject a malicious `LOGBACK_CONFIGURATION_FILE` environment variable) can execute arbitrary Java code via the `JaninoEventEvaluator` extension. The Janino library must be on the classpath.

### Official Fix

| Branch | Fix Version | Commit | Approach |
|---|---|---|---|
| **1.3.x** | **1.3.15** | `c17e5883845e5bc4dec49b3fe74f744e0e574a2b` (classic) + `b44b940cc7d4839e06e31a7d60dca174b99c1aa5` (access) | Complete removal of JaninoEventEvaluator |
| 1.5.x | 1.5.13 | `32638aa7e99c0135cb1b81806ed05352e6bfe27f` (tag `v_1.5.13`) | Same approach |

### Fix Details (on branch_1.3.x)

**Commit `c17e58838`** — "remove JaninoEventEvaluator" (11 files, +42/-664):
- **Deleted:** `logback-classic/src/main/java/.../classic/boolex/JaninoEventEvaluator.java` (150 lines)
- **Deleted:** `logback-core/src/main/java/.../core/boolex/JaninoEventEvaluatorBase.java` (96 lines)
- **Modified:** `ClassicEvaluatorAction.java` — removed `JaninoEventEvaluator` references
- **Modified:** `LogbackClassicDefaultNestedComponentRules.java` — removed default mapping
- **Deleted:** test classes for JaninoEventEvaluator
- **Added:** blackbox test `ifWithExec.xml` to verify exec protection works

**Commit `b44b940cc`** — "remove JaninoEventEvaluator" (5 files, +1/-167):
- **Deleted:** `logback-access/src/main/java/.../access/boolex/JaninoEventEvaluator.java` (84 lines)
- **Modified:** `AccessEvaluatorAction.java` — removed `JaninoEventEvaluator` references
- **Modified:** `LogbackAccessDefaultNestedComponentRegistryRules.java` — removed mapping

### 1.2.x Adaptation Notes
- **Strategy:** Delete the three `JaninoEventEvaluator` classes and their base class
- **Difficulty: LOW** — straightforward deletion of classes and removal of references
- **Key differences in 1.2.x:**
  - `ClassicEvaluatorAction` references `JaninoEventEvaluator` as default evaluator class — need to remove this default or replace with a safe stub
  - `DefaultNestedComponentRules` (1.2.x name) references it — needs cleanup
  - Users relying on `<evaluator>` with Janino will lose this functionality (acceptable security tradeoff)

---

## CVE-2: CVE-2024-12801 — Server-Side Request Forgery (SSRF) via SaxEventRecorder

| Field | Value |
|---|---|
| **CVE ID** | CVE-2024-12801 |
| **Published** | 2024-12-19 |
| **CVSS v4.0** | **2.4 (Low)** |
| **CVSS Vector** | `AV:L/AC:L/AT:P/PR:L/UI:P/VC:L/VI:N/VA:L/SC:H/SI:H/SA:H` |
| **CWE** | CWE-918: Server-Side Request Forgery (SSRF) |
| **Discoverer** | 7asecurity |

### Affected Components
- `ch.qos.logback.core.joran.event.SaxEventRecorder` (logback-core)

### Affected Versions
- 0.1 through 1.3.14 and 1.4.0 through 1.5.12
- **logback 1.2.13 is affected**

### Attack Vector
An attacker who can modify logback's XML configuration files can forge server-side requests by injecting a `DOCTYPE` declaration with an external entity reference (e.g., `<!DOCTYPE test SYSTEM "http://internal-service/">`). The `SaxEventRecorder` does not disable external entity resolution, so the XML parser will attempt to fetch the referenced URL.

### Official Fix

| Branch | Fix Version | Commit | Approach |
|---|---|---|---|
| **1.3.x** | **1.3.15** | `2863a4974a3649b5b00d4a529ee6ff2063470f35` | Override `resolveEntity()` to block external DTDs |
| 1.5.x | 1.5.13 | `5f05041cba4c4ac0a62748c5c527a2da48999f2d` | Same approach |

### Fix Details (on branch_1.3.x, commit `2863a4974`)

**File:** `logback-core/src/main/java/ch/qos/logback/core/joran/event/SaxEventRecorder.java` (+45/-14)

Key change — added `resolveEntity()` override:
```java
@Override
public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    addWarn("Document Type Declaration (DOCTYPE) with external file reference is");
    addWarn("disallowed to prevent Server-Side Request Forgery (SSRF) attacks.");
    addWarn("returning contents of SYSTEM " + systemId + " as a white space");
    return new InputSource(new ByteArrayInputStream(" ".getBytes()));
}
```

Also added:
- Import for `java.io.ByteArrayInputStream`
- Test file `event-ssrf.xml` with malicious DOCTYPE
- Test `testEventSSRF()` with `@Timeout` annotation

### 1.2.x Adaptation Notes
- **Strategy:** Add the `resolveEntity()` override to the 1.2.x version of `SaxEventRecorder`
- **Difficulty: LOW** — minimal code addition, clean port
- **Key differences in 1.2.x:**
  - The 1.2.x `SaxEventRecorder` uses `cai` (ContextAwareImpl) instead of `contextAwareImpl` — use `cai.addWarn()` instead
  - Constructor signature differs slightly (no `ElementPath` parameter overload in 1.2.x)
  - Import `java.io.ByteArrayInputStream` needs to be added
  - The `resolveEntity()` method is a standard `DefaultHandler` override — identical pattern works

---

## CVE-3: CVE-2025-11226 — Arbitrary Code Execution via `<if>` Condition Attribute

| Field | Value |
|---|---|
| **CVE ID** | CVE-2025-11226 |
| **Published** | 2025-10-01 |
| **CVSS v4.0** | **5.9 (Medium)** |
| **CWE** | CWE-454: External Initialization of Trusted Variables or Data Stores |
| **Discoverer** | Heihu577 |

### Affected Components
- `ch.qos.logback.core.joran.conditional.IfAction` (1.2.x) / `IfModelHandler` (1.3.x+)
- `ch.qos.logback.core.joran.conditional.PropertyEvalScriptBuilder`

### Affected Versions
- All versions prior to 1.3.16 and 1.4.0 through 1.5.18
- **logback 1.2.13 is affected**

### Prerequisites
- Janino library **and** Spring Framework must both be on the classpath
- Attacker must have write access to configuration file or ability to inject env var

### Attack Vector
An attacker can execute arbitrary code by using the Java `new` operator within the `condition` attribute of `<if>` elements in logback XML configuration (e.g., `<if condition='new java.lang.ProcessBuilder("cmd").start() != null'>`). The Janino-based condition evaluator does not restrict the `new` operator.

### Official Fix

| Branch | Fix Version | Commit | Approach |
|---|---|---|---|
| **1.3.x** | **1.3.16** | `e3aa0f440cf7a3b98f16fbb21bcea83f72be71e6` | Disallow `new` operator in condition attribute |
| 1.5.x | 1.5.19 | `e572d4f87f06674788eb3ca7148e8d1dffc615fa` | Same approach |

### Fix Details (on branch_1.3.x, commit `e3aa0f440`)

**File:** `logback-core/src/main/java/ch/qos/logback/core/model/processor/conditional/IfModelHandler.java` (+41/-4)

Key changes:
```java
// New constants
public static final String NEW_OPERATOR_DISALLOWED_MSG = "The 'condition' attribute may not contain the 'new' operator.";
public static final String NEW_OPERATOR_DISALLOWED_SEE = "See also " + CoreConstants.CODES_URL + "#conditionNew";

// New check before condition evaluation
if (hasNew(conditionStr)) {
    addError(NEW_OPERATOR_DISALLOWED_MSG);
    addError(NEW_OPERATOR_DISALLOWED_SEE);
    return;
}

// New helper method
private boolean hasNew(String conditionStr) {
    return conditionStr.contains("new ");
}
```

### 1.2.x Adaptation Notes
- **Strategy:** Add `new` operator check to `IfAction.java` (1.2.x equivalent of `IfModelHandler`)
- **Difficulty: LOW** — small code addition, different class but same logic
- **Key differences in 1.2.x:**
  - 1.2.x uses `IfAction` (SAX-event based Joran actions) instead of `IfModelHandler` (model-based processing in 1.3.x+)
  - The condition string is obtained from `attributes.getValue(CONDITION_ATTR)` in the `begin()` method
  - Add the `hasNew()` check right after `conditionAttribute` is substituted but before `pesb.build(conditionAttribute)`
  - Use `addError()` which is available from the parent `Action` class

---

## CVE-4: CVE-2026-1225 — Arbitrary Code Execution via Unrestricted Class Instantiation

| Field | Value |
|---|---|
| **CVE ID** | CVE-2026-1225 |
| **Published** | 2026-01-22 |
| **CVSS v3.1** | **5.0 (Medium)** |
| **CVSS Vector** | `AV:L/AC:H/PR:H/UI:N/S:C/C:L/I:L/A:L` |
| **CWE** | CWE-20: Improper Input Validation |

### Affected Components
- `ch.qos.logback.core.model.processor.ImplicitModelHandler` (1.5.x) / `NestedComplexPropertyIA` (1.2.x)
- `ch.qos.logback.core.joran.util.PropertySetter`
- `ch.qos.logback.core.util.OptionHelper`

### Affected Versions
- All versions up to and including 1.5.24
- **logback 1.2.13 is affected**
- **No 1.3.x backport exists** as of 2026-02-28

### Attack Vector
An attacker who can modify logback configuration files can instantiate arbitrary classes already on the classpath by specifying a `class` attribute on nested XML elements. The Joran configuration engine does not validate that the instantiated class is compatible with the expected property type. Impact is limited because the instance is likely discarded.

### Official Fix

| Branch | Fix Version | Commit | Approach |
|---|---|---|---|
| 1.5.x | **1.5.25** | `d28931f3b9ede954285cd22d44e029142bba52e6` | Restrict instantiation to expected supertype |
| **1.3.x** | **None** | — | **No backport exists** |

### Fix Details (on master/1.5.x, commit `d28931f3b`)

**8 files changed** (+138/-68):

1. **`ImplicitModelDataForComplexProperty.java`** — Added `expectedPropertyType` field with getter/setter

2. **`PropertySetter.java`** — Added two new methods:
   ```java
   public Class<?> getTypeForComplexProperty(String nestedElementTagName, AggregationType aggregationType) {
       // Looks up the setter/adder method and returns the parameter type
   }

   private void checkParameterCount(Method aMethod, String nestedElementTagName) {
       // Validates method has exactly one parameter
   }
   ```

3. **`ImplicitModelHandler.java`** — Modified `doComplex()` method:
   - Before creating the object, obtains the expected property type from the parent bean
   - Uses new `OptionHelper.instantiateClassWithSuperclassRestriction()` instead of `classObj.getConstructor().newInstance()`

4. **`OptionHelper.java`** — Added:
   ```java
   public static Object instantiateClassWithSuperclassRestriction(Class<?> classObj, Class<?> superClass)
           throws IncompatibleClassException, DynamicClassLoadingException {
       if (!superClass.isAssignableFrom(classObj)) {
           throw new IncompatibleClassException(superClass, classObj);
       }
       return classObj.getConstructor().newInstance();
   }
   ```

5. **`AggregationAssessor.java`** — Minor comment addition

### 1.2.x Adaptation Notes
- **Strategy:** Add supertype validation before class instantiation in `NestedComplexPropertyIA`
- **Difficulty: MEDIUM** — requires understanding different architecture
- **Key differences in 1.2.x:**
  - 1.2.x uses **`NestedComplexPropertyIA`** (SAX-based) instead of `ImplicitModelHandler` (model-based)
  - 1.2.x uses **`IADataForComplexProperty`** instead of `ImplicitModelDataForComplexProperty`
  - The vulnerable line is `actionData.setNestedComplexProperty(componentClass.newInstance())` in `NestedComplexPropertyIA.begin()`
  - Need to:
    1. Add `getTypeForComplexProperty()` method to 1.2.x `PropertySetter`
    2. Add `instantiateClassWithSuperclassRestriction()` to `OptionHelper`
    3. In `isApplicable()`, store the expected property type in `IADataForComplexProperty`
    4. In `begin()`, use the new restricted instantiation method
  - 1.2.x `PropertySetter` has a `BeanDescription` approach vs 1.5.x `AggregationAssessor` — method lookup logic differs
  - `componentClass.newInstance()` (deprecated) is used in 1.2.x vs `classObj.getConstructor().newInstance()` in 1.5.x

---

## Summary

| CVE ID | Severity | CVSS | Type | Best Source Branch | Fix Commit (on that branch) | 1.2.x Adaptation Difficulty |
|---|---|---|---|---|---|---|
| CVE-2024-12798 | Medium | 5.9 | ACE (JaninoEvaluator) | **1.3.x** | `c17e58838` + `b44b940cc` | **LOW** — delete classes |
| CVE-2024-12801 | Low | 2.4 | SSRF (SaxEventRecorder) | **1.3.x** | `2863a4974` | **LOW** — add method override |
| CVE-2025-11226 | Medium | 5.9 | ACE (if condition) | **1.3.x** | `e3aa0f440` | **LOW** — add string check |
| CVE-2026-1225 | Medium | 5.0 | ACE (class instantiation) | **1.5.x** (no 1.3.x backport) | `d28931f3b` | **MEDIUM** — architectural differences |

### Key Observations

1. **All 4 CVEs confirmed** — no additional CVEs found beyond the initial candidates
2. **3 of 4 have 1.3.x backports** — use these as primary reference for 1.2.x adaptation
3. **CVE-2026-1225 has no 1.3.x backport** — must adapt from the 1.5.x fix, which has more architectural differences
4. **All CVEs require local access** — attacker must be able to modify config files or inject env vars
5. **Common theme:** configuration file manipulation for code execution/SSRF
6. **1.2.x uses SAX-based Joran actions** while 1.3.x+ uses a model-based processing pipeline — all adaptations must account for this architectural difference
