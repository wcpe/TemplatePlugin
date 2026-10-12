package top.wcpe.mc.plugin.template.core.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * 欢迎语服务的单元测试示例.
 *
 * 测试规范见 .claude/rules/testing.md：方法名用英文 lowerCamelCase，
 * 每个测试必须配中文 `@DisplayName` 说明被测行为与期望结果。
 */
class GreetingServiceImplTest {
    private val service = GreetingServiceImpl()

    @Test
    @DisplayName("welcome 应把传入的玩家名填进欢迎语")
    fun welcomeFillsPlayerName() {
        assertEquals("欢迎 Steve 来到本服务器！", service.welcome("Steve"))
    }

    @Test
    @DisplayName("welcome 传入空名字时不应抛异常")
    fun welcomeAcceptsEmptyName() {
        assertTrue(service.welcome("").isNotEmpty())
    }
}
