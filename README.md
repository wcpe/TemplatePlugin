# TemplatePlugin

基于 [TabooLib](https://github.com/TabooLib/taboolib) 6.x 的 **Bukkit 插件开发模板**：开箱即用的 Gradle 多模块结构，内置依赖注入、持久化、对外 API 发布与一套工程规范。

## 特性

- **多模块分层**：`api`（对外契约）/ `project:core`（业务实现）/ `plugin`（组装打包）。
- **依赖注入**：集成 `top.wcpe.taboolib.ioc`，自动接管 IoC 与 relocate。
- **持久化**：KSP + [EasyQuery](https://github.com/dromara/easy-query) Code First，编译期生成 EntityProxy。
- **对外 API 发布**：`api` 模块配置 `maven-publish`（含源码 JAR），公开类型走纯 Java 保证干净 ABI。
- **国际化**：内置 TabooLib `I18n` 模块。
- **代码质量审查**：内置 Java（PMD / Checkstyle / SpotBugs / FindSecBugs / Error Prone）与 Kotlin（detekt / ktlint）质量门禁，外加 JUnit 5 测试，全部默认挂入 `check` —— 质量门禁与构建同一条命令（`build` = assemble + check），无需额外接线，在 CI 中作为构建的前置阻断，详见 [CODE_QUALITY.md](docs/CODE_QUALITY.md)。
- **工程规范**：`.claude/rules/` 覆盖注释 / 配置 / 测试 / 对外 API / 提交；附发版技能。

技术栈与模块拓扑详见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 模块结构

| 模块 | 路径 | 职责 |
| --- | --- | --- |
| `api` | `api/` | 对外契约：接口 / 注解 / 枚举 / DTO（Java），发布到 Maven。 |
| `project:core` | `project/core/` | 业务实现：服务、仓库、领域逻辑、EasyQuery 持久化（Kotlin）。 |
| `plugin` | `plugin/` | 组装层：TabooLib 描述 + IoC 接管，打包产出最终插件 JAR。 |

### 自带的最小可运行示例

模板不是空骨架，仓库自带一套能编译、能过门禁、能运行的示例代码，可直接作为起点改写，也可以整块删掉：

| 文件 | 作用 |
| --- | --- |
| [api/src/main/java/top/wcpe/mc/plugin/template/api/GreetingService.java](api/src/main/java/top/wcpe/mc/plugin/template/api/GreetingService.java) | 对外契约接口，唯一方法 `String welcome(String playerName)`；示范 `api` 模块的 Java 写法与 2 空格缩进。 |
| [project/core/src/main/kotlin/top/wcpe/mc/plugin/template/core/service/GreetingServiceImpl.kt](project/core/src/main/kotlin/top/wcpe/mc/plugin/template/core/service/GreetingServiceImpl.kt) | 用 `@Service("greetingService")` 交给 IoC 容器托管并实现上述接口。 |
| [project/core/src/main/kotlin/top/wcpe/mc/plugin/template/core/command/TemplateCommand.kt](project/core/src/main/kotlin/top/wcpe/mc/plugin/template/core/command/TemplateCommand.kt) | 命令示例，注册后可用 `/template greet <玩家名>`。 |
| [project/core/src/main/resources/lang/zh_CN.yml](project/core/src/main/resources/lang/zh_CN.yml) | i18n 文案；示范 `sendLang` 的参数个数与节点名的对应规则。 |
| [project/core/src/test/kotlin/top/wcpe/mc/plugin/template/core/service/GreetingServiceImplTest.kt](project/core/src/test/kotlin/top/wcpe/mc/plugin/template/core/service/GreetingServiceImplTest.kt) | 两个 JUnit 5 测试，示范「方法名英文 lowerCamelCase + 中文 `@DisplayName`」的测试规范。 |

## 快速开始

```bash
git clone <仓库地址>
cd TemplatePlugin

# 质量门禁 + 测试，任一违规即失败（工具链固定 JDK 17，产出 Java 8 字节码）
./gradlew check

# 构建发行版本（产物在 plugin/build/libs/）
./gradlew build
```

`build` 就是 assemble + check，门禁不需要单独跑。构建/配置过程需要 **JDK 17**（与 CI 一致）。

最短验证路径：

1. 跑 `./gradlew check` —— PMD / Checkstyle / SpotBugs / Error Prone / detekt / ktlint 与 JUnit 5 测试一起执行，任一违规即失败。覆盖率报告**不在** `check` 里，需要时另跑 `./gradlew koverXmlReport`（含 `src/main/kotlin` 的模块）或 `./gradlew jacocoTestReport`。
2. `./gradlew build` 后把 `plugin/build/libs/` 下的 JAR 放进服务端，启动后输入 `/template greet <玩家名>` —— 示例命令会从 IoC 容器取到 `GreetingService`，并按 `zh_CN.yml` 的节点回复一句带参数的问候。

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

## 发布版本

仓库内置三个 GitHub Actions 工作流：

| 工作流 | 触发方式 | 说明 |
| --- | --- | --- |
| CI | push / PR 到 `master` | 质量门禁与测试（`check`）→ 构建产物（`build`）；质量门禁不通过则第二步不执行 |
| 预览版发布 | **仅手动触发** | Actions → 预览版发布 → Run workflow；发布/刷新 `latest` 预发布 |
| Release | **推送 `v*` 标签** | 发布正式版 Release，附带依据合并 PR 自动生成的变更说明 |

### 发布正式版

1. 把 `gradle.properties` 的 `version=` 更新为目标版本号并提交；
2. 打标签并推送：

```bash
git tag v0.0.1
git push origin v0.0.1
```

标签版本必须与 `gradle.properties` 一致，否则工作流会拒绝发布。带后缀的标签（如 `v1.0.0-rc.1`）会被标记为预发布。

> Release 说明中的「What's Changed」由 GitHub 依据**已合并的 PR** 自动生成，因此建议变更一律走 PR；直接推送到 master 的提交不会出现在其中。

### 发布预览版

在 Actions 页面手动触发「预览版发布」工作流即可。它会把 `latest` 标签移到触发时的提交，并以 `预览版 latest-<短SHA>-<日期>` 为名刷新该预发布，产物文件名同样带该标识，与正式版互不覆盖。

## 首次使用修改清单

克隆模板后，按以下清单把项目标识替换为你自己的（逐项核对，避免遗漏）：

- [ ] **group / 版本**：`gradle.properties` 的 `group=top.wcpe.mc.plugin.template` 改为你的 group；`version=` 设为起始版本。
- [ ] **项目名**：把 `settings.gradle.kts` 中的 `rootProject.name = "TemplatePlugin"` 改为你的插件名。它决定最终插件 JAR 名与 `api` 模块的 `artifactId`（如 `TemplatePlugin` → `templateplugin-api`）。
- [ ] **插件描述与作者**：`plugin/build.gradle.kts` 的 `description { ... }` —— `desc("TabooLib Bukkit 插件模板")` 是占位描述，改成你的插件简介；`contributors { name("WCPE") }` 改成你的名字。
- [ ] **代码包名**：把 `top.wcpe.mc.plugin.template` 整体替换为你的 group 对应路径——[自带示例](#自带的最小可运行示例) 那 5 个文件所在的包目录也要一并替换。
- [ ] **示例代码**：[自带示例](#自带的最小可运行示例) 可以**整块删掉**（删净 `api/src/main/java`、`project/core/src/{main,test}/kotlin`、`project/core/src/main/resources/lang` 即得空骨架），也可以保留作为起点改写；若删除，记得同时清掉 `zh_CN.yml` 里遗留的 `greet:` 节点。
- [ ] **Maven 发布目标**：`api/build.gradle.kts` 的 `releasesRepoUrl` / `snapshotsRepoUrl` 改为你的私服地址；发布凭据 `username` / `password` 配在 `~/.gradle/gradle.properties`。**若不需要对外发布**，可删除远程仓库段、只保留 `mavenLocal()`。
- [ ] **依赖仓库（注意保留）**：根 `build.gradle.kts` 与 `settings.gradle.kts` 中的 `https://maven.wcpe.top/repository/maven-public/` 是 `top.wcpe.taboolib.ioc` 等依赖的来源，**作为依赖源应保留**；它与上一条的"发布目标"是两回事。
- [ ] **更新日志链接**：`CHANGELOG.md` 底部的 `your-org/your-repo` 改为你的仓库地址。
- [ ] **自托管 runner 标签**：三个工作流的 `runs-on` 现为 `[self-hosted, linux]`。请确认你的 runner 带有 `linux` 标签；若标签不同，改为实际使用的标签集（如 `[self-hosted, docker]`），以定向到确定机队而非任意 runner。
- [ ] **runner 代理**：构建步骤已用 `env -u` 清除代理变量并固定 TLS 协议，以绕过会中断 Gradle 握手的本地代理。若你的 runner 不使用代理，这些设置也是安全的空操作，可保留。
- [ ] **许可证**：`LICENSE` 默认版权归属 `WCPE`（`Copyright (c) 2026 WCPE`），改成你的名字，或更换为你需要的协议。
- [ ] **本文件**：把上方标题、简介改成你的项目信息。

## 文档

| 文档 | 说明 |
| --- | --- |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 模块拓扑、技术栈、构建系统、分层约定。 |
| [API.md](docs/API.md) | 对外 API 模块定位、发布坐标、接入方式、编写约定。 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | 开发环境、构建、编码与提交规范、PR 流程。 |
| [CODE_QUALITY.md](docs/CODE_QUALITY.md) | 代码质量与安全审查工具链：工具清单、运行方式、规则配置。 |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志（历史归档于 `docs/changelog/`）。 |
| `.claude/rules/` | 强制工程规范（注释 / 配置 / 测试 / 对外 API / 提交）。 |
| `.claude/skills/release-version/` | 本地发版流程技能。 |

## 许可证

本项目基于 [MIT 许可证](LICENSE) 开源。
