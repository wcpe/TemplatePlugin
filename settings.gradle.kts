
pluginManagement {
    repositories {
        mavenLocal()
        // 仅从 wcpe 代理解析 TabooLib 生态插件；其余插件（KSP 等）走 Maven Central /
        // Plugin Portal，避免 wcpe 对非本库构件的 .module 请求超时（524）导致整体解析失败
        maven("https://maven.wcpe.top/repository/maven-public/") {
            content {
                includeGroup("io.izzel.taboolib")
                includeGroup("top.wcpe.taboolib.ioc")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // 允许 Gradle 按需自动下载缺失的编译工具链 JDK（本项目固定用 JDK 17 工具链编译）
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 根项目名：决定最终插件 JAR 名与 api 模块 artifactId；克隆模板后改成你的插件名
rootProject.name = "TemplatePlugin"

include("api")
include("project:core")
include("plugin")