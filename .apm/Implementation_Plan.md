# Logback Java 8 + SLF4J 1.x Migration – APM Implementation Plan
**Memory Strategy:** Dynamic-MD
**Last Modification:** Hotfix — logback-examples 模块 UserServletFilter.java 中 jakarta.servlet 导入替换为 javax.servlet，修复全项目 mvn clean compile 失败。
**Project Overview:** 将 logback 1.5.33 的核心模块（logback-core、logback-classic）及其黑盒测试模块改造为 Java 8 兼容，同时将 SLF4J 从 2.0.17 降级到 1.7.36，Jakarta EE 依赖回退到 javax 命名空间，移除 JPMS module-info 和 Multi-Release JAR。版本号变更为 1.5.33-slf4j1x-jre8，分支 master-jre8。

## Phase 1: Project Setup & Parent POM Configuration

### Task 1.1 – Create branch and update project version - Agent_Core
**Objective:** 创建 master-jre8 分支并将所有 POM 版本号更新为 1.5.33-slf4j1x-jre8。
**Output:** 新分支及统一的版本号。
**Guidance:** 确保 parent POM 和所有子模块 POM 版本一致。

- 从 master 创建并切换到 `master-jre8` 分支
- 修改 parent `pom.xml` 中 `<version>` 为 `1.5.33-slf4j1x-jre8`
- 修改所有子模块 `pom.xml` 中 `<parent><version>` 为 `1.5.33-slf4j1x-jre8`

### Task 1.2 – Update parent POM Java compilation and global dependency versions - Agent_Core
**Objective:** 将编译目标降为 Java 8，全局依赖版本切换到 Java 8 兼容版本。
**Output:** Parent POM 编译配置和依赖版本属性就绪。
**Guidance:** `<release>` 是 Java 9+ 编译器选项，必须替换为 `<source>`/`<target>`。Jakarta→Javax 需同时变更 groupId 和 artifactId。**Depends on: Task 1.1 Output**

1. 将 `<jdk.version>` 从 `11` 改为 `1.8`，将 `<maven.compiler.release>` 替换为 `<maven.compiler.source>` 和 `<maven.compiler.target>`
2. 将 `<slf4j.version>` 从 `2.0.17` 改为 `1.7.36`
3. 将 `<dependencyManagement>` 中 `jakarta.servlet-api`、`jakarta.mail-api`、`jakarta.activation-api` 替换为 `javax.servlet-api 4.0.1`、`javax.mail 1.6.2`、`javax.activation 1.2.0`
4. 移除 parent POM 中与 Multi-Release JAR 或 JPMS 相关的全局配置（如有）

## Phase 2: logback-core Module Migration

### Task 2.1 – Remove module-info.java and Multi-Release JAR from logback-core - Agent_Core
**Objective:** 移除 logback-core 中的 JPMS 模块声明和 Multi-Release JAR 支持。
**Output:** logback-core 不再包含 Java 9+ 模块系统和多版本 JAR 相关文件及配置。
**Guidance:** 确保同时清理源码文件和 POM 配置。

- 删除 `logback-core/src/main/java/module-info.java`
- 删除 `logback-core/src/main/java21/` 整个目录
- 移除 `logback-core/pom.xml` 中的 `java21-compile` execution 配置
- 移除 `logback-core/pom.xml` 中 maven-bundle-plugin 的 `<Multi-Release>true</Multi-Release>` 配置

### Task 2.2 – Update logback-core POM dependencies and plugin configuration - Agent_Core
**Objective:** 将 logback-core 的 Jakarta 依赖替换为 javax，移除 JPMS 相关的 Surefire 参数。
**Output:** logback-core POM 依赖和插件配置完全兼容 Java 8。
**Guidance:** angus-mail 是 Jakarta Mail 的实现，需替换为 javax.mail 的 Java 8 兼容实现。**Depends on: Task 2.1 Output**

- 将 `jakarta.mail:jakarta.mail-api` 替换为 `javax.mail:javax.mail-api`，将 `jakarta.servlet:jakarta.servlet-api` 替换为 `javax.servlet:javax.servlet-api`
- 将测试依赖 `org.eclipse.angus:angus-mail` 替换为 `com.sun.mail:javax.mail`
- 移除 maven-surefire-plugin argLine 中的 `--add-opens` 和 `--add-reads` JPMS 参数

### Task 2.3 – Replace Java 11 syntax and Jakarta imports in logback-core source code - Agent_Core
**Objective:** 将 Java 10+ 语法和 Jakarta 包名替换为 Java 8 兼容形式。
**Output:** logback-core 源码完全兼容 Java 8 语法和 javax 命名空间。
**Guidance:** `var` 需根据右侧表达式推断实际类型。jakarta→javax 替换涉及 servlet 和 mail 两个包。**Depends on: Task 2.2 Output**

- 在 `DirectJson.java` 中将 4 处 `var` 替换为显式类型声明（`int target`、`int chr`、`int newSize` 等）
- 在 `ViewStatusMessagesServletBase.java`、`SMTPAppenderBase.java`、`LoginAuthenticator.java` 中将 `jakarta.servlet.*` → `javax.servlet.*`、`jakarta.mail.*` → `javax.mail.*`

### Task 2.4 – Verify logback-core tests pass under Java 8 - Agent_Core
**Objective:** 在 Java 8 环境下运行 logback-core 完整测试套件并修复所有失败。
**Output:** logback-core 所有测试在 Java 8 下通过。
**Guidance:** 使用 `sdk use java 8.0.482-kona` 切换环境。不修改 JAVA_HOME 环境变量。**Depends on: Task 2.3 Output**

1. 使用 `sdk use java 8.0.482-kona` 切换到 Java 8 环境，执行 `mvn test -pl logback-core`
2. 分析任何测试失败，识别 Java 8 不兼容原因（API 差异、语法问题、模块系统依赖等）
3. 修复失败的测试代码并重新运行直到全部通过

## Phase 3: logback-core-blackbox Test Migration

### Task 3.1 – Remove module-info.java and JPMS config from logback-core-blackbox - Agent_Core
**Objective:** 移除 logback-core-blackbox 的 JPMS 模块声明和相关配置。
**Output:** logback-core-blackbox 不再包含 Java 9+ 模块系统相关文件和配置。
**Guidance:** module-info.java 位于 test 目录下。POM 中的 JPMS 参数已注释但仍应清理。**Depends on: Task 2.4 Output**

- 删除 `logback-core-blackbox/src/test/java/module-info.java`
- 清理 `logback-core-blackbox/pom.xml` 中注释掉的 `--add-opens`/`--add-reads` JPMS 配置

### Task 3.2 – Verify logback-core-blackbox tests pass under Java 8 - Agent_Core
**Objective:** 在 Java 8 环境下运行 logback-core-blackbox 全部 17 个黑盒测试并修复所有失败。
**Output:** logback-core-blackbox 所有测试在 Java 8 下通过。
**Guidance:** 使用 `sdk use java 8.0.482-kona` 切换环境。**Depends on: Task 3.1 Output**

1. 使用 Java 8 环境执行 `mvn test -pl logback-core-blackbox`
2. 分析失败测试，识别 Java 8 不兼容原因
3. 修复并重新验证直到全部通过

## Phase 4: logback-classic Module Migration

### Task 4.1 – Remove module-info.java from logback-classic - Agent_Classic_SLF4J
**Objective:** 移除 logback-classic 的 JPMS 模块声明。
**Output:** logback-classic 不再包含 Java 9+ 模块系统声明。
**Guidance:** module-info.java 中包含 SLF4J 2.x provider 声明和 jakarta 依赖，整个文件删除。**Depends on: Task 3.2 Output by Agent_Core**

- 删除 `logback-classic/src/main/java/module-info.java`

### Task 4.2 – Update logback-classic POM dependencies and plugin configuration - Agent_Classic_SLF4J
**Objective:** 替换 Jakarta 依赖为 javax，移除 Surefire JPMS 参数。
**Output:** logback-classic POM 依赖和插件配置兼容 Java 8。
**Guidance:** Surefire 有两个 execution（default-test 和 singleJVM）均包含 JPMS 参数。**Depends on: Task 4.1 Output**

1. 将 POM 中的 Jakarta 依赖声明替换为 javax 对应项
2. 移除 `default-test` execution 中的全部 `--add-modules` 和 `--add-opens` 参数
3. 移除 `singleJVM` execution 中的 `--add-opens` 参数

### Task 4.3 – Replace SLF4J 2.x binding with SLF4J 1.x StaticLoggerBinder - Agent_Classic_SLF4J
**Objective:** 移除 SLF4J 2.x ServiceProvider 机制，创建 SLF4J 1.x StaticLoggerBinder。
**Output:** logback-classic 通过 StaticLoggerBinder 与 SLF4J 1.7.36 绑定。
**Guidance:** StaticLoggerBinder 需实现 LoggerFactoryBinder 接口，包含 getSingleton() 和 REQUESTED_API_VERSION。参考 LogbackServiceProvider 的初始化逻辑。**Depends on: Task 4.2 Output**

1. 删除 `LogbackServiceProvider.java`
2. 在 `ch.qos.logback.classic.util` 包下创建 `StaticLoggerBinder.java`，实现 `org.slf4j.spi.LoggerFactoryBinder` 接口，参考 LogbackServiceProvider 的初始化逻辑（LoggerContext 创建、MDCAdapter 设置、自动配置）
3. 确保 `StaticLoggerBinder` 包含 `getSingleton()` 静态方法和 `REQUESTED_API_VERSION` 字段
4. 更新 `ContextSelectorStaticBinder` 中对 StaticLoggerBinder 的引用（如有需要）

### Task 4.4 – Remove SLF4J 2.x Fluent API usage from Logger class - Agent_Classic_SLF4J
**Objective:** 从 Logger 类中移除 SLF4J 2.x Fluent API 相关代码。
**Output:** Logger 类仅实现 SLF4J 1.x 接口。
**Guidance:** Logger.java 是核心类，修改需精确。仅移除 2.x 专有部分，保留 1.x 兼容的 LocationAwareLogger 实现。**Depends on: Task 4.3 Output**

1. 移除 `Logger.java` 中对 `DefaultLoggingEventBuilder`、`LoggingEventBuilder`、`LoggingEventAware` 的 import
2. 移除实现了 `LoggingEventAware` 接口的方法（如 `log(LoggingEvent)` 等 Fluent API 方法）
3. 确保 Logger 类仅实现 SLF4J 1.x 的 `org.slf4j.Logger` 和 `LocationAwareLogger` 接口

### Task 4.5 – Remove KeyValuePair usage from logback-classic source code - Agent_Classic_SLF4J
**Objective:** 从 logback-classic main 源码中移除所有 SLF4J 2.x KeyValuePair 依赖。
**Output:** logback-classic main 源码不再引用 KeyValuePair。
**Guidance:** 修改从 ILoggingEvent 接口开始，级联到所有实现类。如果 KeyValuePairConverter/MaskedKeyValuePairConverter 完全围绕 KVP 功能，则整个类删除。**Depends on: Task 4.4 Output**

1. 从 `ILoggingEvent` 接口中移除 `getKeyValuePairs()` 方法声明及 KeyValuePair import
2. 从 `LoggingEvent` 和 `LoggingEventVO` 中移除 KeyValuePair 相关字段、getter/setter 及 import
3. 如果 `KeyValuePairConverter` 和 `MaskedKeyValuePairConverter` 完全围绕 KeyValuePair 功能，则删除这两个类；否则移除 KVP 依赖
4. 从 `TTLLLayout` 和 `JsonEncoder` 中移除 KeyValuePair 相关的格式化/编码逻辑

### Task 4.6 – Replace Jakarta servlet imports with javax in logback-classic - Agent_Classic_SLF4J
**Objective:** 将 logback-classic 中所有 Jakarta servlet 引用替换为 javax。
**Output:** logback-classic 源码使用 javax.servlet 命名空间。
**Guidance:** 涉及 7 个 main 文件和 1 个 test 文件，全部是 jakarta.servlet 包。**Depends on: Task 4.5 Output**

- 在 MDCInsertingServletFilter、ViewStatusMessagesServlet、LogbackServletContainerInitializer、LogbackServletContextListener、ContextDetachingSCL、LoggerContextFilter 中将 `jakarta.servlet.*` → `javax.servlet.*`
- 在 test 文件 `LogbackServletContainerInitializerTest.java` 中做同样替换

### Task 4.7 – Update META-INF/services files - Agent_Classic_SLF4J
**Objective:** 更新 SLF4J 和 Servlet 的服务发现文件以匹配新的绑定机制和命名空间。
**Output:** META-INF/services 文件反映 SLF4J 1.x 和 javax 命名空间。
**Guidance:** SLF4J 1.x 通过类路径发现 StaticLoggerBinder，不需要 services 文件。验证是否需要保留 services 目录。**Depends on: Task 4.3 Output**

- 删除 `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`
- 将 `META-INF/services/jakarta.servlet.ServletContainerInitializer` 重命名为 `META-INF/services/javax.servlet.ServletContainerInitializer`

### Task 4.8 – Fix logback-classic test code for Java 8 and SLF4J 1.x compatibility - Agent_Classic_SLF4J
**Objective:** 修复所有测试代码中的 SLF4J 2.x 专有 API 和 Java 9+ API 使用。
**Output:** logback-classic 测试代码编译通过，兼容 Java 8 和 SLF4J 1.x。
**Guidance:** 涉及约 11 个 test 文件的 KeyValuePair、2 个文件的 List.of()、1 个文件的 LoggingEventBuilder。**Depends on: Task 4.5 Output, Task 4.6 Output, Task 4.7 Output**

1. 修复 `LoggerTest.java` 中对 `LoggingEventBuilder`/`NOPLoggingEventBuilder` 的使用，移除或重写相关测试方法
2. 修复/移除所有 test 文件中的 KeyValuePair 相关测试代码
3. 将 `MaskedKeyValuePairConverterTest.java` 和 `ConverterTest.java` 中的 `List.of()` 替换为 `Arrays.asList()` 或 `Collections.singletonList()`
4. 运行编译检查确保所有测试代码编译通过

### Task 4.9 – Verify logback-classic tests pass under Java 8 - Agent_Classic_SLF4J
**Objective:** 在 Java 8 环境下运行 logback-classic 完整测试套件并修复所有失败。
**Output:** logback-classic 所有测试在 Java 8 下通过。
**Guidance:** 使用 `sdk use java 8.0.482-kona` 切换环境。**Depends on: Task 4.8 Output**

1. 使用 Java 8 环境执行 `mvn test -pl logback-classic`
2. 分析任何测试失败，区分是 SLF4J 降级导致还是 Java 版本导致
3. 修复失败的测试并重新运行直到全部通过

## Phase 5: logback-classic-blackbox Test Migration

### Task 5.1 – Update logback-classic-blackbox POM dependencies and configuration - Agent_Classic_Test
**Objective:** 替换 Jakarta 依赖为 javax，移除 JPMS 相关配置。
**Output:** logback-classic-blackbox POM 兼容 Java 8。
**Guidance:** 此模块声明了 `jakarta.servlet-api 6.0.0`（高于 parent 的 5.0），需替换。注意 `useModulePath=true` 也需移除。**Depends on: Task 4.9 Output by Agent_Classic_SLF4J**

- 将 `jakarta.servlet:jakarta.servlet-api 6.0.0` 替换为 `javax.servlet:javax.servlet-api 4.0.1`
- 将 `jakarta.mail:jakarta.mail-api` 替换为 `javax.mail:javax.mail-api`（或 `com.sun.mail:javax.mail`）
- 移除 surefire 配置中的 `useModulePath=true`

### Task 5.2 – Replace Jakarta imports in logback-classic-blackbox test code - Agent_Classic_Test
**Objective:** 将黑盒测试代码中的 Jakarta 引用替换为 javax。
**Output:** logback-classic-blackbox 测试代码使用 javax 命名空间。
**Guidance:** 涉及 4 个测试文件。**Depends on: Task 5.1 Output**

- 在 `SMTPAppender_GreenTest.java` 中将 `jakarta.mail.*` → `javax.mail.*`
- 在 `ConfigFileServlet.java`、`ConfigEmbeddedJetty.java`、`ConfigurationWatchListTest.java` 中将 `jakarta.servlet.*` → `javax.servlet.*`

### Task 5.3 – Verify logback-classic-blackbox tests pass under Java 8 - Agent_Classic_Test
**Objective:** 在 Java 8 环境下运行全部 25 个黑盒测试并修复所有失败。
**Output:** logback-classic-blackbox 所有测试在 Java 8 下通过。
**Guidance:** Jetty 12.x 需要 Java 11+，可能需要降级到 Jetty 9.4.x（最后支持 Java 8 和 javax.servlet 的版本）。**Depends on: Task 5.2 Output**

1. 使用 Java 8 环境执行 `mvn test -pl logback-classic-blackbox`
2. 如果 Jetty 12.x 不兼容 Java 8，将 Jetty 降级到 9.4.x
3. 分析并修复其他测试失败
4. 重新运行直到全部通过

## Phase 6: Integration Verification

### Task 6.1 – Full project build and integration test under Java 8 - Agent_Classic_Test
**Objective:** 对所有改造模块执行联合构建和测试，验证整体兼容性。
**Output:** 所有改造模块在 Java 8 下编译通过、测试全部通过，版本号正确。
**Guidance:** 使用 `-pl` 指定改造范围内的模块。如有跨模块接口不匹配需回溯修复。**Depends on: Task 5.3 Output**

1. 使用 Java 8 环境执行 `mvn test -pl logback-core,logback-core-blackbox,logback-classic,logback-classic-blackbox`
2. 验证所有模块编译通过且测试全部通过
3. 如有跨模块集成问题（接口不匹配、依赖版本冲突），定位并修复
4. 确认最终构建产物版本号为 `1.5.33-slf4j1x-jre8`
