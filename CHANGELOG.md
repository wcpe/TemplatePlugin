# 更新日志（CHANGELOG）

本文件遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 风格，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)（SemVer）。

> 约定：本文件**只保留最新 5 个版本**的条目，更旧的版本归档在 `docs/changelog/vX.Y.Z.md`。
> 发版流程由 `.claude/skills/release-version/SKILL.md` 规范，版本号来源是 `gradle.properties` 的 `version=`。

## [Unreleased]

### 新增
- 新增 GitHub Actions 工作流：CI（`master` 推送与 PR 触发，构建并测试）与 Release（`v*` 标签发布正式版）。两者均调度到自托管 runner，统一设置作业超时，不使用 `actions/cache`，也不上传 workflow 制品。
- 新增「预览版发布」工作流：仅手动触发，把 `latest` 标签移到触发时的提交并刷新预发布，产物文件名带 `latest-<短SHA>-<日期>` 标识，与正式版互不覆盖。
- 默认启用 Gradle 配置缓存（`org.gradle.configuration-cache=true`，并配 `problems=fail`）。
- 新增 `.editorconfig`：统一缩进（Java 2 空格，跟随 Checkstyle 的 Google 风格；Kotlin 与 `*.kts` 4 空格）、行宽上限与末尾换行，IDE 与 ktlint 共用同一份约定。
- 新增 GitHub Dependabot 配置（Gradle 与 Actions 依赖自动升级 PR，按 ecosystem 分组减少 PR 数）与 PR 模板、缺陷报告 / 功能建议表单。
- 新增最小可运行示例：`api` 的契约接口 `GreetingService`、`project:core` 的 IoC 服务与 `/template greet <玩家名>` 命令、`lang/zh_CN.yml` 文案，以及两个 JUnit 5 测试。示例已适配全部质量门禁，可直接整块删除得到空骨架。

### 变更
- Release 改为**仅由 `v*` 标签触发**（原会随 master 推送同时更新 `latest` 预发布，现该职责移交预览版工作流），并校验标签指向默认分支、标签版本与 `gradle.properties` 一致。
- Release 与预览版改用 `softprops/action-gh-release` 创建，不再依赖 runner 上是否安装 `gh` CLI；正式版说明开启 GitHub 原生自动笔记（依据合并的 PR 生成 What's Changed）。
- 质量门禁由 `qualityCheck` 聚合任务改为直接挂在 `check` 上（见下方重构条目），CI 两步相应简化为「质量门禁与测试」（`check`）与「构建产物」（`build`），第一步失败则第二步不执行。
- 升级工作流中的 action 版本（`actions/checkout` v7、`actions/setup-java` v6、`softprops/action-gh-release` v3、`actions/github-script` v9），消除 Node.js 20 与 setup-java v4 弃用警告。
- 修复 Checkstyle 抑制文件路径解析：原默认值按子项目目录解析，导致根目录 `config/checkstyle/` 下的抑制文件静默失效；现显式传入绝对路径。
- 补充注释规范：Javadoc 摘要首句需以 ASCII 句点结尾（Checkstyle `SummaryJavadoc` 要求）。
- **构建脚本改为声明式配置**：删除 `qualityCheck` 聚合任务、`gradle.projectsEvaluated` 接线、`qualityTaskNames` 字符串集合，以及 `afterEvaluate` 中递归反射摘除依赖的逻辑。各质量插件默认已把任务挂入 `check`，脚本不再需要任何任务级接线，`build.gradle.kts` 从 262 行降到 155 行。
- 删除 `-PenableErrorProne` 开关：一个布尔量原本同时控制「应用插件 + 切 toolchain + 设 release」三件事，逼出两套编译路径。现编译工具链固定 JDK 17（CI 与 IoC 插件本就要求 17），以 `--release 8` 产出 Java 8 字节码，并校验只调用 Java 8 存在的 API。
- 删除散落各处的 JDK 版本 `onlyIf` 判定与 Checkstyle 的 `javaLauncher` 绑定补丁（工具链固定 17 后不再需要）。
- ktlint 对构建脚本的豁免改由 `filter` 排除 `*.gradle.kts`，不再靠 `tasks.matching` 按任务名关任务（插件改名即失效）。
- 构建配置清理：`useJUnitPlatform()` 统一到根 `subprojects`（此前只有 `project:core` 配了，`api` 与 `plugin` 写 JUnit 5 测试会跑不起来）；移除 `settings.gradle.kts` 中与根脚本重复的 IoC 插件版本声明、`api` 发布配置里的调试 `println`、指向不存在目录的 `fileTree("libs")`，以及 Kotlin 1.5 起已废弃的 `kotlin.incremental.java` 属性。
- 文档同步：重写 `docs/CODE_QUALITY.md` 对齐 `check` + `build` 结构，更新 `README.md`、`ARCHITECTURE.md`、`CONTRIBUTING.md` 与 `.claude/rules/comments.md`，并补充 IoC 注解需手动声明依赖（group 为 `top.wcpe.taboolib.ioc`）这一易踩点。

## [0.0.1] - 2026-06-20

### 新增
- 初始化 TabooLib Bukkit 插件模板：`api` / `project:core` / `plugin` 三个 Gradle 模块。
- 接入 `top.wcpe.taboolib.ioc` 依赖注入、KSP + EasyQuery 持久化、JUnit 5 测试。
- `api` 模块配置 `maven-publish`，发布对外契约（含源码 JAR）。
- 内置 `.claude/` 工程规范（注释 / 配置文件 / 测试 / 对外 API 语言 / Git 提交）与发版技能。
- 补齐项目文档：README、ARCHITECTURE、API（`docs/API.md`）、CONTRIBUTING、CHANGELOG。
- 采用 MIT 许可证（`LICENSE`）开源。

[Unreleased]: https://github.com/your-org/your-repo/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/your-org/your-repo/releases/tag/v0.0.1
