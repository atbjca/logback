# Logback Java 8 + SLF4J 1.x Migration – APM Memory Root
**Memory Strategy:** Dynamic-MD
**Project Overview:** 将 logback 1.5.33 核心模块（logback-core、logback-classic）及其黑盒测试模块改造为 Java 8 兼容，SLF4J 从 2.0.17 降级到 1.7.36（StaticLoggerBinder 绑定机制），Jakarta EE 回退到 javax 命名空间，移除 JPMS module-info 和 Multi-Release JAR。版本号 1.5.33-slf4j1x-jre8，分支 master-jre8。6 个 Phase、18 个 Task、3 个 Agent。
