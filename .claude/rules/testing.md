---
paths:
  - "**/src/test/**/*.kt"
  - "**/src/test/**/*.java"
  - "**/src/*Test/**/*.kt"
  - "**/src/*Test/**/*.java"
---

# 测试规范（Test Conventions）

> 仅作用于测试源集（`src/test/`、`src/*Test/`）。基于 JUnit 5。

## 1. 命名规范（强制）

### 1.1 方法名必须使用英文

- 测试方法名使用 **英文 lowerCamelCase**，语义清晰、可 grep。
- 禁止使用中文、拼音、数字编号（`test1` / `test_场景_1`）作为方法名。
- 禁止使用 Kotlin 反引号包裹的中文方法名（`` fun `中文方法名`() ``）。

### 1.2 必须使用 `@DisplayName` 描述中文语义

- 每个 `@Test` / `@ParameterizedTest` 方法必须配一个 `@DisplayName`，内容为 **简体中文**，描述"被测行为 + 期望结果"。
- 禁止用英文 `@DisplayName`，禁止省略 `@DisplayName` 只留方法名。

### 1.3 示例

✅ 正确：

```kotlin
@Test
@DisplayName("List<String> 字段类型的 displayName 应为 List<String>")
fun fieldTypeListOfStringDisplayName() {
    val t = FieldType.LIST(FieldType.STRING)
    assertEquals("List<String>", t.displayName)
}
```

❌ 错误：方法名用中文

```kotlin
@Test
fun `List String 字段类型的 displayName 应为 List String`() { ... }
```

❌ 错误：缺少 `@DisplayName`

```kotlin
@Test
fun fieldTypeListOfStringDisplayName() { ... }
```

❌ 错误：`@DisplayName` 使用英文

```kotlin
@Test
@DisplayName("FieldType.LIST displayName should be List<String>")
fun fieldTypeListOfStringDisplayName() { ... }
```

## 2. 组织规范

### 2.1 `@Nested` 分组的 `@DisplayName` 同样使用中文

```kotlin
@Nested
@DisplayName("枚举字段解码的边界情况")
inner class EnumDecodeEdge { ... }
```

### 2.2 参数化测试

- `@ParameterizedTest` 使用 `name = "[{index}] ..."` 参数模板时，模板文案使用中文。
- 方法名保持英文 lowerCamelCase。

```kotlin
@ParameterizedTest(name = "[{index}] 输入 {0} 应编码为 {1}")
@MethodSource("varintCases")
@DisplayName("VarInt 编码覆盖典型边界值")
fun varintEncodesBoundaryValues(input: Int, expected: ByteArray) { ... }
```

## 3. 断言与注释

- 断言失败消息（`assertEquals(expected, actual, "...")` 第三参数）使用中文。
- 测试内部注释沿用仓库注释规范：仅中文注释，禁止英文注释（参见 `.claude/rules/comments.md`）。

## 4. 文件与类命名

- 测试类命名沿用被测类 + `Test` 后缀，保持英文 UpperCamelCase（例 `PacketBuilderTest`）。
- 测试类本身**不强制** `@DisplayName`，但如需补充上下文，使用中文 `@DisplayName`。

## 5. 检查清单

提交测试代码前自查：

- [ ] 每个 `@Test` / `@ParameterizedTest` 方法名是英文 lowerCamelCase
- [ ] 每个 `@Test` / `@ParameterizedTest` 带中文 `@DisplayName`
- [ ] `@Nested` 分组有中文 `@DisplayName`
- [ ] 断言失败消息、测试内注释均为中文
- [ ] 方法名可直接 grep 定位（不依赖 `@DisplayName` 搜索）
