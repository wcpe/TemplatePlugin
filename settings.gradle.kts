
pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.wcpe.top/repository/maven-public/")
        // KSP 插件标记（com.google.devtools.ksp）托管在 Google Maven
        maven("https://dl.google.com/dl/android/maven2/")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("top.wcpe.taboolib.ioc") version "0.0.6"
    }
}

plugins {
    // 允许 Gradle 按需自动下载 Java 8 toolchain（构建 JVM 为 JDK 17+ 时保证编译目标仍是 Java 8）
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 根项目名：决定最终插件 JAR 名与 api 模块 artifactId；克隆模板后改成你的插件名
rootProject.name = "TemplatePlugin"

include("api")
include("project:core")
include("plugin")