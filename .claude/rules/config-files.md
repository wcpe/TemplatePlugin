---
paths:
  - "**/src/main/resources/**/*.yml"
  - "**/src/main/resources/**/*.yaml"
  - "**/src/test/resources/**/*.yml"
  - "**/src/test/resources/**/*.yaml"
---

# 配置文件规范

> 适用于本仓库所有面向运行期的 YAML 配置文件（含 `*.yml` / `*.yaml`）。
> 仅约束业务/插件配置；构建期生成的资源（如 `META-INF/*.toml`）不在范围内。

## 1. 文件命名（强制）

- 文件名必须使用 **kebab-case**：全部小写英文字母 + 数字，多词之间用连字符 `-` 拼接。
- 扩展名统一使用 `.yml`，**不混用** `.yaml`（除非上游框架强制要求）。
- 禁止使用 `snake_case`、`camelCase`、`PascalCase`、空格、中文、点号 `.` 作为单词分隔符。

### 1.1 文件名示例

✅ 正确：

```
extra-slots.yml
ui-huds.yml
config.yml
inventory-rules.yml
crafting-recipes.yml
```

❌ 错误：

```
extraSlots.yml          # camelCase
extra_slots.yml         # snake_case
ExtraSlots.yml          # PascalCase
extra slots.yml         # 含空格
extra.slots.yml         # 用点号分隔（应仅作扩展名分隔符）
```

## 2. 字段命名（强制）

- 所有 YAML 字段名必须使用 **kebab-case**：全部小写英文字母 + 数字，多词之间用连字符 `-` 拼接。
- 字段层级嵌套不影响命名规则，每一层都遵守 kebab-case。
- 禁止 `snake_case`、`camelCase`、`PascalCase`、含空格的字段名。

### 2.1 例外

以下情况**不视为字段名**，不受本规则约束：

- 字段值（`value`）部分：业务字符串、枚举、ID 等可保留原本约定（如枚举常量 `ANY_LINE`、Minecraft 物品 ID `minecraft:diamond`）。
- 用户自定义实例 key：当 YAML 字段是"由用户起名的实例标识"，且这些 key 会作为业务 ID 被消费时（例如配方 ID、自定义槽位 ID），允许使用业务领域约定的命名风格，但**仍建议**优先使用 kebab-case；如必须使用其它风格，需在该字段上方注释里说明原因。
- 第三方框架强制字段：如某 Bukkit/Paper/Mod 库要求特定字段名（如 `commands.<name>.permission` 等约定 key），按上游约定即可。

### 2.2 字段名示例

✅ 正确：

```yaml
default-locale: "zh_CN"
watch-interval: 1000
rule-logic: OR
condition-logic: AND
lore-match-mode: ANY_LINE
reject-message: "此格子只能放入指定物品"
```

❌ 错误：

```yaml
defaultLocale: "zh_CN"          # camelCase
default_locale: "zh_CN"         # snake_case
DefaultLocale: "zh_CN"          # PascalCase
"default locale": "zh_CN"       # 含空格
```

## 3. 字段中文注释（强制）

- **每一个字段上方必须有中文行注释**，说明该字段的用途、取值范围、默认值、影响。
- 注释使用大白话，让非技术运维也能看懂；避免"它就是个开关"这种空话。
- 注释行使用 `#` 开头，紧贴字段所在行的上一行；同一作用域多个字段，每个都单独写一段注释。
- 嵌套对象 / 列表的父字段也要有注释；若子字段一目了然且父注释已覆盖，可仅给父字段注释（建议仍逐字段写，便于运维查阅）。

### 3.1 注释内容建议

- 写清楚 **作用**（这字段控制什么）。
- 写清楚 **可选值** / **取值范围**（枚举列出全部，数值给出范围或单位）。
- 写清楚 **默认值**（若有）。
- 写清楚 **影响**（开启 / 关闭 / 修改后会发生什么）。
- 必要时写清楚 **依赖关系**（依赖另一个字段 / 与另一个字段互斥）。

### 3.2 示例

✅ 正确：

```yaml
i18n:
  # 默认语言：玩家未指定客户端语言时使用的语言代码（参照 Minecraft 语言代码，如 zh_CN / en_US）
  default-locale: "zh_CN"
  # 兜底语言：默认语言中找不到对应翻译键时回退使用的语言
  fallback-locale: "en_US"
  dev-mode:
    # 是否启用开发热重载：开启后会监听 lang/ 目录变更并自动重新加载语言文件
    enabled: false
    # 监听轮询间隔（毫秒）：值越小响应越快但 IO 开销越大，建议 500-2000 之间
    watch-interval: 1000
```

❌ 错误：缺少注释

```yaml
i18n:
  default-locale: "zh_CN"
  fallback-locale: "en_US"
  dev-mode:
    enabled: false
    watch-interval: 1000
```

❌ 错误：注释空洞 / 不说人话

```yaml
i18n:
  # locale
  default-locale: "zh_CN"
  # fallback
  fallback-locale: "en_US"
  dev-mode:
    # mode
    enabled: false
    # ms
    watch-interval: 1000
```

❌ 错误：注释写在字段右边而非上方

```yaml
i18n:
  default-locale: "zh_CN"     # 默认语言
  fallback-locale: "en_US"    # 兜底语言
```

> 行尾注释在 YAML 中容易被忽略且不便阅读，本仓库统一要求注释独占一行写在字段上方。

## 4. 文件头部说明（建议）

每个配置文件文件头建议保留 1-3 行整体说明：写清楚这个配置文件是干什么的、由哪个模块加载、如何热重载（若支持）。

```yaml
# 插件主配置
# 加载方：core 模块的配置管理器
# 修改后：执行 /<plugin> reload 热重载，无需重启服务器

settings:
  # 多条 rule 之间的逻辑关系：OR = 满足任一 rule 即可生效；AND = 全部 rule 都满足才生效
  rule-logic: OR
  ...
```

## 5. 检查清单

提交配置文件改动前自查：

- [ ] 文件名是 kebab-case，扩展名是 `.yml`
- [ ] 所有字段名是 kebab-case，没有 `snake_case` / `camelCase`
- [ ] 每个字段上方有独立的中文注释（非右侧行尾）
- [ ] 注释内容大白话，覆盖"作用 / 可选值 / 默认值 / 影响"中的关键点
- [ ] 文件头部有 1-3 行整体说明（可选但建议）

## 6. 与现有规则的关系

- 本规则是注释规范（`comments.md`）在配置文件场景下的细化要求；遇到冲突以本规则为准。
- 本规则不约束代码侧的常量名 / 字段名；代码侧仍按各语言约定（Java/Kotlin lowerCamelCase 等）。
- 本规则不约束业务字符串值（枚举值、Minecraft ID 等）。
