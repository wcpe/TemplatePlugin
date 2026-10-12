# 架构说明（ARCHITECTURE）

> 本文档记录模板的**模块拓扑、技术栈与分层约定**，是发版时同步架构事实的依据（见 `.claude/skills/release-version/SKILL.md`）。
> 当前模板**自带一套最小可运行示例**（见 §3.4），删掉即得空骨架；下文描述的既是现有约定，也是落地时的组织依据。

## 1. 概述

这是一个基于 [TabooLib](https://github.com/TabooLib/taboolib) 6.x 的 **Bukkit 插件模板**，采用 Gradle 多模块结构，内置：

- 对外发布的 `api` 模块（Java 公开契约，发布到 Maven）。
- 业务实现模块 `project:core`（Kotlin + IoC + EasyQuery 持久化）。
- 组装模块 `plugin`（产出最终插件 JAR）。
- 一套 `.claude/` 工程规范（注释 / 配置 / 测试 / 对外 API / 提交）与发版技能。

## 2. 技术栈

| 维度 | 选型 | 版本 |
| --- | --- | --- |
| 构建工具 | Gradle Wrapper | 8.9 |
| 插件框架 | TabooLib | 6.3.0-wcpe.1 |
| TabooLib Gradle 插件 | `io.izzel.taboolib` | 2.0.39-wcpe.1 |
| 语言 | Kotlin / Java | Kotlin 2.1.0 / Java 8（编译工具链**固定 JDK 17**，以 `--release 8` 产出 Java 8 字节码，可在 Java 8 服务器运行） |
| 依赖注入 | `top.wcpe.taboolib.ioc` | 0.0.11 |
| 注解处理 | KSP（`com.google.devtools.ksp`） | 2.1.0-1.0.29 |
| 持久化 ORM | EasyQuery（`com.easy-query`） | 3.1.82 |
| 测试框架 | JUnit 5（`junit-jupiter`） | 5.11.4 |
| Minecraft 服务端 API | `ink.ptms.core`（NMS，仅 `compileOnly`） | v12004 |

TabooLib 安装的模块：`Basic`、`Bukkit`、`BukkitUtil`、`BukkitUI`、`CommandHelper`、`I18n`。

## 3. 模块拓扑

### 3.1 模块清单

共 **3 个 Gradle 模块**，**1 个**对外发布模块（`api`）。

| 模块 | 主要语言 | 是否发布到 Maven | 职责 |
| --- | --- | --- | --- |
| `api` | Java（公开类型）| ✅ 是 | 对外契约：接口 / 注解 / 枚举 / DTO。供三方插件与上层模块依赖。 |
| `project:core` | Kotlin | ❌ 否 | 业务实现：服务、仓库、领域逻辑、EasyQuery 实体与持久化。 |
| `plugin` | Kotlin | ❌ 否 | 组装层：TabooLib 插件描述、IoC 接管，把 `api` + `core` 打进最终 JAR。 |

> 模块由 `settings.gradle.kts` 的 `include(...)` 声明。新增模块需同步更新本表、§3.2 路径映射，以及（若为发布模块）§5。

### 3.2 Gradle 路径 ↔ 磁盘路径映射

| Gradle 路径 | 磁盘路径 | 构建脚本 |
| --- | --- | --- |
| `:api` | `api/` | `api/build.gradle.kts` |
| `:project:core` | `project/core/` | `project/core/build.gradle.kts` |
| `:plugin` | `plugin/` | `plugin/build.gradle.kts` |

### 3.3 依赖流向

```
        ┌─────────────────────────────────────────────┐
        │                  plugin                      │  ← subproject=false，最终插件
        │  taboo(:api)  +  taboo(:project:core)        │
        │  jar 内打包 api / core 的 main 输出          │
        └───────────────┬───────────────┬─────────────┘
                        │               │
              compileOnly│               │taboo(打包)
                        ▼               ▼
                ┌───────────────┐   ┌───────┐
                │ project:core  │──▶│  api  │   core compileOnly(:api)
                │ (实现/持久化) │   │(契约) │
                └───────────────┘   └───────┘
```

- `plugin` 通过 `taboo(...)` 依赖 `api` 与 `project:core`，并在 `jar` 任务里把二者的 `main` 输出一并打包，产出单一插件 JAR。
- `project:core` 以 `compileOnly(project(":api"))` 依赖契约，运行期由 `plugin` 提供。
- `api` 不依赖任何业务模块，仅由根 `subprojects {}` 提供 `kotlin-stdlib` 与 `ink.ptms.core`（均 `compileOnly`）。
- 依赖方向单向：`plugin → core → api`，**严禁反向依赖**（`api` 不得引用 `core`/`plugin` 的类型）。

### 3.4 源码目录结构

模板自带的示例代码分布如下（路径以仓库根为基准）：

```
api/src/main/java/top/wcpe/mc/plugin/template/api/
  └─ GreetingService.java                 对外契约接口（Java）

project/core/src/main/kotlin/top/wcpe/mc/plugin/template/core/
  ├─ service/GreetingServiceImpl.kt       IoC 托管的实现（@Service("greetingService")）
  └─ command/TemplateCommand.kt           命令示例（/template greet <玩家名>）

project/core/src/main/resources/lang/
  └─ zh_CN.yml                            i18n 文案

project/core/src/test/kotlin/top/wcpe/mc/plugin/template/core/service/
  └─ GreetingServiceImplTest.kt           JUnit 5 测试
```

几点约定：

- `api` 的公开类型走 `src/main/java`（Java 编写），其余模块的源码在 `src/main/kotlin`。
- i18n 文案固定放 `src/main/resources/lang/`，文件名用语言代码（如 `zh_CN.yml`）。
- 测试源码在 `src/test/kotlin`；`useJUnitPlatform()` 已在根 `build.gradle.kts` 的 `subprojects` 中统一配置，各模块不必重复声明。
- 删掉上述文件即得空骨架，其余内容不受影响。

## 4. 构建系统

### 4.1 构建命令

| 命令 | 用途 |
| --- | --- |
| `./gradlew check` | **质量门禁 + 测试**：PMD / Checkstyle / SpotBugs / Error Prone / detekt / ktlint 与 JUnit 5 一并执行，任一违规即失败。这些工具由各插件默认挂入 `check`，构建脚本里没有任何任务级接线。 |
| `./gradlew build` | assemble + check，即构建**发行版本**（不含 TabooLib 本体）并跑全部门禁与测试。 |
| `./gradlew taboolibBuildApi -PDeleteCode` | 构建**开发版本**（含 TabooLib 本体，供开发者使用、不可直接运行）；`-PDeleteCode` 移除逻辑代码以减小体积。 |
| `./gradlew :api:publishToMavenLocal` | 把 `api` 模块发布到本地 Maven 仓库（联调 / 校验产物用）。 |
| `./gradlew test` | 仅运行测试（`project:core` 使用 JUnit 5）。 |
| `./gradlew koverXmlReport` / `./gradlew jacocoTestReport` | 手动生成覆盖率报告。**覆盖率不在 `check` 里自动出报告**：含 `src/main/kotlin` 的模块用 Kover，其余用 JaCoCo，按模块择一执行。 |

### 4.2 打包逻辑

`plugin` 模块的 `jar` 任务：

- `archiveBaseName` 设为 `rootProject.name`。
- 通过 `from(project(":api").sourceSets["main"].output)` 与 `from(project(":project:core").sourceSets["main"].output)`，把 `api`、`core` 的编译产物合并进 `plugin` 的最终 JAR。

因此**最终插件 JAR 由 `plugin` 模块产出**，包含全部三个模块的字节码。

### 4.3 TabooLib 与 IoC

- 根 `build.gradle.kts` 对所有 `subprojects` 应用 `io.izzel.taboolib` 并设 `subproject = true`；`plugin` 模块单独设 `subproject = false`，作为真正的插件入口。
- IoC 由 `top.wcpe.taboolib.ioc` 提供。`plugin` 的 `taboolibIoc { autoTakeover(true); analysisFailOnError(true) }` 表示：自动注入 IoC 依赖并追加 relocate 规则；静态诊断发现 error 直接拦截构建。
- 业务代码使用 IoC 注解（`@Service` / `@Repository` / `@Inject` 等）组织，避免手写单例与全局状态。

> **易踩的坑**：TabooLib IoC 的 Gradle 插件只做**接管与 relocate**，**不会**把注解自动加进编译期 classpath。用到 `@Service` / `@Inject` 的模块必须自己声明依赖，否则编译期报 `Could not find`：
>
> ```kotlin
> dependencies {
>     compileOnly("top.wcpe.taboolib.ioc:taboolib-ioc-annotation:1.1.0")
> }
> ```
>
> 注意 group 是 `top.wcpe.taboolib.ioc`，**不是** `top.wcpe.taboolib`——写成后者会一直找不到依赖。当前 `project/core/build.gradle.kts` 即按此声明，版本与插件接管的 IoC 运行时保持一致。

### 4.4 质量门禁与格式约定

- 质量工具与 JUnit 5 由各插件**默认挂入 `check`**，不再需要任何任务级接线。
- SpotBugs 与 Error Prone **仅在模块存在 `src/main/java` 时启用**，避免对 Kotlin 字节码误报。
- ktlint **不检查 Gradle 构建脚本**（`*.gradle.kts` 已被排除）。
- 根目录新增 `.editorconfig` 统一格式：Java 2 空格缩进（跟随 Checkstyle 的 Google 风格），Kotlin 与 kts 4 空格，`max_line_length = 140`。改动该文件会影响 ktlint 结果，改前先跑一次 `./gradlew ktlintCheck`。

工具清单与规则细节见 [CODE_QUALITY.md](docs/CODE_QUALITY.md)。

## 5. 发布模块（api）

`api` 是当前唯一发布到 Maven 的模块，配置见 `api/build.gradle.kts`：

| 项 | 值 |
| --- | --- |
| `groupId` | `project.group`（即 `gradle.properties` 的 `group`，当前 `top.wcpe.mc.plugin.template`） |
| `artifactId` | `"${rootProject.name}-api".lowercase()`（当前 `templateplugin-api`） |
| `version` | `project.version`（即 `gradle.properties` 的 `version`） |
| 附带产物 | `withSourcesJar()` —— 同时发布源码 JAR |
| 发布仓库 | `maven.wcpe.top` 的 releases / snapshots（按版本号是否以 `SNAPSHOT` 结尾切换）+ `mavenLocal()` |
| 凭据 | `username` / `password` 工程属性（由 `~/.gradle/gradle.properties` 或 `-P` 提供） |

> `artifactId` 取决于 `rootProject.name`（`settings.gradle.kts` 已固定为 `TemplatePlugin`）。详见 `.claude/rules/public-api-language.md` §5。

公开 API 的语言约束（必须 Java、不得暴露内部类型等）见 [API.md](docs/API.md) 与 `.claude/rules/public-api-language.md`。

## 6. 分层约定与依赖方向

推荐的代码分层（自底向上，依赖单向）：

```
api（对外契约，Java）
  └─ project:core
       ├─ database/entity      EasyQuery 实体（KSP 生成 EntityProxy）
       ├─ database/repository  仓库层：只做数据存取
       ├─ service              业务编排 / 事务边界
       └─ domain               领域逻辑
  └─ plugin
       ├─ command              命令（CommandHelper）
       ├─ listener             事件监听
       └─ <PluginMain>         插件入口（TabooLib object + 生命周期）
```

约束：

- 依赖单向：`command/listener → service → repository → entity`，不得反向或跨层穿透。
- `api` 只放对外契约，不依赖 `core`/`plugin`。
- 业务逻辑不写进 `repository`（仓库只做存取）；Bukkit API 不下沉到与平台无关的纯逻辑层。

## 7. 持久化（EasyQuery + KSP）

- `project:core` 应用 `com.google.devtools.ksp`，引入 EasyQuery 的 `sql-api-proxy`（`compileOnly`）与 `sql-ksp-processor`（`ksp`）。
- KSP 在编译期为实体生成 `EntityProxy` 类，输出到 `build/generated/ksp/main/kotlin`，并通过 `kotlin.srcDir(...)` 纳入 `main` 源集。
- 采用 Code First：以实体类定义为准，**禁止**手写 SQL 建表脚本初始化结构。

## 8. 配套规范与文档

| 内容 | 位置 |
| --- | --- |
| 工程规范（注释 / 配置 / 测试 / 对外 API / 提交） | `.claude/rules/*.md` |
| 发版流程技能 | `.claude/skills/release-version/SKILL.md` |
| 对外 API 说明 | [API.md](docs/API.md) |
| 贡献与开发指南 | [CONTRIBUTING.md](CONTRIBUTING.md) |
| 更新日志 | [CHANGELOG.md](CHANGELOG.md)（历史归档于 `docs/changelog/`） |
| 快速上手 | [README.md](README.md) |
