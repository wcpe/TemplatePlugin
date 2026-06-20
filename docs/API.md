# 对外 API 说明（API）

> 本文档说明 `api` 模块的定位、编写约定与三方接入方式。
> `api` 是本项目唯一发布到 Maven 的模块（见 [ARCHITECTURE.md](../ARCHITECTURE.md) §5），承载**对外契约**：接口、注解、枚举、数据传输对象等。
> 当前模板的 `api` 模块为空骨架，下文示例中的类型名均为占位演示，落地时替换为真实 API。

## 1. 定位

- `api` 只放**对外契约**，不含业务实现。三方插件、上层模块面向 `api` 编程。
- 业务实现在 `project:core`，运行期由 `plugin` 模块统一打包提供。
- 依赖方向单向：实现依赖契约，契约**不得**反向依赖实现。

## 2. 发布坐标

由 `api/build.gradle.kts` 推导（详见 [ARCHITECTURE.md](../ARCHITECTURE.md) §5）：

| 项 | 当前值 |
| --- | --- |
| `groupId` | `top.wcpe.mc.plugin.template` |
| `artifactId` | `templateplugin-api`（= `"${rootProject.name}-api".lowercase()`） |
| `version` | `0.0.1`（见 `gradle.properties`） |

> 重命名项目后，`groupId` / `artifactId` / 版本都会变化，记得同步本文档。

## 3. 三方接入

在使用方的构建脚本中加入仓库与依赖。`api` 的字节码已被主插件打包进最终 JAR，因此第三方**插件**用 `compileOnly` 即可（运行期由主插件提供）。

### Gradle（Kotlin DSL）

```kotlin
repositories {
    maven("https://maven.wcpe.top/repository/maven-public/")
}

dependencies {
    // 第三方插件：仅编译期依赖，运行期由本插件提供 api 类
    compileOnly("top.wcpe.mc.plugin.template:templateplugin-api:0.0.1")
}
```

### Maven

```xml
<repositories>
  <repository>
    <id>wcpe</id>
    <url>https://maven.wcpe.top/repository/maven-public/</url>
  </repository>
</repositories>

<dependency>
  <groupId>top.wcpe.mc.plugin.template</groupId>
  <artifactId>templateplugin-api</artifactId>
  <version>0.0.1</version>
  <scope>provided</scope>
</dependency>
```

## 4. 编写约定（强制）

完整规则见 `.claude/rules/public-api-language.md`，要点：

- **公开类型必须用 Java 编写**，放在 `api/src/main/java/`。包括 `public interface` / `@interface` / `enum` / `abstract class` / 工具类入口等。
  - 原因：Kotlin 编译产物的 `@Metadata`、`$default` 桥接方法、`data class`/`sealed class` 的 ABI 波动会污染纯 Java 调用方并破坏稳定性。
- **`api/src/main/kotlin/` 下不得出现 `public`/`open` 的 Kotlin 类型**（模板对所有子模块统一应用了 Kotlin 插件，但公开类型一律走 Java）。
- 公开签名**不得引用**内部实现模块（`project:core`）的类型。
- 公开签名**避免**暴露易误用的 Kotlin 标准库类型（`kotlin.Unit` / `kotlin.Function0` / `kotlin.coroutines.Continuation` 等），优先用 `java.util.*` / `java.util.function.*`。
- 注释使用简体中文（见 `.claude/rules/comments.md`）。

### 示例

✅ 推荐（Java 接口，干净 ABI）：

```java
// api/src/main/java/top/wcpe/mc/plugin/template/api/PlayerDataService.java
package top.wcpe.mc.plugin.template.api;

import java.util.Optional;
import java.util.UUID;

/** 玩家数据服务对外契约。 */
public interface PlayerDataService {
    /** 按玩家 UUID 查询数据，不存在时返回空。 */
    Optional<PlayerData> find(UUID uuid);
}
```

❌ 反例（Kotlin 公开类型进入对外 API）：

```kotlin
// 禁止：data class 的 ABI 形态会随 Kotlin 版本变化
data class PlayerData(val uuid: UUID, val coins: Long)
```

## 5. 新增一个对外 API 的步骤

1. 在 `api/src/main/java/<包路径>/` 下用 Java 定义公开类型。
2. 在 `project:core` 中用 Kotlin 实现，并通过 IoC（`@Service` 等）注册。
3. 本地验证产物：`./gradlew :api:publishToMavenLocal`，确认 `~/.m2/repository` 下结构正常、无 Kotlin 元数据污染。
4. 若属用户可见 / 对外可见能力，按发版技能更新文档与版本。

## 6. 版本与兼容性

- 遵循 SemVer：破坏对外 API（签名 / 行为不兼容）→ 主版本号 +1；新增向后兼容能力 → 次版本号 +1；修复 → 修订号 +1。
- 破坏性变更必须在 [CHANGELOG.md](../CHANGELOG.md) 标注，并给出迁移说明。
- 发版流程（版本推导、构建验证、打 tag）见 `.claude/skills/release-version/SKILL.md`。
