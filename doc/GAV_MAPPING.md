# Logback GAV 坐标映射表

> **文档版本：** 1.0
> **创建日期：** 2026-03-09
> **适用版本：** `1.2.13-nes.patch.2-SNAPSHOT`（开发中）
> **最新正式发布：** `1.2.13-nes.patch.1`（2026-06-25，Nexus releases）

本文档记录所有从官方 logback GAV 到 BJCA 内部 fork 版本的完整映射关系，供下游团队在 `<dependencyManagement>` / BOM / Gradle platforms 中统一替换使用。

---

## 一、GAV 映射总表

### 1.1 核心模块（下游项目通常需要引用的）

| 原始 groupId | 原始 artifactId | 原始 version | 新 groupId | 新 artifactId | 新 version |
|---|---|---|---|---|---|
| `ch.qos.logback` | `logback-core` | `1.2.13` | `cn.bjca.footstone.bogback` | `bjca-footstone-bogback-core` | `1.2.13-nes.patch.2-SNAPSHOT` |
| `ch.qos.logback` | `logback-classic` | `1.2.13` | `cn.bjca.footstone.bogback` | `bjca-footstone-bogback-classic` | `1.2.13-nes.patch.2-SNAPSHOT` |
| `ch.qos.logback` | `logback-access` | `1.2.13` | `cn.bjca.footstone.bogback` | `bjca-footstone-bogback-access` | `1.2.13-nes.patch.2-SNAPSHOT` |

### 1.2 非核心模块（一般不需要下游引用）

| 原始 groupId | 原始 artifactId | 新 groupId | 新 artifactId | 新 version |
|---|---|---|---|---|
| `ch.qos.logback` | `logback-parent` | `cn.bjca.footstone.bogback` | `bjca-footstone-bogback-parent` | `1.2.13-nes.patch.2-SNAPSHOT` |
| `ch.qos.logback` | `logback-site` | `cn.bjca.footstone.bogback` | `bjca-footstone-bogback-site` | `1.2.13-nes.patch.2-SNAPSHOT` |
| `ch.qos.logback` | `logback-examples` | `cn.bjca.footstone.bogback` | `bjca-footstone-bogback-examples` | `1.2.13-nes.patch.2-SNAPSHOT` |

---

## 二、下游 Maven 项目替换指南

### 2.1 方式一：使用 dependencyManagement 统一管理（推荐）

在父 POM 或项目根 POM 的 `<dependencyManagement>` 中声明新坐标：

```xml
<dependencyManagement>
    <dependencies>
        <!-- logback-classic（最常用，自动传递依赖 logback-core） -->
        <dependency>
            <groupId>cn.bjca.footstone.bogback</groupId>
            <artifactId>bjca-footstone-bogback-classic</artifactId>
            <version>1.2.13-nes.patch.2-SNAPSHOT</version>
        </dependency>

        <!-- logback-core（通常由 classic 传递引入，显式声明用于版本锁定） -->
        <dependency>
            <groupId>cn.bjca.footstone.bogback</groupId>
            <artifactId>bjca-footstone-bogback-core</artifactId>
            <version>1.2.13-nes.patch.2-SNAPSHOT</version>
        </dependency>

        <!-- logback-access（仅 Servlet 容器访问日志场景需要） -->
        <dependency>
            <groupId>cn.bjca.footstone.bogback</groupId>
            <artifactId>bjca-footstone-bogback-access</artifactId>
            <version>1.2.13-nes.patch.2-SNAPSHOT</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

然后在子模块中直接引用（无需重复写 version）：

```xml
<dependencies>
    <dependency>
        <groupId>cn.bjca.footstone.bogback</groupId>
        <artifactId>bjca-footstone-bogback-classic</artifactId>
    </dependency>
</dependencies>
```

### 2.2 方式二：排除旧依赖 + 引入新依赖

当 transitive 依赖拉入了旧版 logback 时，需要先排除再替换：

```xml
<!-- 排除某个依赖传递引入的旧版 logback -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <version>x.y.z</version>
    <exclusions>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </exclusion>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 显式引入新版 -->
<dependency>
    <groupId>cn.bjca.footstone.bogback</groupId>
    <artifactId>bjca-footstone-bogback-classic</artifactId>
    <version>1.2.13-nes.patch.2-SNAPSHOT</version>
</dependency>
```

### 2.3 方式三：全局排除（Maven Enforcer 辅助）

在根 POM 中通过 `maven-enforcer-plugin` 禁止旧版 logback 出现在依赖树中：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-enforcer-plugin</artifactId>
            <version>3.4.1</version>
            <executions>
                <execution>
                    <id>ban-old-logback</id>
                    <goals>
                        <goal>enforce</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <bannedDependencies>
                                <excludes>
                                    <exclude>ch.qos.logback:logback-core</exclude>
                                    <exclude>ch.qos.logback:logback-classic</exclude>
                                    <exclude>ch.qos.logback:logback-access</exclude>
                                </excludes>
                                <message>
                                    请使用 cn.bjca.footstone.bogback 替代 ch.qos.logback，
                                    详见 doc/GAV_MAPPING.md
                                </message>
                            </bannedDependencies>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 三、下游 Gradle 项目替换指南

### 3.1 直接替换

```groovy
dependencies {
    // 替换前
    // implementation 'ch.qos.logback:logback-classic:1.2.13'

    // 替换后
    implementation 'cn.bjca.footstone.bogback:bjca-footstone-bogback-classic:1.2.13-nes.patch.2-SNAPSHOT'
}
```

### 3.2 使用 Dependency Substitution 全局替换

```groovy
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute module('ch.qos.logback:logback-core') using module('cn.bjca.footstone.bogback:bjca-footstone-bogback-core:1.2.13-nes.patch.2-SNAPSHOT')
        substitute module('ch.qos.logback:logback-classic') using module('cn.bjca.footstone.bogback:bjca-footstone-bogback-classic:1.2.13-nes.patch.2-SNAPSHOT')
        substitute module('ch.qos.logback:logback-access') using module('cn.bjca.footstone.bogback:bjca-footstone-bogback-access:1.2.13-nes.patch.2-SNAPSHOT')
    }
}
```

### 3.3 使用 Gradle Platform / BOM

```groovy
dependencies {
    // 如果发布了 BOM
    implementation platform('cn.bjca.footstone.bogback:bjca-footstone-bogback-parent:1.2.13-nes.patch.2-SNAPSHOT')
    implementation 'cn.bjca.footstone.bogback:bjca-footstone-bogback-classic'
}
```

---

## 四、Spring Boot 项目特别说明

Spring Boot Starter 默认通过 `spring-boot-starter-logging` 传递依赖 `ch.qos.logback:logback-classic`。替换方式：

```xml
<!-- 在 spring-boot-starter 中排除默认 logback -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <exclusions>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </exclusion>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 引入 BJCA 内部 fork 版本 -->
<dependency>
    <groupId>cn.bjca.footstone.bogback</groupId>
    <artifactId>bjca-footstone-bogback-classic</artifactId>
    <version>1.2.13-nes.patch.2-SNAPSHOT</version>
</dependency>
```

> **注意：** 由于 Java 包名和 SLF4J 绑定机制（`META-INF/services/org.slf4j.spi.SLF4JServiceProvider` 和 `org.slf4j.impl.StaticLoggerBinder`）均未修改，Spring Boot 的自动配置和日志系统初始化不受任何影响。

---

## 五、不变项说明

以下内容在 GAV 重命名后 **保持不变**，无需下游项目做任何调整：

| 项目 | 值 | 说明 |
|---|---|---|
| Java 包名 | `ch.qos.logback.core.*`、`ch.qos.logback.classic.*`、`ch.qos.logback.access.*` | 所有 import 语句不变 |
| SLF4J 绑定 | `org.slf4j.impl.StaticLoggerBinder` | SLF4J 1.7.x 静态绑定机制不变 |
| logback 配置文件 | `logback.xml`、`logback-test.xml`、`logback-spring.xml` | 文件名和配置语法不变 |
| logback 配置内容 | `<appender class="ch.qos.logback.classic.xxx">` | 配置文件中的类引用不变 |
| API 调用 | `LoggerFactory.getLogger()`、`Logger.info()` 等 | 所有 SLF4J API 调用不变 |

---

## 六、内部 Nexus 仓库配置

确保内部 Nexus 仓库配置了以下 SNAPSHOT 仓库地址（在 `settings.xml` 或 POM 中）：

```xml
<repositories>
    <repository>
        <id>nexus-snapshots</id>
        <name>BJCA Nexus Snapshots</name>
        <url>${nexusSnapshotUrl}</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

正式发布时（去掉 `-SNAPSHOT` 后缀），使用 release 仓库：

```xml
<repositories>
    <repository>
        <id>nexus-releases</id>
        <name>BJCA Nexus Releases</name>
        <url>${nexusReleaseUrl}</url>
    </repository>
</repositories>
```
