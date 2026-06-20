
pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.wcpe.top/repository/maven-public/")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("top.wcpe.taboolib.ioc") version "0.0.5"
    }
}

// 根项目名：决定最终插件 JAR 名与 api 模块 artifactId；克隆模板后改成你的插件名
rootProject.name = "TemplatePlugin"

include("api")
include("project:core")
include("plugin")