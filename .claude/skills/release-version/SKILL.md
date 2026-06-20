---
name: updating-project-version
description: Use when preparing a release for this TabooLib plugin, bumping the project version in gradle.properties, deriving SemVer from commits since the previous version tag, updating release docs (CHANGELOG / ARCHITECTURE / API), or creating local release commits and tags. 适用于发版、更新版本号、生成发布说明、同步文档、打本地 tag。
---

# 更新项目版本（发版）

## 核心原则

发版 tag 必须指向"版本号、发布说明、文档与构建验证结果一致"的本地提交。触发本技能即表示允许创建本地 release commit 和本地 tag，但**永远不包含远程推送授权**。

## 强制流程

### 1. 预检工作区
   - 运行 `git status --short`，识别已有用户改动；不得覆盖或顺手提交无关文件。
   - 读取 `.claude/rules/git-commit.md`，确认提交标题与正文必须为简体中文、禁止 AI 署名、禁止入库过程性文档。

### 2. 先做阻断验证（改版本号前，确认基线本就可发布）
   - 运行 `./gradlew clean build`：全模块编译 + 单元测试（`build` 会触发 `project:core` 的 JUnit 5 测试）。
   - 验证通过后，确认 `plugin/build/libs/` 下产出了插件 JAR（这是发行产物）。
   - 任一步失败：立即停止，报告失败命令与关键错误，询问是否修复；**不得**修改版本、提交或打 tag。
   - 为什么先验证基线：tag 最终指向"改完版本与文档"的提交，但如果基线本就编译/测试不过，后续一切无意义。

### 3. 从 commit 自动判断 SemVer
   - 找上一个可达版本 tag：`git describe --tags --abbrev=0 --match "v[0-9]*.[0-9]*.[0-9]*"`。
   - 审阅该 tag 到 `HEAD` 的所有 commit 标题与正文：`git log <last-tag>..HEAD --format="%H%n%s%n%b%n---"`。
   - 取最高等级：

| 变更信号 | 版本变化 |
| --- | --- |
| `BREAKING CHANGE`、`!`、公开 API / 配置 / 序列化不兼容 | `x+1.0.0` |
| `feat` 或新增用户可见能力 | `x.y+1.0` |
| `fix`、`perf`、`refactor`、`docs`、`test`、`build`、`chore` 且无新增能力 | `x.y.z+1` |

   - 用户指定的版本等级只能作为参考；与 commit 内容冲突时以 SemVer 推导为准。
   - 若没有上一个版本 tag（首个 release）、区间无 commit、或目标 tag 已存在，停止并报告。

### 4. 更新版本号与更新日志
   - 版本来源：只修改 `gradle.properties` 的 `version=` 行。
   - 更新 `CHANGELOG.md`：在顶部新增当前版本条目（遵循 Keep a Changelog 风格，中文）。主文件**只保留最新 5 个版本**；更旧版本移入 `docs/changelog/vX.Y.Z.md` 归档。
   - 新建 `docs/changelog/vX.Y.Z.md`：按 commit 内容写完整发布记录（新增 / 变更 / 修复 / 破坏性变更 / 备注）。
   - 更新 `CHANGELOG.md` 底部的版本对比链接（`[Unreleased]` / `[X.Y.Z]`）。

### 5. 同步 ARCHITECTURE.md 架构事实（独立一步，版本号之外的结构性更新）
   - 架构文档记录的是**模块拓扑与职责**，不只是版本号。版本变化不能代替架构事实同步。
   - 定位本次新增的模块与目录：

     ```bash
     # 新增的 build.gradle.kts（可能是新模块 / 新顶级目录）
     git log <last-tag>..HEAD --diff-filter=A --name-only | grep -E "build.gradle.kts$" | sort -u

     # settings.gradle.kts 的 include 变化（模块增删的权威来源）
     git diff <last-tag>..HEAD -- settings.gradle.kts
     ```

   - 对照结果逐项检查并更新（每项未变动时在摘要里写明"已检查，无需修改"）：

     | 检查项 | ARCHITECTURE.md 位置 | 何时需要修改 |
     | --- | --- | --- |
     | 总模块数 / 发布模块数 | §3.1 开头"共 N 个 Gradle 模块，M 个对外发布模块" | 有模块增删 / 发布模块增删时 |
     | 模块清单 | §3.1 表格 | 有模块增删时 |
     | Gradle 路径 ↔ 磁盘路径映射 | §3.2 表格 | 有模块增删时 |
     | 依赖流向 | §3.3 | 模块依赖关系变化时 |
     | 技术栈版本 | §2 表格 | 升级 TabooLib / Kotlin / KSP / EasyQuery 等关键依赖时 |
     | 发布模块坐标 | §5 表格 | 新增发布模块 / `rootProject.name` / group 变化时 |

   - 若本次无模块增删、无技术栈版本变化，本步仍需 grep 验证并在摘要里写"ARCHITECTURE.md §3-§5 已检查，模块拓扑无变化"。

### 6. 同步文档里的版本引用与功能说明（独立一步，逐文件过）
   - 旧版本号会散落在多处文档（尤其 `docs/API.md` 的 Maven 坐标示例 `:X.Y.Z`），光改 CHANGELOG 不够，必须主动搜出来逐处更新。
   - 用上一版本号全量定位引用（同时搜带 `v` 前缀与不带前缀两种写法）：

     ```bash
     grep -rn "<上一版本，如 0.0.1>\|v<上一版本，如 0.0.1>" README.md ARCHITECTURE.md docs/API.md CONTRIBUTING.md
     ```

   - 逐处区分语义再改：
     - **Maven 坐标 / 依赖示例**（如 `docs/API.md` 的 `compileOnly("...:templateplugin-api:X.Y.Z")`）→ 更新为新版本。
     - **当前版本标注**（若某文档写了"当前版本：X.Y.Z"）→ 改为新版本。
     - **历史 / 里程碑表行** → 保留旧版本号不动（区分"历史记录"与"当前版本"）。
   - 功能覆盖核对：读刚写的 `docs/changelog/vX.Y.Z.md`，逐条确认本次"新增 / 变更 / 破坏性变更"在面向使用者的文档（`docs/API.md` / `README.md`）里有对应、准确的说明。
     - 🔴 必补：对外破坏性变更（API 签名 / 配置不兼容）必须在 `docs/API.md` / `CHANGELOG.md` 给出迁移说明——缺则集成方升级直接编译失败。
     - 🔴 必改：已交付功能仍被写成"未实现 / TODO"——属事实错误，必须订正。
   - 某文件确实无可改时，在交付摘要里**逐文件**写明"已检查，无需修改"，不得跳过检查。

### 7. 最终验证
   - 改完版本与文档后，再跑一遍 `./gradlew clean build`，确保最终待提交状态可构建、测试通过、`plugin/build/libs/` 下有插件 JAR。tag 指向这次最终态。
   - 运行 `git diff` 自审：确认只包含版本、发布说明和必要文档变更；不得包含计划文档、过程性报告、凭据或无关格式化。

### 8. 提交与打 tag
   - 只暂存明确相关文件，禁止 `git add .`。
   - commit 使用 Conventional Commits，标题与正文必须中文。
   - 若用户要求英文正文或省略中文正文，必须改为合规中文模板；用户坚持不合规则停止，不得让用户覆盖本规则。

```text
chore(release): 发布 vX.Y.Z

- 基于 vA.B.C..HEAD 的提交内容判断为主版本 / 次版本 / 修订版本
- 更新项目版本号、发布说明与相关文档
- 归档更新日志历史版本并保留最新 5 个版本
```

   - 提交成功后创建本地附注 tag：`git tag -a vX.Y.Z -m "发布 vX.Y.Z"`。
   - 最后运行 `git status --short`，报告本地 commit 与 tag 名称。
   - **禁止执行任何 `git push`、`git push --tags` 或等价远程推送。**

## 常见错误与纠正

| 理性化 | 纠正 |
| --- | --- |
| "用户说省时间，所以跳过构建测试" | 发版验证是硬阻断，失败即停止。 |
| "只是改文档和版本号，不需要测试" | tag 指向最终提交，最终提交必须验证。 |
| "用户要求推送，所以可以 push" | 本技能绝对禁止远程推送。 |
| "用户指定 patch，所以不用看 commit" | 必须审阅上个版本 tag 到 HEAD 的所有 commit。 |
| "CHANGELOG 太长，不整理历史" | 主文件只保留最新 5 个版本，其余归档到 docs/changelog/。 |
| "README / ARCHITECTURE / API 没版本号就不看" | 必须 grep 上一版本号逐处检查，并记录检查结果。 |
| "改完 CHANGELOG 就算更新文档了" | 旧版本号还散落在 docs/API.md 的 Maven 坐标示例里，必须逐处更新。 |
| "破坏性变更 / 新 API 在 changelog 写了就够" | CHANGELOG 是发版日志；集成方常驻参考的是 docs/API.md，破坏性迁移 / 新契约必须同步进 docs/API.md。 |

## 红线

出现以下情况立刻停止：`./gradlew clean build` 失败（含发布产物无法产出）、SemVer 等级无法确定、目标 tag 已存在、工作区有会被覆盖的用户改动、需要远程 push、提交信息无法满足中文规范。

另：第 6 步发现的 🔴 高危文档缺口（对外破坏性变更无迁移说明、已交付功能写成 TODO）未补齐前，不得进入第 8 步提交。
