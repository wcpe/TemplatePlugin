# 贡献指南（CONTRIBUTING）

感谢参与本项目。提交代码前请通读本指南与 `.claude/rules/` 下的工程规范。

## 1. 开发环境

| 工具 | 要求 |
| --- | --- |
| JDK | 8 或更高。项目编译目标为 Java 8，由 Gradle toolchain 管理；若本机无 JDK 8，请安装或开启 Gradle toolchain 自动获取。 |
| Gradle | 无需单独安装，使用仓库自带 Wrapper（`./gradlew`，Gradle 8.9）。 |
| IDE | 推荐 IntelliJ IDEA，安装 Kotlin 插件。 |

> Windows 用 `gradlew.bat`，Unix 用 `./gradlew`。

## 2. 构建与测试

| 命令 | 用途 |
| --- | --- |
| `./gradlew build` | 构建发行版本（不含 TabooLib 本体）+ 运行测试。 |
| `./gradlew taboolibBuildApi -PDeleteCode` | 构建开发版本（含 TabooLib 本体，不可直接运行）。 |
| `./gradlew test` | 运行单元测试（JUnit 5）。 |
| `./gradlew :api:publishToMavenLocal` | 将 `api` 模块发布到本地 Maven 仓库。 |

提交前至少跑通 `./gradlew build`。

## 3. 项目结构

模块拓扑、技术栈、分层约定见 [ARCHITECTURE.md](ARCHITECTURE.md)；对外 API 约定见 [API.md](docs/API.md)。简言之：

- `api`：对外契约（Java 公开类型，发布到 Maven）。
- `project:core`：业务实现（Kotlin + IoC + EasyQuery）。
- `plugin`：组装层（打包产出最终插件 JAR）。

## 4. 编码规范

本仓库的强制规范集中在 `.claude/rules/`，提交前请自查：

| 规范 | 文件 | 要点 |
| --- | --- | --- |
| 注释 | [comments.md](.claude/rules/comments.md) | 所有注释必须简体中文。 |
| 配置文件 | [config-files.md](.claude/rules/config-files.md) | YAML 文件名 / 字段名用 kebab-case；每个字段上方写中文注释。 |
| 测试 | [testing.md](.claude/rules/testing.md) | 方法名英文 lowerCamelCase + 中文 `@DisplayName`。 |
| 对外 API 语言 | [public-api-language.md](.claude/rules/public-api-language.md) | `api` 模块公开类型必须 Java；不暴露内部实现类型。 |
| Git 提交 | [git-commit.md](.claude/rules/git-commit.md) | 提交标题 / 正文必须中文；禁止 AI 署名；禁止入库过程性文档。 |

通用原则：**精准修改**——只改与当前任务直接相关的代码，不顺手大规模重排无关内容。

## 5. 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/)，**标题与正文必须简体中文**（type/scope 用英文小写）。详见 [git-commit.md](.claude/rules/git-commit.md)。

```
<type>(<scope>): <中文描述>

- 改动要点 1
- 改动要点 2
```

- 常用 type：`feat` / `fix` / `refactor` / `docs` / `chore` / `test` / `build` / `ci`。
- 常用 scope：`api` / `core` / `plugin` / `i18n` / `build`。
- **禁止**任何 AI 署名或尾注（`Co-authored-by: Claude` 等）。
- **禁止**把计划 / 需求 / 实施报告等过程性文档入库（详见 git-commit.md §2）。
- 一次提交只做一件事；不跳过 hooks（`--no-verify`）；不对已 push 的提交 `--amend`。

## 6. Pull Request

1. 从最新主分支切出特性分支（如 `feat/xxx`、`fix/xxx`）。
2. 保证 `./gradlew build` 通过、测试不回退。
3. PR 描述说明动机与改动要点，关联相关 issue。
4. 涉及对外 API / 配置 / 行为变更时，同步更新 [API.md](docs/API.md) / [CHANGELOG.md](CHANGELOG.md) 等文档。

## 7. 发版

发版（更新版本号、推导 SemVer、同步文档、构建验证、打本地 tag）由发版技能规范：`.claude/skills/release-version/SKILL.md`。该技能**只创建本地提交与 tag，绝不推送远程**。

## 8. 首次使用本模板

如果你是基于本模板新建项目，请先完成 [README.md](README.md#首次使用修改清单) 中的"首次使用修改清单"，替换项目标识（group、名称、Maven 私服、包名等）。
