package top.wcpe.mc.plugin.template.core.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommandExec
import taboolib.module.lang.sendLang

/**
 * 模板示例命令，注册后可用 `/template greet <玩家名>` 打招呼.
 *
 * `@CommandHeader` 声明命令头，`@CommandBody` 标注执行体字段，
 * 泛型参数是执行体里 `sender` 的类型，`args` 是玩家输入的参数字符串。
 */
@CommandHeader("template", description = "模板示例命令")
object TemplateCommand {
    /**
     * 打招呼子命令，回复文案取自 lang/zh_CN.yml.
     *
     * sendLang 的参数个数决定读取哪个节点：这里传两个参数，
     * 因此实际读取 greet-command-reply-2，%1% 与 %2% 按顺序替换。
     */
    @CommandBody(description = "向指定玩家打招呼")
    val greet =
        subCommandExec<ProxyCommandSender> {
            sender.sendLang("greet-command-reply", sender.name, args)
        }
}
