package top.wcpe.mc.plugin.template.api;

/**
 * 欢迎语服务，对外暴露的最小契约示例.
 *
 * <p>公开类型必须用 Java 编写，且只暴露 JDK 自带与基础类型，
 * 避免把内部实现细节写进 ABI，详见 .claude/rules/public-api-language.md。
 */
public interface GreetingService {

  /**
   * 生成一条欢迎语.
   *
   * @param playerName 玩家名
   * @return 欢迎语文本
   */
  String welcome(String playerName);
}
