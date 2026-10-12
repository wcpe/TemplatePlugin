# 注释规范

> 适用于仓库内所有源代码文件（Kotlin / Java / Gradle KTS / 配置文件等）。

## 1. 强制规则

- **所有注释必须使用简体中文。**
- 禁止使用英文、日文、拼音或其他非中文语言书写注释。
- 包括但不限于：
  - 行注释 `//`
  - 块注释 `/* ... */`
  - 文档注释 `/** ... */`（KDoc / Javadoc）
  - Kotlin / Java 中的 TODO / FIXME / NOTE 标记后的说明文字
  - Gradle KTS、YAML、Properties、JSON5 等配置文件中的注释
  - 资源文件（lang、yml）中的人工注释

## 2. 例外

以下情况**不视为注释**，不受本规则约束：

- 代码中的英文标识符（类名、方法名、变量名、包名）
- 字符串字面量、日志文案、用户可见文本
- 第三方生成代码中的原始注释（如 protoc / 生成器输出，但应尽量隔离在 `build/` 等非 VCS 目录）
- 许可证头（LICENSE header），如确需保留英文版权声明可豁免

## 3. 示例

✅ 正确：

```kotlin
// 校验玩家是否在线，离线时直接返回 null
fun resolve(uuid: UUID): Player? { ... }

/**
 * 数据包注册表。
 *
 * 通过反射收集所有标注 @Packet 的类。
 */
class PacketRegistry { ... }
```

❌ 错误：

```kotlin
// check if player online, return null when offline
fun resolve(uuid: UUID): Player? { ... }

/**
 * Packet registry.
 */
class PacketRegistry { ... }
```

## 4. Javadoc 首句的句点约定

> 本仓库强制中文注释，但 Checkstyle 的 `SummaryJavadoc` 规则（随 Google 官方规则集启用）要求
> Javadoc / KDoc 的**摘要首句必须以 ASCII 句点 `.` 结尾**，中文句号 `。` 会被判定为
> `Javadoc的第一句缺少一个结束时期` 并使质量门禁失败。

- Java 的 **Javadoc 摘要首句**统一用 ASCII 句点 `.` 结尾，例如 `/** 探针类，用于验证质量工具链是否生效. */`。
- 其余正文（`@param` / `@return` 等描述、块注释、行注释）不受此限制，正常使用中文句号 `。`。
- Kotlin 的 KDoc **不触发**该规则，可继续使用中文句号。
- 质量门禁以 `maxWarnings = 0` 严格阻断，违反此约定会直接导致 `qualityCheck` 失败。

## 5. 处理已存在的英文注释

- 修改一处代码时，如发现该处或紧邻位置存在英文注释，可顺手翻译为中文。
- 禁止为了"统一注释语言"而对无关代码做大规模翻译式改动：遵循精准修改原则，只改与当前任务直接相关的代码。
