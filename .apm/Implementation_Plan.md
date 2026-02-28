# Logback 1.2.13 CVE 安全补丁 – APM Implementation Plan
**Memory Strategy:** Dynamic-MD
**Last Modification:** All 3 phases completed. Project finished — 9/9 tasks done.
**Project Overview:** 为 logback 1.2.13 收集所有已知 CVE 漏洞，在 branch_1.2.x-bjca-patch 分支上逐一修复（参考官方 commit 手动重写），每个修复单独提交并通过完整测试验证，更新版本号为 1.2.13-bjca-patch-SNAPSHOT，编写完整中文 CVE 修复文档。

## Phase 1: 项目初始化与 CVE 调研

### Task 1.1 – 创建工作分支 - Agent_CodeFix
**Objective:** 从 branch_1.2.x 的 1.2.13 release commit 创建新工作分支 branch_1.2.x-bjca-patch。
**Output:** 可用的 branch_1.2.x-bjca-patch 工作分支。
**Guidance:** 必须基于 `prepare release 1.2.13` commit（`2648b9e7f`）创建分支，确保起点与 1.2.13 发布版本一致。

- 在 branch_1.2.x 上定位 1.2.13 release commit（`prepare release 1.2.13`: `2648b9e7f`）
- 从该 commit 创建并切换到新分支 `branch_1.2.x-bjca-patch`：`git checkout -b branch_1.2.x-bjca-patch 2648b9e7f`

### Task 1.2 – CVE 全面调研与官方修复 commit 定位 - Agent_Research
**Objective:** 全面搜索影响 logback 1.2.13 的所有已知 CVE，定位每个 CVE 的官方修复 commit 并分析修复方案。
**Output:** 结构化 CVE 分析报告，包含每个 CVE 的详情、官方 commit 引用和 1.2.x 适配策略。
**Guidance:** CVE 范围仅限仍影响 1.2.13 的漏洞，排除已在 1.2.13 及之前版本修复的 CVE。优先查找 1.3.x 分支的 backport commit（离 1.2.x 最近）。初步调研已发现以下 CVE：CVE-2024-12798、CVE-2024-12801、CVE-2025-11226、CVE-2026-1225，需进一步验证和补充。

1. Ad-Hoc Delegation – CVE 数据库全面搜索（参考 .claude/commands/apm-7-delegate-research.md）
2. 在 NVD、GitHub Advisories、Snyk、logback 官方 news 页面搜索所有影响 logback 1.2.13 的 CVE，排除已在 1.2.13 及之前版本修复的 CVE（如 CVE-2023-6378/6481 已在 1.2.13 修复，CVE-2021-42550 已在 1.2.9 修复）
3. 对每个确认的 CVE，在官方 logback GitHub 仓库（qos-ch/logback）中定位修复 commit，优先查找 1.3.x 分支的 backport commit
4. 分析每个修复 commit 的代码变更内容，评估在 1.2.x 代码基础上重写的难度和策略
5. 输出结构化 CVE 分析报告，包含：CVE ID、CVSS 评分、影响组件、攻击向量描述、官方修复 commit ID、修复方案摘要、1.2.x 适配注意事项

## Phase 2: CVE 修复

### Task 2.1 – 修复 CVE-2024-12798（JaninoEventEvaluator ACE） - Agent_CodeFix
**Objective:** 参考官方修复 commit，手动重写修复 JaninoEventEvaluator 中的任意代码执行漏洞。
**Output:** 通过完整测试的修复 commit。
**Guidance:** **Depends on: Task 1.2 Output by Agent_Research** 参考官方 1.3.15 修复 commit（移除/限制 JaninoEventEvaluator）。修复方式为手动重写适配，非 cherry-pick。提交格式：`fix(CVE-2024-12798): 中文描述`。

1. 根据 Task 1.2 调研报告，查看 CVE-2024-12798 的官方修复 commit 代码变更（重点关注 JaninoEventEvaluator 的变更）
2. 对比 1.2.x 分支中对应代码，分析差异并确定适配方案
3. 在 branch_1.2.x-bjca-patch 上实现修复代码（手动重写，非 cherry-pick）
4. 运行完整 `mvn test`（JDK 8），确保所有测试通过且无回归
5. 提交修复：`fix(CVE-2024-12798): 修复 JaninoEventEvaluator 任意代码执行漏洞`

### Task 2.2 – 修复 CVE-2024-12801（SaxEventRecorder SSRF） - Agent_CodeFix
**Objective:** 参考官方修复 commit，手动重写修复 SaxEventRecorder 中的 SSRF 漏洞。
**Output:** 通过完整测试的修复 commit。
**Guidance:** **Depends on: Task 2.1 Output** 参考官方 1.3.15 修复 commit（修改 SaxEventRecorder 忽略外部 DTD）。按 CVE 编号顺序执行，需在 Task 2.1 完成后进行。

1. 根据 Task 1.2 调研报告，查看 CVE-2024-12801 的官方修复 commit 代码变更（重点关注 SaxEventRecorder 的 DTD 处理逻辑）
2. 对比 1.2.x 分支中 SaxEventRecorder 代码，分析差异并确定适配方案
3. 在 branch_1.2.x-bjca-patch 上实现修复代码，使 SaxEventRecorder 忽略外部 DTD 声明
4. 运行完整 `mvn test`（JDK 8），确保所有测试通过且无回归
5. 提交修复：`fix(CVE-2024-12801): 修复 SaxEventRecorder SSRF 漏洞`

### Task 2.3 – 修复 CVE-2025-11226（`<if>` 条件 ACE） - Agent_CodeFix
**Objective:** 参考官方修复 commit，手动重写修复配置文件 `<if>` 元素条件评估中的任意代码执行漏洞。
**Output:** 通过完整测试的修复 commit。
**Guidance:** **Depends on: Task 2.2 Output** 参考官方 1.3.16 修复 commit（禁止 condition 属性中 `new` 操作符）。可能涉及 Janino 条件评估相关代码。

1. 根据 Task 1.2 调研报告，查看 CVE-2025-11226 的官方修复 commit 代码变更（重点关注 `<if>` 元素的条件评估逻辑和 `new` 操作符过滤）
2. 对比 1.2.x 分支中对应的条件评估代码，分析差异并确定适配方案
3. 在 branch_1.2.x-bjca-patch 上实现修复代码，禁止 condition 属性中使用 `new` 操作符
4. 运行完整 `mvn test`（JDK 8），确保所有测试通过且无回归
5. 提交修复：`fix(CVE-2025-11226): 禁止配置文件 if 条件中使用 new 操作符`

### Task 2.4 – 修复 CVE-2026-1225（组件实例化 ACE） - Agent_CodeFix
**Objective:** 参考官方修复 commit，手动重写修复配置文件处理中组件实例化验证不足的任意代码执行漏洞。
**Output:** 通过完整测试的修复 commit。
**Guidance:** **Depends on: Task 2.3 Output** 官方修复在 1.5.25（无 1.3.x backport），与 1.2.x 差异可能较大。如有 1.3.x backport 优先参考。适配难度可能高于其他 CVE。

1. 根据 Task 1.2 调研报告，查看 CVE-2026-1225 的官方修复 commit 代码变更（重点关注组件实例化验证逻辑）
2. 由于官方修复在 1.5.x 分支（与 1.2.x 差异较大），需深入分析 1.5.x 修复与 1.2.x 代码结构的差异，确定最佳适配方案
3. 如有 1.3.x 的 backport commit 可参考，优先使用 1.3.x 版本的修复方案
4. 在 branch_1.2.x-bjca-patch 上实现修复代码，确保配置文件处理时只实例化与预期类兼容的组件
5. 运行完整 `mvn test`（JDK 8），确保所有测试通过且无回归
6. 提交修复：`fix(CVE-2026-1225): 限制配置文件组件实例化范围`

## Phase 3: 版本更新与文档编写

### Task 3.1 – 更新版本号 - Agent_CodeFix
**Objective:** 将项目所有 pom.xml 中的版本号更新为 1.2.13-bjca-patch-SNAPSHOT。
**Output:** 版本号更新 commit。
**Guidance:** **Depends on: Task 2.4 Output** logback 是多模块 Maven 项目，需更新父 pom 和所有子模块 pom。确保版本更新后构建仍正常。

- 查找项目所有 pom.xml 文件，将版本号从 `1.2.13` 更新为 `1.2.13-bjca-patch-SNAPSHOT`
- 运行 `mvn test`（JDK 8）验证版本更新未破坏构建
- 提交版本更新 commit

### Task 3.2 – 编写完整 CVE 修复文档 - Agent_Documentation
**Objective:** 在 doc/CVE/ 目录下编写完整的中文 CVE 修复文档，涵盖所有已修复 CVE 的详细信息。
**Output:** 完整的中文 Markdown CVE 修复文档文件。
**Guidance:** **Depends on: Task 1.2 Output by Agent_Research** 和 **Depends on: Task 2.4 Output by Agent_CodeFix** 文档必须"完备"，每个 CVE 条目必须包含官方修复方案的详细说明和官方 commit ID。语言为中文。

1. 在 `doc/CVE/` 目录下创建文档文件（如 `CVE_修复说明.md`）
2. 编写文档头部：项目概述、修复版本（1.2.13-bjca-patch-SNAPSHOT）、修复日期、CVE 总览表（含 CVE ID、类型、严重程度、状态）
3. 为每个 CVE 编写详细条目，必须包含以下所有小节：
   - **CVE 基本信息**：CVE ID、CVSS 评分、严重程度等级
   - **漏洞描述**：影响组件、攻击向量、利用条件的详细描述
   - **官方修复方案**：官方是如何修复此漏洞的（修改了哪些类/方法、采用了什么策略、为什么这样修复），需详细说明官方修复的技术思路
   - **官方修复 Commit**：官方修复的 commit ID（如 qos-ch/logback 仓库的 commit hash）、所属分支（1.3.x / 1.5.x）、修复版本号
   - **本次补丁修复**：我们在 branch_1.2.x-bjca-patch 中对应的 commit 信息、与官方修复的差异说明（如有适配改动需注明）
   - **验证与测试步骤**：如何验证此修复有效的具体步骤
4. 编写附录：已在 1.2.13 及之前版本修复的 CVE 列表（CVE-2023-6378/6481、CVE-2021-42550 等），说明无需再修复及其官方修复版本
5. 审阅文档完整性，确保每个 CVE 条目信息完备，提交文档

### Task 3.3 – 最终构建验证 - Agent_CodeFix
**Objective:** 在所有修复和版本更新完成后，进行最终的完整构建验证。
**Output:** 验证通过的确认。
**Guidance:** **Depends on: Task 3.1 Output** 和 **Depends on: Task 3.2 Output by Agent_Documentation** 最终验证确保项目整体状态正确。

- 运行完整 `mvn clean test`（JDK 8），验证所有修复在版本更新后仍然正常
- 验证 git log 确认所有 CVE 修复 commit 按正确顺序存在，commit message 格式正确
