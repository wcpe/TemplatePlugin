# 代码质量审查

本模板内置一套 Java / Kotlin 代码质量与安全审查工具链，覆盖缺陷检测、代码异味、样式、安全与测试覆盖率。

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
| 安全审查 | detekt 自带规则 + Semgrep | Semgrep 在 CI 中运行公共规则集 |
| 样式 / 格式化 | ktlint | 官方 `ktlint_official` 规则集 |
| 测试覆盖率 | Kover | Kotlin 模块的覆盖率报告 |

## 2. 运行环境

> **关键点**：这些工具运行在「启动 Gradle 的 JVM（守护进程）」上，而不是项目的 Java 8 toolchain。
> SpotBugs / Error Prone / detekt / Checkstyle 10 / PMD 7 均要求 **JDK 11+（推荐 17）** 才能运行；
> 编译产物仍是 **Java 8 字节码**（启用 Error Prone 时用 `--release 8` 保证兼容）。

- 在 **JDK 8** 上运行时，质量任务会通过 `onlyIf` 自动跳过，因此 JDK 8 的 `ci.yml`（纯编译 + 测试）不受影响。
- 本地与 CI 运行质量门禁请使用 **JDK 17 或更高**。

## 3. 本地运行

```bash
# 运行全部质量门禁与覆盖率报告（需 JDK 17+），不含 Error Prone
./gradlew qualityCheck

# 追加 Error Prone（切到 JDK 17 toolchain + --release 8）
./gradlew qualityCheck -PenableErrorProne
```

- `qualityCheck` 是聚合任务，汇集所有模块的 PMD / Checkstyle / SpotBugs / detekt / ktlint 与覆盖率报告。
- 任一规则违规即 **构建失败**（严格阻断）。
- 报告输出在各模块的 `build/reports/` 下（HTML / XML）。

也可单独运行：`./gradlew detekt`、`./gradlew ktlintCheck`、`./gradlew pmdMain`、`./gradlew checkstyleMain`、`./gradlew spotbugsMain` 等。

## 4. 与构建解耦

质量任务**不挂入** `check` / `build`，因此：

- `./gradlew build`（含 `ci.yml`）只做编译 + 测试，不触发质量门禁；JDK 8 上可正常运行。
- 质量门禁统一由 `qualityCheck` 与 `.github/workflows/quality.yml` 驱动，运行在 JDK 17 上。

## 5. 模块按需启用

为避免误报与代理冲突，工具按模块源码类型自动启用：

- **SpotBugs / Error Prone**：仅当模块存在 `src/main/java` 时启用（避免分析 Kotlin 字节码产生误报）。
- **覆盖率**：含 `src/main/kotlin` 的模块用 **Kover**，否则用 **JaCoCo**（二者都挂测试 JVM 代理，互斥应用避免冲突）。
- **PMD / Checkstyle**：仅扫描 `src/*/java`，无 Java 源码时空过。
- **detekt / ktlint**：扫描 `.kt` 源码；**不检查** Gradle 构建脚本（`*.kts`），生成代码（`build/`、`generated/`）也被排除。

> 因此在空模板上 `qualityCheck` 直接通过；随着你往各模块写代码，对应工具会自动生效。

## 6. CI

`.github/workflows/quality.yml`（独立于 `ci.yml`）：

- **gradle-quality**：JDK 17，运行 `./gradlew qualityCheck -PenableErrorProne`，上传报告产物。
- **semgrep**：使用官方镜像与公共规则集（`p/default`、`p/java`、`p/kotlin`、`p/secrets`），无需登录令牌，发现问题即阻断。

## 7. 规则配置与例外

| 工具 | 配置位置 | 调整方式 |
| --- | --- | --- |
| Checkstyle | `config/checkstyle/checkstyle.xml` | 直接编辑（默认 Google 规则）；可放置 `checkstyle-suppressions.xml` 抑制 |
| PMD | 根 `build.gradle.kts` 的 `ruleSets` | 改为自定义 ruleset 文件或增减分类 |
| SpotBugs / FindSecBugs | 根 `build.gradle.kts` | 可配置 `excludeFilter` 排除过滤器 |
| detekt | 官方默认 | 可新增 `detekt.yml` 并开启 `buildUponDefaultConfig` |
| ktlint | 官方 `ktlint_official` | 通过项目 `.editorconfig` 调整 |

行内抑制示例：

- Java：`@SuppressWarnings("PMD.XxxRule")`、`@SuppressFBWarnings(...)`、Checkstyle 注释 `// CHECKSTYLE.OFF: Xxx`。
- Kotlin：`@Suppress("DetektRuleId")`、ktlint 行内 `// ktlint-disable`（或 `@Suppress("ktlint:standard:rule-id")`）。

## 8. 工具版本

| 工具 | 版本 |
| --- | --- |
| SpotBugs 插件 / 引擎 | `6.5.8` / `4.8.6` |
| FindSecBugs | `1.13.0` |
| Error Prone 插件 / 引擎 | `4.1.0` / `2.36.0` |
| PMD | `7.7.0` |
| Checkstyle | `10.21.0` |
| JaCoCo | `0.8.12` |
| detekt | `1.23.8` |
| ktlint 插件 | `12.1.2` |
| Kover | `0.9.1` |
