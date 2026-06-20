---
paths:
  - "**/build.gradle.kts"
  - "**/build.gradle"
  - "**/settings.gradle.kts"
---

# 对外 API 语言约束

> 适用于所有发布到 Maven（mavenLocal / mavenCentral / 私服）的"对外 API"模块。
> 仅约束**对外可见**的类型，模块内部实现不在范围内。

## 1. 适用范围

满足以下任一条件的模块视为"对外 API 模块"：

- 配置了 `maven-publish` + `MavenPublication`，且产物预期被三方插件 / 上层模块依赖。
- 模块名以 `*-api` / `*-protocol` / `*-spi` 等结尾，明确表达"对外契约"语义。

本模板中，对外 API 模块即根目录下的 **`api`** 模块（见 `api/build.gradle.kts`）。

非对外 API 模块**不受本规则约束**，可自由使用 Kotlin：

- `project:core`：业务实现模块（Kotlin + KSP + EasyQuery）。
- `plugin`：最终组装模块（仅做打包与 TabooLib 描述）。

## 2. 强制规则

### 2.1 公开 API 必须使用 Java 编写

对外 API 模块的以下类型**必须使用 Java 源码（`src/main/java`）实现**：

- 公开接口（`public interface`）
- 公开注解（`public @interface`）
- 公开枚举（`public enum`）
- 公开抽象类（`public abstract class`）
- 公开记录类（`public record`，如适用）
- 任何对外暴露的工具类入口（`public final class` 含 `public static` 方法）

理由：

- Kotlin 编译产物携带 `@Metadata` 注解、合成类、属性访问器等元数据，会污染纯 Java 调用方的 IDE 提示与反射结果。
- Kotlin 默认参数会编译为 `$default` 桥接方法，三方 Java 反射时极易踩坑。
- Kotlin `data class` / `sealed class` 的 ABI 形态在 Kotlin 版本升级时可能变化，对外 API 必须保持稳定。

### 2.2 不得用 Kotlin 编写对外 API 模块的公开类型

本模板在根 `build.gradle.kts` 的 `subprojects { ... }` 中对**所有**子模块统一应用了 `org.jetbrains.kotlin.jvm` 插件，`api` 模块因此也带 Kotlin 插件。这是模板的既定结构，无需为此单独移除插件。本规则落到 `api` 模块的具体要求是：

- **`src/main/` 下不得创建任何 `public` 可见性的 Kotlin 类型**，所有公开类型一律写在 `src/main/java/`。
- `src/main/kotlin/` 即便存在，也只能放 `internal` / 包私有的辅助类型（注意 Kotlin `internal` 在字节码层仍是 `public`，仍需检查实际 ABI）。
- 测试代码（`src/test/kotlin/`）允许使用 Kotlin，不在本规则约束范围内。

> 进阶定制（模板默认不做）：如需对 `api` 模块彻底隔离 Kotlin，可在根 `subprojects {}` 中按模块名跳过 Kotlin 插件的应用，再单独配置 `api`。这属于结构性改造，改前请评估对打包（`plugin` 模块 `from(project(":api").sourceSets["main"].output)`）的影响。

### 2.3 公开类型升级必须迁移源码

- 已有内部 Kotlin 类型如需开放给三方扩展，**必须先用 Java 重写**，再迁入对外 API 模块。
- 禁止用 Kotlin 写完后通过 `@JvmStatic` / `@JvmOverloads` / `@JvmField` 等注解硬塞进对外 API。
- 迁移过程中保持包路径、类名、方法签名、字段名一致，避免破坏现有调用方。

### 2.4 不得暴露内部实现类型

新增或修改对外 API 模块前，必须人工核查其公开 ABI：

- 公开方法签名、字段类型、注解参数中**不得引用**未发布的内部模块类型（如 `project:core` 中的类）。
- 公开方法签名、字段类型中**不得引用** Kotlin 标准库中容易误用的类型（如 `kotlin.Function0` / `kotlin.Unit` / `kotlin.coroutines.Continuation`）。允许的 Kotlin 类型仅限 `kotlin.collections.*` 等已被 Kotlin Java 互操作明确支持的稳定类型，且**强烈建议**用 `java.util.function.*` / `java.util.*` 替代。
- 三方依赖本 API 模块时，不应被传递依赖到任何内部实现模块。

## 3. 例外

以下情况**不受本规则约束**：

- 模块的 `src/test/` 目录：测试代码与发布产物隔离，可任意使用 Kotlin。
- 模块内部的 `internal` / 包私有 / 非 `public` Kotlin 类型：不进入公开 ABI（注意 Kotlin `internal` 在字节码层仍是 `public`，仍需检查实际 ABI 形态）。
- 仅供本仓库内部使用、不对外发布的临时模块：建议仍遵循本规则，以便后续转为正式发布。

## 4. 检查清单

新增或修改对外 API 模块时自查：

- [ ] `api/src/main/kotlin/` 不存在，或不含任何 `public` / `open` 可见性的类型
- [ ] `api/src/main/java/` 下所有公开类型未引用 Kotlin 标准库中不稳定的类型
- [ ] 公开方法 / 字段 / 注解参数中没有出现 `project:core` 等内部模块类型
- [ ] 已通过 `./gradlew :api:publishToMavenLocal` 校验产物结构正常
- [ ] 三方插件按 `compileOnly("<group>:<artifactId>:<版本>")` 引入后，IDE 提示中不会出现 Kotlin 元数据相关警告

## 5. 当前对外 API 模块（参考）

本模板中唯一的对外 API 模块是 `api`，其发布坐标由 `api/build.gradle.kts` 决定：

```text
groupId    = project.group                              # 即 gradle.properties 的 group
artifactId = "${rootProject.name}-api".lowercase()      # 默认 templateplugin-api（取决于 rootProject.name）
version    = project.version                             # 即 gradle.properties 的 version
```

- `artifactId` 取决于 `rootProject.name`，`settings.gradle.kts` 已固定为 `TemplatePlugin`。重命名项目时改这一处即可，`artifactId` 不会随仓库目录名漂移。
- 发布目标：`maven.wcpe.top` 的 releases / snapshots 仓库（按版本号是否以 `SNAPSHOT` 结尾自动切换）+ `mavenLocal()`。
- 新增其它发布模块时，应复用 `api/build.gradle.kts` 的 `publishing { ... }` 写法，而不是新建一套绕过本规则的配置。

## 6. 与现有规则的关系

- 本规则不约束模块**内部**实现：`project:core` 等内部模块可继续使用 Kotlin 编写主体。
- 本规则不约束代码侧的命名风格：Java / Kotlin 各按语言约定（Java lowerCamelCase 等）。
- 本规则与 `comments.md` / `git-commit.md` 等规则正交，互不覆盖。
