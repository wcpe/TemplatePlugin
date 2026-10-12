# 代码质量审查

本模板内置一套 Java / Kotlin 代码质量与安全审查工具链，覆盖缺陷检测、代码异味、样式、安全与测试覆盖率。

所有工具由 Gradle 插件在 `subprojects` 中统一应用，**它们的任务默认就挂在 `check` 上**，构建脚本不做任何任务级接线。因此质量门禁没有独立的聚合命令：跑 `check` 即跑全部门禁。

## 1. 工具总览

### Java

| 维度 | 工具 | 说明 |
| --- | --- | --- |
| 缺陷检测（字节码） | SpotBugs | 分析编译后的字节码，定位潜在 bug |
| 缺陷检测（编译期） | Error Prone | 挂入 `javac`，在编译期发现常见错误 |
| 安全审查 | FindSecBugs | 作为 SpotBugs 插件挂载，覆盖安全类缺陷 |
| 代码异味 / 源码规则 | PMD | 官方 `quickstart` 规则集 |
| 样式审查 | Checkstyle | 官方 Google 规则集（`config/checkstyle/checkstyle.xml`） |
| 测试覆盖率 | JaCoCo | Java 模块的覆盖率报告 |

### Kotlin

| 维度 | 工具 | 说明 |
| --- | --- | --- |
| 缺陷 + 异味 + 复杂度 | detekt | 官方默认规则集 |
| 样式 / 格式化 | ktlint | 官方 `ktlint_official` 规则集 |
| 测试覆盖率 | Kover | Kotlin 模块的覆盖率报告 |

## 2. 运行环境

> **关键点**：这些工具运行在「启动 Gradle 的 JVM（守护进程）」上，而不是项目的编译工具链。
> SpotBugs / Error Prone / detekt / Checkstyle 10 / PMD 7 均要求 **JDK 11+（推荐 17）**；
> 本项目编译工具链固定为 **JDK 17**，满足要求。

- 构建与配置需要 **JDK 17**（TabooLib IoC 插件为 Java 17 字节码，低版本无法加载）。
- 产物仍是 **Java 8 字节码**：编译走 `options.release = 8`，既锁定语言级别，也校验「只调用 Java 8 存在的 API」，可直接跑在 Java 8 的服务器上。
- 质量任务的报告输出在各模块的 `build/reports/` 下（HTML / XML）。

## 3. 本地运行

```bash
# 质量门禁 + 测试（PMD / Checkstyle / SpotBugs / Error Prone / detekt / ktlint + JUnit 5）
./gradlew check

# 完整构建：assemble + check
./gradlew build
```

- 任一规则违规即 **构建失败**（严格阻断，各工具均未开 `ignoreFailures`）。
- `build` 已包含 `check`，本地不必再单独跑一遍。
- 覆盖率报告**不在** `check` 中自动生成，需要时按模块类型单独跑：

```bash
./gradlew koverXmlReport        # 含 src/main/kotlin 的模块
./gradlew jacocoTestReport      # 其余模块
```

也可单独运行某个工具：`./gradlew detekt`、`./gradlew ktlintCheck`、`./gradlew pmdMain`、`./gradlew checkstyleMain`、`./gradlew spotbugsMain`。

## 4. 模块按需启用

为避免误报与代理冲突，工具按模块源码类型自动启用：

- **SpotBugs / Error Prone**：仅当模块存在 `src/main/java` 时启用（避免分析 Kotlin 字节码产生误报）。
- **覆盖率**：含 `src/main/kotlin` 的模块用 **Kover**，否则用 **JaCoCo**（二者都挂测试 JVM 代理，互斥应用避免冲突）。
- **PMD / Checkstyle**：仅扫描 `src/*/java`，无 Java 源码时空过。
- **detekt / ktlint**：扫描 `.kt` 源码；**不检查** Gradle 构建脚本（`*.kts`），生成代码（`build/`、`generated/`）也被排除。

> 模板自带的示例代码覆盖了 Java 与 Kotlin 两侧，可直接验证各工具是否生效；删掉示例后各任务转为空过。

## 5. CI 集成

质量门禁已在 `.github/workflows/ci.yml` 中接入，作为产物构建的前置阻断步骤（非独立 job，复用同一个 runner 与已解析的依赖）：

```yaml
- name: 质量门禁与测试
  run: ./gradlew check --stacktrace --no-daemon

- name: 构建产物
  run: ./gradlew build --stacktrace --no-daemon
```

任何规则违规都会在第一步失败，第二步不会执行，因此**违规代码不会被产出成插件 JAR**。第二步的 `build` 会复用第一步的 `check` 结果（up-to-date），只补齐产物打包，很快。

本地执行同样命令即可复现：

```bash
./gradlew check
```

## 6. 配置缓存

项目默认启用 Gradle **配置缓存**（`gradle.properties` 中 `org.gradle.configuration-cache=true`，并配 `problems=fail` 以免静默降级）。`check`（含全部质量工具）与 `build` 均完全兼容，第二次起复用任务图，构建启动明显变快。

若新增构建逻辑后出现 configuration cache 不兼容，构建会直接失败并指出具体行号；此时应改用兼容写法（避免在执行阶段读取 `Project`/`Task` 实例、使用 `providers` API 等），而不是关闭配置缓存。

## 7. 规则配置与例外

| 工具 | 配置位置 | 调整方式 |
| --- | --- | --- |
| Checkstyle | `config/checkstyle/checkstyle.xml` | 直接编辑（内容为官方 Google `google_checks.xml` 原版）；用 `config/checkstyle/checkstyle-suppressions.xml` 抑制（路径已在构建脚本中显式指定为绝对路径） |
| 缩进与行宽 | `.editorconfig` | Java 2 空格（跟随 Checkstyle Google 风格），Kotlin 与 `*.kts` 4 空格，`max_line_length = 140` |
| PMD | 根 `build.gradle.kts` 的 `ruleSets` | 改为自定义 ruleset 文件或增减分类 |
| SpotBugs / FindSecBugs | 根 `build.gradle.kts` | 可配置 `excludeFilter` 排除过滤器 |
| detekt | 官方默认 | 可新增 `detekt.yml` 并开启 `buildUponDefaultConfig` |
| ktlint | 官方 `ktlint_official` | 通过项目 `.editorconfig` 调整 |

行内抑制示例：
- Java：`@SuppressWarnings("PMD.XxxRule")`、`@SuppressFBWarnings(...)`、Checkstyle 注释 `// CHECKSTYLE.OFF: Xxx`。
- Kotlin：`@Suppress("DetektRuleId")`、ktlint 行内 `// ktlint-disable`（或 `@Suppress("ktlint:standard:rule-id")`）。

### 7.1 Javadoc 首句句点（易踩）

`SummaryJavadoc` 要求 Javadoc 摘要**首句以 ASCII 句点 `.` 结尾**，中文句号 `。` 会报
`Javadoc的第一句缺少一个结束时期`。因 `maxWarnings = 0` 严格阻断，这会让门禁直接失败：

```java
/** 探针类，用于验证质量工具链是否生效. */   // ✅ ASCII 句点
/** 探针类，用于验证质量工具链是否生效。 */   // ❌ 触发违规
```

Kotlin 的 KDoc 不触发该规则，可正常使用中文句号。详见 `.claude/rules/comments.md` §4。

### 7.2 ktlint_official 的两条格式硬规则（易踩）

模板自带的示例代码已按下列规则书写，改造代码时注意：

- **类体首行不能有空行**：KDoc 之后要直接跟第一行成员，否则报 `no-empty-first-line-in-class-body`。
- **多行表达式要换行**：`val x = subCommandExec { ... }` 这类多行赋值，`=` 后要换行再写表达式：

  ```kotlin
  val greet =
      subCommandExec<ProxyCommandSender> {
          sender.sendLang("greet-command-reply", sender.name, args)
      }
  ```

- 文件末尾必须留一个换行（detekt `NewLineAtEndOfFile`），`.editorconfig` 的 `insert_final_newline = true` 对 IDE 生效，但命令行写入的文件需自行保证。

## 8. 工具版本

| 工具 | 版本 |
| --- | --- |
| SpotBugs 插件 / 引擎 | `6.5.8` / `4.8.6` |
| FindSecBugs | `1.13.0` |
| Error Prone 插件 / 引擎 | `4.1.0` / `2.36.0` |
| PMD | `7.7.0` |
| Checkstyle | `10.21.0` |
| JaCoCo | `0.8.11`（未显式指定，取 Gradle 8.9 内置默认值） |
| detekt | `1.23.8` |
| ktlint 插件 | `12.1.2` |
| Kover | `0.9.1` |
