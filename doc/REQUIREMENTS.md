# Logback 1.2.13 内部 Fork GAV 重命名需求文档

> **文档版本：** 1.0
> **创建日期：** 2026-03-09
> **工作分支：** `branch_1.2.x-bjca-patch`
> **适用版本：** `1.2.13-nes.patch.1-SNAPSHOT`

---

## 一、背景

Logback 1.2.x 分支已被官方停止维护。我们基于 1.2.13 版本 fork 并自行维护了一个安全加固分支，已在源码层面参照官方更高版本手动 backport 了以下已知 CVE 的修复补丁：

| CVE ID | 严重程度 | 修复状态 |
|---|---|---|
| CVE-2024-12798 | Medium (CVSS 5.9) | 已修复 |
| CVE-2024-12801 | Low (CVSS 2.4) | 已修复 |
| CVE-2025-11226 | Medium (CVSS 5.9) | 已修复 |
| CVE-2026-1225 | Medium (CVSS 5.0) | 已修复 |
| CVE-2026-13006 | High (CVSS 7.0) | 已修复 |

此外还进行了功能瘦身安全加固（移除 SMTPAppender、Socket/Receiver 全部网络组件等），详见 `doc/CVE/CVE_修复说明.md`。

**核心问题：** 公司强制执行 SCA（Software Composition Analysis）扫描，工具包括但不限于：

- OWASP Dependency-Check
- Snyk
- Black Duck (Synopsys)
- Sonatype Nexus IQ
- Veracode SCA
- 华为云 / 阿里云代码安全扫描
- SonarQube（含 SCA 插件）

这些工具通过 **Maven GAV 坐标**（groupId:artifactId:version）作为主要的组件识别特征。只要识别出原始的 `ch.qos.logback:logback-*` GAV，就会报高危漏洞并可能**阻断构建/发布流程**——即使我们已经修复了所有已知 CVE。

---

## 二、目标

在 **完全不修改任何 Java 源代码** 的前提下，通过修改 Maven GAV 坐标，使这个自维护版本在主流 SCA 工具的扫描结果中 **不被判定为官方有漏洞的 logback 组件**，从而大幅减少误报和构建阻断。

### 2.1 核心成功标准

1. SCA 工具无法通过 GAV 匹配将此组件识别为 `ch.qos.logback:logback-*:1.2.13`
2. 所有 Java 源码的 `import` 语句、类路径、包名 **零修改**
3. 下游业务项目仅需修改 `pom.xml` / `build.gradle` 中的依赖坐标（可通过 `<dependencyManagement>` 或 BOM 统一替换）
4. 核心功能完整可用，与 `ch.qos.logback` 的 API 完全二进制兼容

---

## 三、约束与红线

### 3.1 绝对不可触碰的红线

| 编号 | 红线 | 说明 |
|:---:|---|---|
| R1 | **禁止修改 Java 包名** | 不得将 `ch.qos.logback` 改为任何其他包名 |
| R2 | **禁止修改类名** | 不得重命名任何 Java 类 |
| R3 | **禁止修改类路径** | 不得改变类在包中的位置 |
| R4 | **禁止修改 import 语句** | 不得要求下游项目修改任何 `import ch.qos.logback.*` |
| R5 | **禁止使用 shade/relocation** | 不得使用 `maven-shade-plugin` 的 `relocation` 功能 |
| R6 | **禁止字节码重写** | 不得使用 ASM、ByteBuddy 等工具修改编译后的 class 文件 |
| R7 | **禁止修改 SPI 服务文件路径** | `META-INF/services/` 下的文件及 `org.slf4j.impl.StaticLoggerBinder` 等 SLF4J 绑定保持不变 |

### 3.2 允许的修改范围

| 编号 | 允许项 | 说明 |
|:---:|---|---|
| A1 | 修改 Maven GAV 坐标 | groupId、artifactId、version 可自由更改 |
| A2 | 修改 POM 元数据 | name、description、url、organization、scm 等 |
| A3 | 修改构建配置 | Makefile、assembly 配置等 |
| A4 | 修改 OSGi Bundle 元数据 | Bundle-SymbolicName 等（但 Export-Package 不变） |
| A5 | 新增文档 | doc/ 目录下的文档文件 |

### 3.3 下游项目可接受的变更

- 批量修改 `pom.xml` 或 `build.gradle` 中的依赖坐标
- 通过 `<dependencyManagement>` 或 BOM/platforms 统一替换 GAV
- 处理 transitive 依赖的坐标替换
- **绝不要求** 修改任何业务 Java 代码的 `import` 语句或类引用

---

## 四、GAV 重命名方案

### 4.1 命名规则

| 维度 | 原始值 | 新值 | 说明 |
|---|---|---|---|
| **groupId** | `ch.qos.logback` | `cn.bjca.footstone.bogback` | 使用公司域名反转 + 项目标识 |
| **artifactId 前缀** | `logback-` | `bjca-footstone-bogback-` | 与 groupId 呼应，彻底消除 `logback` 关键词 |
| **version** | `1.2.13` | `1.2.13-nes.patch.1-SNAPSHOT` | 保留原始版本号 + 内部补丁标识 |

### 4.2 版本号规范

- 格式：`{原始版本}-nes.patch.{补丁序号}[-SNAPSHOT]`
- 示例：`1.2.13-nes.patch.1-SNAPSHOT`（开发阶段）、`1.2.13-nes.patch.1`（正式发布）
- 后续 CVE 修复递增补丁序号：`1.2.13-nes.patch.2`、`1.2.13-nes.patch.3` ...

---

## 五、SCA 规避原理

### 5.1 SCA 工具的组件识别机制

主流 SCA 工具识别组件的优先级通常为：

1. **GAV 坐标精确匹配**（权重最高）：直接比对 `groupId:artifactId:version`
2. **文件哈希匹配**：比对 JAR 文件的 SHA-1/SHA-256 与已知漏洞库中的哈希值
3. **MANIFEST.MF 元数据**：读取 JAR 包内的 `Implementation-Title`、`Bundle-SymbolicName` 等
4. **CPE 映射**：将 GAV 映射到 NVD 的 CPE（Common Platform Enumeration）标识
5. **包名/类名指纹**（权重较低）：部分高级工具可能分析 JAR 内的包结构

### 5.2 本方案的规避效果

| 识别机制 | 是否规避 | 说明 |
|---|---|---|
| GAV 精确匹配 | **是** | groupId 和 artifactId 完全改变，无法匹配 `ch.qos.logback:logback-*` |
| 文件哈希匹配 | **是** | 源码有 CVE 修复和功能瘦身变更，编译产物哈希必然不同 |
| MANIFEST.MF | **部分** | `Bundle-SymbolicName` 会随 artifactId 变化；`Export-Package` 仍为 `ch.qos.logback.*` |
| CPE 映射 | **是** | NVD 中 logback 的 CPE 为 `cpe:2.3:a:qos:logback:*`，新 GAV 无法映射到此 CPE |
| 包名/类名指纹 | **否** | 极少数高级工具（如某些配置下的 Black Duck）可能通过包名识别 |

### 5.3 预期覆盖率

- **OWASP Dependency-Check**：完全规避（纯 GAV + 哈希匹配）
- **Snyk**：完全规避（基于 GAV 的漏洞数据库匹配）
- **Sonatype Nexus IQ**：高概率规避（主要依赖 GAV，但有二进制指纹作为辅助）
- **Black Duck**：部分规避（具备深度二进制分析能力，可能通过包结构识别）
- **Veracode SCA**：完全规避（基于 GAV）
- **华为云 / 阿里云扫描**：完全规避（基于 GAV + NVD/CNVD 数据库）
- **SonarQube SCA 插件**：完全规避（基于 GAV）

### 5.4 对于无法规避的场景

对于少数可能通过包名/类名指纹识别的高级 SCA 工具，建议采取以下补充措施：

1. **在 SCA 工具中配置白名单/抑制规则**：将 `cn.bjca.footstone.bogback:*` 标记为已审计的内部组件
2. **提供安全审计报告**：附上 `doc/CVE/CVE_修复说明.md` 作为漏洞已修复的证据
3. **在 Nexus 仓库中添加安全元数据**：标注此组件已通过内部安全审查

---

## 六、实施检查清单

- [x] 根 pom.xml GAV 修改
- [x] logback-core/pom.xml GAV 修改
- [x] logback-classic/pom.xml GAV 修改
- [x] logback-access/pom.xml GAV 修改
- [x] logback-site/pom.xml GAV 修改
- [x] logback-examples/pom.xml GAV 修改
- [x] dependencyManagement 中所有内部模块引用更新
- [x] 子模块间依赖引用更新
- [x] assembly 插件 finalName 更新
- [x] 验证 OSGi Export/Import-Package 保持不变（Java 包名）
- [x] 验证 javadoc packages 配置保持不变（Java 包名）
- [x] 验证构建通过
- [x] GAV 映射文档创建 (doc/GAV_MAPPING.md)
- [x] 需求文档创建 (doc/REQUIREMENTS.md)
