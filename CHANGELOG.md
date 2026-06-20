# 更新日志（CHANGELOG）

本文件遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 风格，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)（SemVer）。

> 约定：本文件**只保留最新 5 个版本**的条目，更旧的版本归档在 `docs/changelog/vX.Y.Z.md`。
> 发版流程由 `.claude/skills/release-version/SKILL.md` 规范，版本号来源是 `gradle.properties` 的 `version=`。

## [Unreleased]

### 新增
- 新增 GitHub Actions 工作流：CI（`master` 推送与 PR 触发，构建测试并上传插件产物）与 Release（`v*` 标签发布正式版、`master` 推送更新 `latest` 预发布）。

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
