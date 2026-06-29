# Logback 1.2.13 CVE 安全补丁修复说明

## 项目概述

本文档记录了针对 logback 1.2.13 版本的 CVE 安全漏洞修复工作。logback 1.2.x 分支已不再由官方维护，但由于生产环境兼容性要求，我们基于 1.2.13 版本进行安全补丁回移（backport），修复了 5 个已知的安全漏洞。

- **基线版本：** logback 1.2.13
- **修复版本：** `1.2.13-nes.patch.1`
- **修复日期：** 2026-02-28（初始补丁）；2026-06-25（CVE-2026-13006）；**正式发布：** 2026-06-25
- **工作分支：** `branch_1.2.x-bjca-patch`

---

## CVE 总览表

| CVE ID | 漏洞类型 | 严重程度 | CVSS 评分 | 状态 |
|---|---|---|---|---|
| CVE-2024-12798 | 任意代码执行（ACE） | Medium | 5.9 (CVSS v4.0) | 已修复 |
| CVE-2024-12801 | 服务端请求伪造（SSRF） | Low | 2.4 (CVSS v4.0) | 已修复 |
| CVE-2025-11226 | 任意代码执行（ACE） | Medium | 5.9 (CVSS v4.0) | 已修复 |
| CVE-2026-1225 | 任意代码执行（ACE） | Medium | 5.0 (CVSS v3.1) | 已修复 |
| CVE-2026-13006 | 任意代码执行（ACE） | High | 7.0 (CVSS v4.0) | 已修复 |

---

## CVE-2024-12798 — 通过 JaninoEventEvaluator 执行任意代码

### CVE 基本信息

| 字段 | 值 |
|---|---|
| **CVE ID** | CVE-2024-12798 |
| **CVSS 评分** | 5.9 (CVSS v4.0, Medium) |
| **严重程度** | 中等 (Medium) |
| **CWE 分类** | CWE-917: 表达式语言中特殊元素的不当处理 |
| **发现者** | 7asecurity |
| **公开日期** | 2024-12-19 |

### 漏洞描述

**影响组件：**
- `ch.qos.logback.classic.boolex.JaninoEventEvaluator` (logback-classic)
- `ch.qos.logback.core.boolex.JaninoEventEvaluatorBase` (logback-core)
- `ch.qos.logback.access.boolex.JaninoEventEvaluator` (logback-access)

**攻击向量：**
攻击者如果能够写入 logback 配置文件（或注入恶意的 `LOGBACK_CONFIGURATION_FILE` 环境变量），可以通过 `JaninoEventEvaluator` 扩展执行任意 Java 代码。前提条件是 Janino 库必须在类路径上。

**利用条件：**
- 攻击者需要对 logback 配置文件具有写权限，或能够设置 `LOGBACK_CONFIGURATION_FILE` 环境变量
- Janino 库必须存在于应用类路径中
- 属于本地攻击向量（AV:L）

### 官方修复方案

官方在 1.3.x 分支（1.3.15 版本）采用了**完全移除 JaninoEventEvaluator 类**的策略。具体修改如下：

- **Commit `c17e58838`**（logback-classic/logback-core，11 文件，+42/-664）：
  - 删除 `JaninoEventEvaluator.java`（classic）和 `JaninoEventEvaluatorBase.java`（core）
  - 修改 `ClassicEvaluatorAction.java`，移除 `JaninoEventEvaluator` 引用
  - 修改 `LogbackClassicDefaultNestedComponentRules.java`，移除默认映射
  - 删除相关测试类
- **Commit `b44b940cc`**（logback-access，5 文件，+1/-167）：
  - 删除 `JaninoEventEvaluator.java`（access）
  - 修改 `AccessEvaluatorAction.java` 和 `LogbackAccessDefaultNestedComponentRegistryRules.java`

**技术思路：** `JaninoEventEvaluator` 允许在 logback 配置中嵌入并执行任意 Java 表达式，这是一个无法通过输入过滤安全解决的设计缺陷，因此官方选择完全移除该功能。

### 官方修复 Commit

| 所属分支 | 修复版本 | Commit Hash |
|---|---|---|
| 1.3.x | 1.3.15 | `c17e5883845e5bc4dec49b3fe74f744e0e574a2b`（classic/core） |
| 1.3.x | 1.3.15 | `b44b940cc7d4839e06e31a7d60dca174b99c1aa5`（access） |
| 1.5.x | 1.5.13 | `32638aa7e99c0135cb1b81806ed05352e6bfe27f` |

### 本次补丁修复

**分支：** `branch_1.2.x-bjca-patch`
**Commit：** `f5dabd9fc`

**修改的文件列表：**

| 操作 | 文件路径 |
|---|---|
| 删除 | `logback-classic/src/main/java/.../classic/boolex/JaninoEventEvaluator.java` |
| 删除 | `logback-core/src/main/java/.../core/boolex/JaninoEventEvaluatorBase.java` |
| 删除 | `logback-access/src/main/java/.../access/boolex/JaninoEventEvaluator.java` |
| 删除 | `logback-classic/src/test/.../classic/boolex/JaninoEventEvaluatorTest.java` |
| 删除 | `logback-classic/src/test/.../classic/joran/EvaluatorJoranTest.java` |
| 删除 | `logback-access/src/test/.../access/boolex/JaninoEventEvaluatorTest.java` |
| 修改 | `logback-classic/.../joran/action/EvaluatorAction.java` — 移除 import，`defaultClassName()` 返回 null |
| 修改 | `logback-access/.../joran/action/EvaluatorAction.java` — 移除 import，`defaultClassName()` 返回 null |
| 修改 | `logback-classic/.../util/DefaultNestedComponentRules.java` — 移除 evaluator 映射 |
| 修改 | `logback-access/.../joran/JoranConfigurator.java` — 移除 evaluator 映射 |
| 修改 | `logback-core/.../net/ssl/SSLParametersConfiguration.java` — 移除无关的 Janino import |
| 修改 | `logback-classic/src/test/.../boolex/PackageTest.java` — 移除测试引用 |
| 修改 | `logback-access/src/test/.../boolex/PackageTest.java` — 清空测试套件 |
| 修改 | `logback-classic/src/test/.../joran/PackageTest.java` — 移除测试引用 |
| 修改 | `logback-classic/src/test/.../joran/JoranConfiguratorTest.java` — 移除 `testEvaluatorFilter` 测试方法 |

**与官方修复的差异说明：**
- 1.2.x 中使用 `EvaluatorAction`（继承 `AbstractEventEvaluatorAction`），而 1.3.x 使用 `ClassicEvaluatorAction`（继承 `EventEvaluatorAction`），类名和继承结构不同但修改逻辑一致
- 额外清理了 `SSLParametersConfiguration.java` 中无关的 Janino import（与 1.3.x 修复一致）
- 额外清理了 `JoranConfiguratorTest.testEvaluatorFilter` 和 `joran/PackageTest.java`（研究报告中未提及但实际修复需要）
- 注意：Janino 库依赖保留在 pom.xml 中，因为 `<if>` 条件处理仍然需要（由 CVE-2025-11226 单独处理）

### 验证与测试步骤

1. 确认 `JaninoEventEvaluator`、`JaninoEventEvaluatorBase`（core）和 `JaninoEventEvaluator`（access）三个源文件已从项目中删除
2. 在 logback XML 配置中使用 `<evaluator>` 元素而不指定 class 属性，应产生错误日志而非实例化 `JaninoEventEvaluator`
3. 编译项目确认无残留引用：`mvn compile -pl logback-classic,logback-core,logback-access`
4. 运行相关模块测试：`mvn test -pl logback-classic,logback-core`

---

## CVE-2024-12801 — 通过 SaxEventRecorder 的服务端请求伪造（SSRF）

### CVE 基本信息

| 字段 | 值 |
|---|---|
| **CVE ID** | CVE-2024-12801 |
| **CVSS 评分** | 2.4 (CVSS v4.0, Low) |
| **严重程度** | 低 (Low) |
| **CWE 分类** | CWE-918: 服务端请求伪造 (SSRF) |
| **发现者** | 7asecurity |
| **公开日期** | 2024-12-19 |

### 漏洞描述

**影响组件：**
- `ch.qos.logback.core.joran.event.SaxEventRecorder` (logback-core)

**攻击向量：**
攻击者如果能够修改 logback 的 XML 配置文件，可以通过注入 `DOCTYPE` 声明并引用外部实体（如 `<!DOCTYPE test SYSTEM "http://internal-service/">`）来伪造服务端请求。`SaxEventRecorder` 未禁用外部实体解析，XML 解析器会尝试获取被引用的 URL，从而实现 SSRF 攻击。

**利用条件：**
- 攻击者需要对 logback XML 配置文件具有写权限
- 属于本地攻击向量（AV:L），需要较高的前置条件（AT:P）

### 官方修复方案

官方在 1.3.x 分支（1.3.15 版本）通过**覆写 `resolveEntity()` 方法阻止外部 DTD 解析**来修复此漏洞。核心修改如下：

在 `SaxEventRecorder` 中添加 `resolveEntity()` 覆写方法：
```java
@Override
public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException {
    addWarn("Document Type Declaration (DOCTYPE) with external file reference is");
    addWarn("disallowed to prevent Server-Side Request Forgery (SSRF) attacks.");
    addWarn("returning contents of SYSTEM " + systemId + " as a white space");
    return new InputSource(new ByteArrayInputStream(" ".getBytes()));
}
```

**技术思路：** 当 XML 解析器遇到 DOCTYPE 中的外部实体引用时，会调用 `resolveEntity()` 方法。覆写此方法使其返回空白内容（而非实际请求外部 URL），从而阻止 SSRF 攻击，同时不影响正常的配置文件解析。

### 官方修复 Commit

| 所属分支 | 修复版本 | Commit Hash |
|---|---|---|
| 1.3.x | 1.3.15 | `2863a4974a3649b5b00d4a529ee6ff2063470f35` |
| 1.5.x | 1.5.13 | `5f05041cba4c4ac0a62748c5c527a2da48999f2d` |

### 本次补丁修复

**分支：** `branch_1.2.x-bjca-patch`
**Commit：** `83cbaec15`

**修改的文件列表：**

| 操作 | 文件路径 |
|---|---|
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/joran/event/SaxEventRecorder.java` |

具体修改：
- 添加 `import java.io.ByteArrayInputStream`
- 添加 `resolveEntity(String publicId, String systemId)` 覆写方法（含 Javadoc）

**与官方修复的差异说明：**
- 1.2.x 的 `SaxEventRecorder` 自带便捷的 `addWarn()` 方法（内部委托给 `cai.addWarn()`），因此方法体与官方修复完全一致
- 无结构性差异，此修复是最直接的回移

### 验证与测试步骤

1. 创建包含外部 DOCTYPE 声明的测试配置文件：`<!DOCTYPE test SYSTEM "http://example.com/malicious">`
2. 使用该配置文件初始化 logback，确认不会发起外部 HTTP 请求
3. 检查日志输出，应包含 SSRF 防护警告信息
4. 验证正常的 logback 配置文件（不含 DOCTYPE）解析不受影响

---

## CVE-2025-11226 — 通过 `<if>` 条件属性执行任意代码

### CVE 基本信息

| 字段 | 值 |
|---|---|
| **CVE ID** | CVE-2025-11226 |
| **CVSS 评分** | 5.9 (CVSS v4.0, Medium) |
| **严重程度** | 中等 (Medium) |
| **CWE 分类** | CWE-454: 外部初始化受信任的变量或数据存储 |
| **发现者** | Heihu577 |
| **公开日期** | 2025-10-01 |

### 漏洞描述

**影响组件：**
- `ch.qos.logback.core.joran.conditional.IfAction` (1.2.x)
- `ch.qos.logback.core.joran.conditional.PropertyEvalScriptBuilder`

**攻击向量：**
攻击者可以在 logback XML 配置中 `<if>` 元素的 `condition` 属性中使用 Java `new` 操作符来执行任意代码。例如：`<if condition='new java.lang.ProcessBuilder("cmd").start() != null'>`。基于 Janino 的条件评估器未限制 `new` 操作符的使用。

**利用条件：**
- Janino 库**和** Spring Framework 必须同时存在于类路径上
- 攻击者需要对配置文件具有写权限，或能够注入环境变量

### 官方修复方案

官方在 1.3.x 分支（1.3.16 版本）通过**在条件属性中禁止 `new` 操作符**来修复此漏洞。核心修改在 `IfModelHandler.java` 中：

```java
// 新增常量
public static final String NEW_OPERATOR_DISALLOWED_MSG =
    "The 'condition' attribute may not contain the 'new' operator.";

// 在条件评估前添加检查
if (hasNew(conditionStr)) {
    addError(NEW_OPERATOR_DISALLOWED_MSG);
    addError(NEW_OPERATOR_DISALLOWED_SEE);
    return;
}

// 辅助方法
private boolean hasNew(String conditionStr) {
    return conditionStr.contains("new ");
}
```

**技术思路：** 通过简单的字符串检测（检查是否包含 `"new "`），在 Janino 编译和执行条件表达式之前进行拦截。当检测到 `new` 操作符时，记录错误并立即返回，阻止条件表达式的编译执行。这是一种轻量有效的防护措施。

### 官方修复 Commit

| 所属分支 | 修复版本 | Commit Hash |
|---|---|---|
| 1.3.x | 1.3.16 | `e3aa0f440cf7a3b98f16fbb21bcea83f72be71e6` |
| 1.5.x | 1.5.19 | `e572d4f87f06674788eb3ca7148e8d1dffc615fa` |

### 本次补丁修复

**分支：** `branch_1.2.x-bjca-patch`
**Commit：** `ac2ae32a1`

**修改的文件列表：**

| 操作 | 文件路径 |
|---|---|
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/joran/conditional/IfAction.java` (+15 行) |

具体修改：
- 添加 `NEW_OPERATOR_DISALLOWED_MSG` 和 `NEW_OPERATOR_DISALLOWED_SEE` 两个公共静态常量
- 在 `begin()` 方法中 `OptionHelper.substVars()` 之后、`pesb.build()` 之前插入 `hasNew()` 检查
- 添加 `hasNew(String)` 私有方法

**与官方修复的差异说明：**
- 1.2.x 使用 `IfAction`（基于 SAX 事件的 Joran Action），而 1.3.x 使用 `IfModelHandler`（基于模型的处理器），这是不同架构下的对应组件
- 修改逻辑完全一致：在条件字符串经过变量替换后、在 Janino 编译前进行 `new` 操作符检查
- `addError()` 方法在两个版本中均可从父类获得，用法一致

### 验证与测试步骤

1. 创建包含 `<if condition='new java.lang.ProcessBuilder("echo test").start() != null'>` 的测试配置文件
2. 加载该配置，确认条件不会被执行，应在日志中看到 `NEW_OPERATOR_DISALLOWED_MSG` 错误信息
3. 验证正常的 `<if condition='property("xxx").contains("yyy")'>` 条件仍能正常工作
4. 确认包含 `new` 但不在条件属性中的配置元素不受影响

---

## CVE-2026-13006 — 通过 Unicode 转义绕过 `<if>` 条件 `new` 操作符限制

### CVE 基本信息

| 字段 | 值 |
|---|---|
| **CVE ID** | CVE-2026-13006 |
| **CVSS 评分** | 7.0 (CVSS v4.0, High) |
| **严重程度** | 高 (High) |
| **CWE 分类** | CWE-20: 输入验证不当 |
| **发现者** | IcySun (icysun@qq.com) |
| **公开日期** | 2026-06-24 |

### 漏洞描述

**影响组件：**
- `ch.qos.logback.core.joran.conditional.IfAction` (1.2.x)
- `ch.qos.logback.core.joran.conditional.PropertyEvalScriptBuilder`

**攻击向量：**
CVE-2025-11226 的修复通过检测条件字符串中的 `"new "` 字面量来拦截 `new` 操作符。攻击者可以使用 Java Unicode 转义序列（`\u` 或 `\U`）拼出 `new` 关键字，从而绕过该检测。例如：`<if condition='n\u0067w Integer(1).equals(1)'>`（其中 `\u0067` 解码为字母 `g`），Janino 编译后会等价于 `new Integer(1).equals(1)`，从而执行任意代码。

**利用条件：**
- Janino 库必须存在于类路径上
- 攻击者需要对配置文件具有写权限，或能够注入环境变量
- 目标版本已包含 CVE-2025-11226 修复但仍未包含本 CVE 修复

### 官方修复方案

官方在 1.5.x 分支（1.5.35 版本）通过**在条件属性中拒绝 Unicode 转义序列**来修复此漏洞。核心修改如下：

1. **`OptionHelper`** — 新增 `containsUnicodeEscape()` 方法：
   ```java
   public static boolean containsUnicodeEscape(String value) {
       return value.contains("\\u") || value.contains("\\U");
   }
   ```

2. **`IfModelHandler`** — 在 `hasNew()` 检查之前插入 Unicode 转义检测：
   ```java
   if (OptionHelper.containsUnicodeEscape(conditionStr)) {
       addError(NEW_OPERATOR_DISALLOWED_MSG);
       addError(NEW_OPERATOR_DISALLOWED_SEE);
       return;
   }
   ```

**技术思路：** 在 Janino 编译条件表达式之前，检测条件字符串是否包含 `\u` 或 `\U` 转义序列。若存在则拒绝处理并记录错误，防止通过 Unicode 转义绕过 `new` 操作符禁令。

### 官方修复 Commit

| 所属分支 | 修复版本 | Commit Hash |
|---|---|---|
| 1.5.x | 1.5.35 | `347efc8ec3f10defafd0cf6d4b9a0c81b3320c3a` |

### 本次补丁修复

**分支：** `branch_1.2.x-bjca-patch`

**修改的文件列表：**

| 操作 | 文件路径 |
|---|---|
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/util/OptionHelper.java` (+4 行) |
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/joran/conditional/IfAction.java` (+6 行) |
| 新增 | `logback-core/src/test/input/joran/conditional/ifNew.xml` |
| 新增 | `logback-core/src/test/input/joran/conditional/ifNewSlashU.xml` |
| 修改 | `logback-core/src/test/java/ch/qos/logback/core/joran/conditional/IfThenElseTest.java` |
| 修改 | `logback-core/src/test/java/ch/qos/logback/core/util/OptionHelperTest.java` |

具体修改：
- **`OptionHelper`**：添加 `containsUnicodeEscape(String)` 方法
- **`IfAction`**：在 `substVars()` 之后、`hasNew()` 之前插入 Unicode 转义检测，使用与 CVE-2025-11226 相同的错误消息常量
- **测试**：添加 `ifWithNew` 和 `ifWithNewSlashU` 测试用例，验证 `new` 字面量和 `\u` 转义绕过均被拦截

**与官方修复的差异说明：**
- 1.2.x 使用 `IfAction`（SAX 事件 Joran Action），1.5.x 使用 `IfModelHandler`（模型处理器），对应组件不同但逻辑一致
- 1.2.x 不引入官方 1.5.x 新增的 `UNICODE_DISALLOWED_MSG` 常量，复用 `NEW_OPERATOR_DISALLOWED_MSG`（与官方 1.5.35 在 `IfModelHandler` 中的实际行为一致）

### 验证与测试步骤

1. 创建包含 `<if condition='n\u0067w Integer(1).equals(1)'>` 的测试配置文件
2. 加载该配置，确认条件不会被执行，应在日志中看到 `NEW_OPERATOR_DISALLOWED_MSG` 错误信息
3. 验证正常的 `<if condition='p("xxx").equals("yyy")'>` 条件仍能正常工作
4. 运行测试：`mvn test -pl logback-core -Dtest="IfThenElseTest,OptionHelperTest"`

---

## CVE-2026-1225 — 通过配置文件实例化任意类

### CVE 基本信息

| 字段 | 值 |
|---|---|
| **CVE ID** | CVE-2026-1225 |
| **CVSS 评分** | 5.0 (CVSS v3.1, Medium) |
| **严重程度** | 中等 (Medium) |
| **CWE 分类** | CWE-20: 输入验证不当 |
| **公开日期** | 2026-01-22 |

### 漏洞描述

**影响组件：**
- `ch.qos.logback.core.joran.action.NestedComplexPropertyIA` (1.2.x)
- `ch.qos.logback.core.joran.util.PropertySetter`
- `ch.qos.logback.core.util.OptionHelper`

**攻击向量：**
攻击者如果能够修改 logback 配置文件，可以通过在嵌套 XML 元素上指定 `class` 属性来实例化类路径上的任意类。Joran 配置引擎在实例化类时不验证该类是否与预期的属性类型兼容。由于实例化后的对象可能因类型不匹配而被丢弃，实际影响有限。

**利用条件：**
- 攻击者需要对 logback 配置文件具有写权限
- 属于本地攻击向量（AV:L），攻击复杂度高（AC:H）
- 需要高权限（PR:H）

### 官方修复方案

官方在 1.5.x 分支（1.5.25 版本）通过**限制类实例化必须匹配预期的超类型**来修复此漏洞。这是一个四部分的修改：

1. **`ImplicitModelDataForComplexProperty`** — 添加 `expectedPropertyType` 字段及 getter/setter
2. **`PropertySetter`** — 添加 `getTypeForComplexProperty()` 方法，通过查找 setter/adder 方法获取参数类型
3. **`OptionHelper`** — 添加 `instantiateClassWithSuperclassRestriction()` 方法：
   ```java
   public static Object instantiateClassWithSuperclassRestriction(
           Class<?> classObj, Class<?> superClass)
           throws IncompatibleClassException, DynamicClassLoadingException {
       if (!superClass.isAssignableFrom(classObj)) {
           throw new IncompatibleClassException(superClass, classObj);
       }
       return classObj.getConstructor().newInstance();
   }
   ```
4. **`ImplicitModelHandler`** — 在 `doComplex()` 中使用受限实例化替代直接 `newInstance()`

**技术思路：** 在实例化组件类之前，先通过 JavaBean 属性分析获取预期的属性类型（即 setter/adder 方法的参数类型），然后验证要实例化的类是否是预期类型的子类。如果不兼容则抛出 `IncompatibleClassException`，阻止任意类的实例化。

**注意：** 此 CVE **没有 1.3.x 分支的 backport**，仅在 1.5.x 中修复。

### 官方修复 Commit

| 所属分支 | 修复版本 | Commit Hash |
|---|---|---|
| 1.5.x | 1.5.25 | `d28931f3b9ede954285cd22d44e029142bba52e6` |
| 1.3.x | 无 | 无 backport |

### 本次补丁修复

**分支：** `branch_1.2.x-bjca-patch`
**Commit：** `e237e1bd8`

**修改的文件列表：**

| 操作 | 文件路径 |
|---|---|
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/joran/action/IADataForComplexProperty.java` (+9 行) |
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/joran/util/PropertySetter.java` (+8 行) |
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/util/OptionHelper.java` (+12 行) |
| 修改 | `logback-core/src/main/java/ch/qos/logback/core/joran/action/NestedComplexPropertyIA.java` (+3/-1 行) |

具体修改：
- **`IADataForComplexProperty`**：添加 `expectedPropertyType` 字段及 getter/setter（对应 1.5.x 的 `ImplicitModelDataForComplexProperty`）
- **`PropertySetter`**：添加 `getTypeForComplexProperty(String, AggregationType)` 方法，利用已有的 `getRelevantMethod()` 和 `getParameterClassForMethod()` 获取预期属性类型
- **`OptionHelper`**：添加 `instantiateClassWithSuperclassRestriction(Class<?>, Class<?>)` 方法，使用 `classObj.newInstance()`（Java 6 兼容）替代 1.5.x 的 `classObj.getConstructor().newInstance()`
- **`NestedComplexPropertyIA`**：在 `isApplicable()` 中存储预期类型，在 `begin()` 中用受限实例化方法替换原来的 `componentClass.newInstance()`

**与官方修复的差异说明：**
- 1.2.x 使用 `NestedComplexPropertyIA`（SAX 行为），1.5.x 使用 `ImplicitModelHandler`（模型处理器）——架构差异最大的一个修复
- 1.2.x 使用 `PropertySetter` + `BeanDescription` 方式，1.5.x 使用 `AggregationAssessor`——属性类型查找机制不同但功能等价
- Java 6 兼容性适配：使用 `classObj.newInstance()` 替代 `classObj.getConstructor().newInstance()`；使用 `catch (Exception e)` 替代多 catch 语法
- `IncompatibleClassException` 构造器为包私有访问，`OptionHelper` 与其在同一包 `ch.qos.logback.core.util` 中，因此可正常访问
- Null 安全处理：当 `expectedPropertyType` 为 null 时跳过超类检查（正常情况下 `AS_COMPLEX_PROPERTY`/`AS_COMPLEX_PROPERTY_COLLECTION` 场景不会出现 null）

### 验证与测试步骤

1. 在 logback 配置中使用嵌套元素的 `class` 属性指定一个不兼容的类（如在 `<appender>` 的 `<encoder>` 子元素中指定 `class="java.util.HashMap"`），确认抛出 `IncompatibleClassException`
2. 验证正常的配置（如 `<encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">`）仍能正常工作
3. 运行 logback-core Joran 测试：`mvn test -pl logback-core -Dtest="**/joran/**"` — 确认 323 个测试通过
4. 运行 logback-classic Joran 配置测试：`mvn test -pl logback-classic -Dtest="JoranConfiguratorTest"` — 确认 27 个测试通过

---

## 功能瘦身安全加固

### 概述

除上述 CVE 定点修复外，本次补丁还通过**移除高风险功能模块**进一步减少攻击面。以下功能在安全审计中被识别为潜在风险点，且在目标生产环境中不被使用，因此予以移除。

### 加固措施一览

| 加固项 | 措施 | 安全收益 |
|---|---|---|
| SMTPAppender | 完全删除 | 防止通过日志配置触发邮件泄露敏感信息 |
| Socket/Receiver 网络组件 | 完全删除 | 消除远程日志传输攻击面（反序列化、网络监听） |
| ConsolePlugin | 完全删除（依赖 SocketAppender） | 消除本地 Socket 连接接口 |
| javax.mail 依赖 | 从所有模块 pom.xml 中移除 | 减少依赖链，防止邮件相关类被恶意配置利用 |

### 移除的 SMTPAppender 组件

**安全动机：** SMTPAppender 允许通过 logback 配置文件触发邮件发送。如果攻击者能修改配置文件，可将日志内容（可能包含敏感信息）发送到外部邮箱。

**删除的源文件：**

| 模块 | 文件 |
|---|---|
| logback-core | `core/net/SMTPAppenderBase.java`、`core/net/LoginAuthenticator.java` |
| logback-classic | `classic/net/SMTPAppender.java` |
| logback-access | `access/net/SMTPAppender.java` |

**删除的测试文件：**
- `classic/net/DilutedSMTPAppenderTest.java`
- `classic/net/SMTPAppender_GreenTest.java`
- `classic/net/SMTPAppender_SubethaSMTPTest.java`

**删除的示例和配置：**
- `logback-examples/src/main/java/chapters/appenders/mail/` 整个目录
- `logback-examples/src/main/resources/chapters/appenders/mail/` 整个目录
- SMTP 相关的 XML 配置文件（`logback-SMTP.xml`、`logback-SMTPWithHtml.xml` 等）

**pom.xml 依赖清理：**
- 父 `pom.xml`：移除 `javax.mail.version` 属性和 `javax.mail:mail` 依赖管理
- `logback-core/pom.xml`：移除 `javax.mail:mail` optional 依赖
- `logback-classic/pom.xml`：移除 `javax.mail:mail` optional 依赖、`greenmail` 和 `subethasmtp` 测试依赖
- `logback-access/pom.xml`：移除 `javax.mail:mail` optional 依赖

### 移除的 Socket/Receiver 网络组件

**安全动机：** Socket 和 Receiver 组件实现了基于 TCP 的远程日志传输，涉及对象序列化/反序列化，是潜在的远程代码执行和拒绝服务攻击入口。虽然 CVE-2023-6378/CVE-2023-6481 已在 1.2.13 基线中修复了 DoS 漏洞，但完全移除这些组件可从根本上消除此攻击面。

**删除的 logback-core 源文件：**

| 类别 | 文件 |
|---|---|
| Socket 基类 | `core/net/AbstractSocketAppender.java`、`core/net/AbstractSSLSocketAppender.java` |
| 连接器 | `core/net/SocketConnector.java`、`core/net/DefaultSocketConnector.java` |
| 序列化工具 | `core/net/AutoFlushingObjectWriter.java`、`core/net/ObjectWriter.java`、`core/net/ObjectWriterFactory.java`、`core/net/HardenedObjectInputStream.java`、`core/net/QueueFactory.java` |
| Server 框架 | `core/net/server/` 整个目录（含 `AbstractServerSocketAppender`、`ConcurrentServerRunner`、`ServerSocketListener`、`RemoteReceiverStreamClient` 等） |

**删除的 logback-classic 源文件：**

| 类别 | 文件 |
|---|---|
| Socket Appender | `classic/net/SocketAppender.java`、`classic/net/SSLSocketAppender.java` |
| Socket Server | `classic/net/SimpleSocketServer.java`、`classic/net/SimpleSSLSocketServer.java`、`classic/net/SocketNode.java`、`classic/net/SocketAcceptor.java` |
| Receiver | `classic/net/ReceiverBase.java`、`classic/net/SocketReceiver.java`、`classic/net/SSLSocketReceiver.java` |
| Server 包 | `classic/net/server/` 整个目录（含 `ServerSocketReceiver`、`SSLServerSocketReceiver`、`ServerSocketAppender`、`RemoteAppenderClient` 等） |
| 序列化变换 | `classic/net/LoggingEventPreSerializationTransformer.java` |
| Joran Action | `classic/joran/action/ReceiverAction.java`、`classic/joran/action/ConsolePluginAction.java` |

**删除的 logback-access 源文件：**

| 类别 | 文件 |
|---|---|
| Socket Appender | `access/net/SocketAppender.java`、`access/net/SSLSocketAppender.java`、`access/net/SimpleSocketServer.java`、`access/net/SocketNode.java` |
| Server 包 | `access/net/server/` 整个目录 |
| 序列化工具 | `access/net/AccessEventPreSerializationTransformer.java`、`access/net/HardenedAccessEventInputStream.java`、`access/net/URLEvaluator.java` |

**Joran 配置清理：**
- `classic/joran/JoranConfigurator.java`：移除 `configuration/receiver` 和 `configuration/consolePlugin` 规则注册

**删除的测试和示例：**
- `logback-core/src/test/java/ch/qos/logback/core/net/` 下的 Socket 相关测试及 mock 类
- `logback-classic/src/test/` 下的 Socket/Receiver 相关测试
- `logback-access/src/test/` 下的网络相关测试
- `logback-examples/` 下的 socket 和 receivers 章节示例

### 保留的组件

以下组件经评估后保留：

| 组件 | 保留原因 |
|---|---|
| Janino 依赖 (optional) | `<if>` 条件功能仍需要（已通过 CVE-2025-11226 和 CVE-2026-13006 修复加固） |
| **SyslogAppender** | 标准 Syslog 协议，无反序列化风险，且为常用功能 |
| **SSL 基础框架**（`core/net/ssl/`） | 通用 SSL 配置类，可能被外部扩展使用 |
| **JNDI 支持** | 已在 1.2.9 中限制为仅允许 `java:` 前缀 |

### 验证

- 三个核心模块（logback-core、logback-classic、logback-access）编译通过
- 测试结果：logback-core 478 测试（1 失败为预存的文件滚动时序问题）、logback-classic 316 测试全部通过、logback-access 40 测试全部通过
- 所有测试失败均与本次功能移除无关

---

## 附录：已在 1.2.13 及之前版本修复的 CVE

以下 CVE 已在 logback 1.2.13 或更早的版本中修复，本次补丁**无需再修复**。

| CVE ID | 修复版本 | 说明 |
|---|---|---|
| CVE-2023-6378 | 1.2.13 | 通过 receiver 组件的拒绝服务（DoS）漏洞，已在 1.2.13 基线版本中修复 |
| CVE-2023-6481 | 1.2.13 | 与 CVE-2023-6378 为同一底层修复，已在 1.2.13 基线版本中修复 |
| CVE-2021-42550 | 1.2.9 | 基于 JNDI 的远程代码执行（RCE）漏洞，已在 1.2.9 版本中修复 |
