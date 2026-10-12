package top.wcpe.mc.plugin.template.core.service

import top.wcpe.mc.plugin.template.api.GreetingService
import top.wcpe.taboolib.ioc.annotation.Service

/**
 * 欢迎语服务的默认实现.
 *
 * 标注 `@Service` 后由 IoC 容器托管，其他组件用 `@Inject` 按名称注入即可，无需手动 new。
 */
@Service("greetingService")
class GreetingServiceImpl : GreetingService {
    override fun welcome(playerName: String): String = "欢迎 $playerName 来到本服务器！"
}
