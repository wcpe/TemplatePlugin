# TemplatePlugin

基于 [TabooLib](https://github.com/TabooLib/taboolib) 6.x 的 **Bukkit 插件开发模板**：开箱即用的 Gradle 多模块结构，内置依赖注入、持久化、对外 API 发布与一套工程规范。

## 特性

- **多模块分层**：`api`（对外契约）/ `project:core`（业务实现）/ `plugin`（组装打包）。
- **依赖注入**：集成 `top.wcpe.taboolib.ioc`，自动接管 IoC 与 relocate。
- **持久化**：KSP + [EasyQuery](https://github.com/dromara/easy-query) Code First，编译期生成 EntityProxy。
- **对外 API 发布**：`api` 模块配置 `maven-publish`（含源码 JAR），公开类型走纯 Java 保证干净 ABI。
- **国际化**：内置 TabooLib `I18n` 模块。
- **代码质量审查**：内置 Java（SpotBugs / FindSecBugs / Error Prone / PMD / Checkstyle / JaCoCo）与 Kotlin（detekt / ktlint / Kover / Semgrep）质量门禁，独立 CI 工作流，与编译解耦，详见 [CODE_QUALITY.md](docs/CODE_QUALITY.md)。
- **工程规范**：`.claude/rules/` 覆盖注释 / 配置 / 测试 / 对外 API / 提交；附发版技能。

技术栈与模块拓扑详见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 模块结构

| 模块 | 路径 | 职责 |
| --- | --- | --- |
| `api` | `api/` | 对外契约：接口 / 注解 / 枚举 / DTO（Java），发布到 Maven。 |
| `project:core` | `project/core/` | 业务实现：服务、仓库、领域逻辑、EasyQuery 持久化（Kotlin）。 |
| `plugin` | `plugin/` | 组装层：TabooLib 描述 + IoC 接管，打包产出最终插件 JAR。 |

## 快速开始

```bash
git clone <仓库地址>
cd TemplatePlugin

# 构建发行版本（产物在 plugin/build/libs/）
./gradlew build
```

若基于本模板新建项目，请先完成下方 [首次使用修改清单](#首次使用修改清单)。

## 构建发行版本

发行版本用于正常使用，不含 TabooLib 本体。

```
./gradlew build
```

## 构建开发版本

开发版本包含 TabooLib 本体，用于开发者使用，但不可运行。

```
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 `-PDeleteCode` 表示移除所有逻辑代码以减少体积。

## 首次使用修改清单

克隆模板后，按以下清单把项目标识替换为你自己的（逐项核对，避免遗漏）：

- [ ] **group / 版本**：`gradle.properties` 的 `group=top.wcpe.mc.plugin.template` 改为你的 group；`version=` 设为起始版本。
- [ ] **项目名**：把 `settings.gradle.kts` 中的 `rootProject.name = "TemplatePlugin"` 改为你的插件名。它决定最终插件 JAR 名与 `api` 模块的 `artifactId`（如 `TemplatePlugin` → `templateplugin-api`）。
- [ ] **插件描述与作者**：`plugin/build.gradle.kts` 的 `description { ... }` —— `desc("TabooLib Bukkit 插件模板")` 是占位描述，改成你的插件简介；`contributors { name("WCPE") }` 改成你的名字。
- [ ] **代码包名**：业务代码包路径改为你的 group 对应路径（替换 `top.wcpe.mc.plugin.template`）。
- [ ] **Maven 发布目标**：`api/build.gradle.kts` 的 `releasesRepoUrl` / `snapshotsRepoUrl` 改为你的私服地址；发布凭据 `username` / `password` 配在 `~/.gradle/gradle.properties`。**若不需要对外发布**，可删除远程仓库段、只保留 `mavenLocal()`。
- [ ] **依赖仓库（注意保留）**：根 `build.gradle.kts` 与 `settings.gradle.kts` 中的 `https://maven.wcpe.top/repository/maven-public/` 是 `top.wcpe.taboolib.ioc` 等依赖的来源，**作为依赖源应保留**；它与上一条的"发布目标"是两回事。
- [ ] **更新日志链接**：`CHANGELOG.md` 底部的 `your-org/your-repo` 改为你的仓库地址。
- [ ] **许可证**：`LICENSE` 默认版权归属 `WCPE`（`Copyright (c) 2026 WCPE`），改成你的名字，或更换为你需要的协议。
- [ ] **本文件**：把上方标题、简介改成你的项目信息。

## 文档

| 文档 | 说明 |
| --- | --- |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 模块拓扑、技术栈、构建系统、分层约定。 |
| [API.md](docs/API.md) | 对外 API 模块定位、发布坐标、接入方式、编写约定。 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | 开发环境、构建、编码与提交规范、PR 流程。 |
| [CODE_QUALITY.md](docs/CODE_QUALITY.md) | 代码质量与安全审查工具链：工具清单、运行方式、CI、规则配置。 |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志（历史归档于 `docs/changelog/`）。 |
| `.claude/rules/` | 强制工程规范（注释 / 配置 / 测试 / 对外 API / 提交）。 |
| `.claude/skills/release-version/` | 本地发版流程技能。 |

## 许可证

本项目基于 [MIT 许可证](LICENSE) 开源。
