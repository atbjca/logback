# Logback 1.2.13-bjca-patch 安全加固阶段总结

> **文档类型：** 阶段性工作总结
> **工作周期：** 2026-02-28
> **工作分支：** `branch_1.2.x-bjca-patch`
> **基线版本：** logback 1.2.13
> **补丁版本：** `1.2.13-nes.patch.1`

---

## 一、背景

Logback 1.2.x 分支已停止官方维护，但由于生产环境的 Java 版本和框架兼容性要求，无法直接升级到 1.3.x/1.5.x。与此同时，1.2.13 之后陆续披露了多个安全漏洞（CVE），存在潜在的安全风险。

本次工作的目标是：**在不升级大版本的前提下，对 logback 1.2.13 进行安全补丁回移（backport）和功能瘦身加固，消除已知安全隐患。**

---

## 二、工作内容概览

本次工作共产生 **7 个提交**，涵盖 CVE 修复、功能瘦身、版本管理和文档编写四大部分。

| 序号 | 类别 | Commit | 说明 |
|:---:|---|---|---|
| 1 | CVE 修复 | `f5dabd9fc` | 修复 CVE-2024-12798：移除 JaninoEventEvaluator |
| 2 | CVE 修复 | `83cbaec15` | 修复 CVE-2024-12801：阻止 SaxEventRecorder SSRF |
| 3 | CVE 修复 | `ac2ae32a1` | 修复 CVE-2025-11226：禁止 `<if>` 条件中的 `new` 操作符 |
| 4 | CVE 修复 | `e237e1bd8` | 修复 CVE-2026-1225：限制配置文件组件实例化范围 |
| 5 | 版本管理 | `2a5f27fb3` | 版本号更新为 `1.2.13-nes.patch.1` |
| 6 | 文档 | `22cec7a75` | 添加 CVE 修复说明文档 |
| 7 | 功能瘦身 | `1bc86b1d5` | 移除 SMTPAppender、Socket/Receiver 全部网络组件 |

**变更规模：** 247 个文件变更，+3,962 行，-13,152 行（净减少约 9,190 行代码）。

---

## 三、CVE 安全漏洞修复（5 项）

### 3.1 CVE-2024-12798 — 任意代码执行

| 项目 | 内容 |
|---|---|
| **严重程度** | Medium (CVSS 5.9) |
| **漏洞本质** | `JaninoEventEvaluator` 允许在配置文件中嵌入并执行任意 Java 表达式 |
| **修复策略** | 完全移除 `JaninoEventEvaluator` 及其基类（与官方 1.3.15 策略一致） |
| **影响模块** | logback-core、logback-classic、logback-access |

### 3.2 CVE-2024-12801 — 服务端请求伪造（SSRF）

| 项目 | 内容 |
|---|---|
| **严重程度** | Low (CVSS 2.4) |
| **漏洞本质** | `SaxEventRecorder` 未禁用外部实体解析，可通过 DOCTYPE 注入触发 SSRF |
| **修复策略** | 覆写 `resolveEntity()` 方法，返回空白内容阻止外部 URL 请求 |
| **影响模块** | logback-core |

### 3.3 CVE-2025-11226 — 通过 `<if>` 条件执行任意代码

| 项目 | 内容 |
|---|---|
| **严重程度** | Medium (CVSS 5.9) |
| **漏洞本质** | `<if>` 条件属性中可使用 `new` 操作符实例化任意对象并执行代码 |
| **修复策略** | 在条件表达式编译前检测并拦截 `new` 操作符（与官方 1.3.16 策略一致） |
| **影响模块** | logback-core |

### 3.4 CVE-2026-1225 — 配置文件实例化任意类

| 项目 | 内容 |
|---|---|
| **严重程度** | Medium (CVSS 5.0) |
| **漏洞本质** | Joran 引擎在实例化嵌套组件时不验证类型兼容性，可实例化任意类 |
| **修复策略** | 添加超类型检查，实例化前验证目标类是否为预期类型的子类 |
| **影响模块** | logback-core |

### 3.5 CVE-2026-13006 — 通过 Unicode 转义绕过 `<if>` 条件限制

| 项目 | 内容 |
|---|---|
| **严重程度** | High (CVSS 7.0) |
| **漏洞本质** | 使用 `\u`/`\U` Unicode 转义拼出 `new` 关键字，绕过 CVE-2025-11226 的字面量检测 |
| **修复策略** | 在 Janino 编译前检测并拒绝条件字符串中的 Unicode 转义序列（与官方 1.5.35 策略一致） |
| **影响模块** | logback-core |

> 所有 CVE 修复均参考官方高版本的修复方案，回移到 1.2.x 的代码架构中。详细的修复说明和代码对比见 [CVE 修复说明文档](CVE/CVE_修复说明.md)。

---

## 四、功能瘦身安全加固

在完成 CVE 定点修复后，进一步通过**移除高风险且未使用的功能模块**来缩小攻击面。

### 4.1 移除的组件

| 组件 | 涉及模块 | 安全收益 |
|---|---|---|
| **SMTPAppender** | core / classic / access | 防止通过配置文件触发邮件发送泄露敏感信息 |
| **SocketAppender** | core / classic / access | 消除基于 TCP 的远程日志传输攻击面 |
| **ServerSocketAppender** | core / classic / access | 消除服务端监听端口风险 |
| **SocketReceiver / ServerSocketReceiver** | classic | 消除远程日志接收的反序列化攻击面 |
| **SimpleSocketServer** | classic / access | 移除独立 Socket 服务进程 |
| **ConsolePlugin** | classic | 消除本地 Socket 连接接口（依赖 SocketAppender） |

### 4.2 清理的依赖

- `javax.mail:mail` — 从所有模块移除
- `com.icegreen:greenmail` — 从 logback-classic 测试依赖移除
- `org.subethamail:subethasmtp` — 从 logback-classic 测试依赖移除

### 4.3 保留的组件

| 组件 | 保留原因 |
|---|---|
| Janino 依赖 (optional) | `<if>` 条件功能仍需要（已通过 CVE-2025-11226 和 CVE-2026-13006 修复加固） |
| SyslogAppender | 标准 Syslog 协议，无反序列化风险，属常用功能 |
| SSL 基础框架 (`core/net/ssl/`) | 通用 SSL 配置类，可能被外部扩展使用 |

---

## 五、测试验证

| 模块 | 测试数量 | 结果 |
|---|---|---|
| logback-core | 478 | 通过（1 个预存的文件滚动时序问题，与本次修改无关） |
| logback-classic | 316 | 全部通过 |
| logback-access | 40 | 全部通过 |

---

## 六、升级影响与兼容性说明

### 6.1 不兼容变更

使用以下功能的项目在升级后将**无法正常工作**，需提前排查和迁移：

| 功能 | 配置文件中的典型表现 | 替代方案 |
|---|---|---|
| 邮件告警 | `<appender class="...SMTPAppender">` | 使用外部告警系统（如 Prometheus AlertManager） |
| 远程日志传输 | `<appender class="...SocketAppender">` | 使用 Fluentd / Logstash / Filebeat 采集日志 |
| 远程日志接收 | `<receiver class="...SocketReceiver">` | 使用集中日志平台接收 |
| Janino 表达式评估 | `<evaluator>` 不指定 class | 使用 `<filter>` 的 Level/Threshold 过滤器替代 |
| `<if>` 中使用 `new` | `<if condition='new ...'>` | 改用属性判断方式 |

### 6.2 升级检查清单

1. 搜索项目中所有 `logback.xml` / `logback-spring.xml` 配置文件
2. 检查是否包含 `SMTPAppender`、`SocketAppender`、`SocketReceiver`、`ServerSocket*`、`SimpleSocketServer`、`ConsolePlugin` 等关键字
3. 检查是否使用 `<evaluator>` 元素（无 class 属性时默认使用 JaninoEventEvaluator）
4. 如有以上用法，在升级前完成迁移

---

## 七、产出物清单

| 产出物 | 路径/位置 |
|---|---|
| 补丁代码 | `branch_1.2.x-bjca-patch` 分支（7 个 commit） |
| CVE 修复详细文档 | `doc/CVE/CVE_修复说明.md` |
| 本阶段总结文档 | `doc/logback-1.2.13-bjca-patch-阶段总结.md`（本文档） |

---

## 八、后续建议

1. **发布正式版本：** 已于 2026-06-25 发布 `1.2.13-nes.patch.1` 至内部 Maven 仓库
2. **通知下游项目：** 告知使用 logback 1.2.x 的项目组进行升级，并提供升级检查清单
3. **持续跟踪：** 关注后续 logback CVE 披露，必要时在本分支上继续回移修复
4. **长期规划：** 评估应用的 Java 版本升级可行性，条件成熟时迁移到官方维护的 logback 1.5.x
