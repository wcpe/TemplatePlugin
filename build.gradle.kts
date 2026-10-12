import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.spotbugs.snom.SpotBugsExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    java
    id("io.izzel.taboolib") version "2.0.39-wcpe.1" apply false
    id("org.jetbrains.kotlin.jvm") version "2.4.21" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
    id("top.wcpe.taboolib.ioc") version "0.0.11" apply false
    // 代码质量插件：仅声明到 classpath，由 subprojects 统一 apply
    id("com.github.spotbugs") version "6.5.12" apply false
    id("net.ltgt.errorprone") version "5.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.11" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "top.wcpe.taboolib.ioc")

    configure<TabooLibExtension> {
        subproject = true
        env {
            install(Basic)
            install(Bukkit)
            install(BukkitUtil)
            install(BukkitUI)
            install(CommandHelper)
            install(I18n)
        }
        version { taboolib = "6.3.0-wcpe.1" }
    }

    repositories {
        mavenLocal()
        maven("https://maven.wcpe.top/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly("ink.ptms.core:v12004:12004:mapped")
        compileOnly("ink.ptms.core:v12004:12004:universal")
    }

    // 编译工具链固定 JDK 17，产物用 --release 8 产出 Java 8 字节码，
    // 在 Java 8 的服务器上照样能跑。source/target 必须显式对齐 1.8，
    // 否则 Kotlin 会判定 Java 与 Kotlin 的 JVM 目标不一致。
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    // 测试统一走 JUnit 5：各模块新增测试时不必重复配置
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // ==================== 代码质量 ====================
    // 各插件默认已把自己的任务挂入 check，构建脚本不需要任何任务级接线：
    // detekt / ktlint / PMD / Checkstyle / SpotBugs 与 JUnit 5 测试统一由 check 汇总。
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "pmd")
    apply(plugin = "checkstyle")
    // 覆盖率：Kotlin 模块用 Kover，Java 模块用 JaCoCo（二者都挂测试 JVM 代理，互斥避免冲突）
    apply(plugin = if (file("src/main/kotlin").exists()) "org.jetbrains.kotlinx.kover" else "jacoco")

    configure<DetektExtension> {
        parallel = true
        ignoreFailures = false
    }
    tasks.withType<Detekt>().configureEach {
        // 排除构建产物与 KSP 生成代码，避免对生成代码误报
        exclude("**/build/**", "**/generated/**")
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }

    configure<KtlintExtension> {
        ignoreFailures.set(false)
        // 排除构建产物；构建脚本沿用 TabooLib DSL 习惯（含通配符 import），不做样式门禁
        filter {
            exclude {
                val path = it.file.path.replace('\\', '/')
                path.contains("/build/") || path.endsWith(".gradle.kts")
            }
        }
    }

    configure<PmdExtension> {
        toolVersion = "7.7.0"
        isConsoleOutput = true
        ruleSets = listOf("rulesets/java/quickstart.xml")
    }

    configure<CheckstyleExtension> {
        toolVersion = "10.21.0"
        maxWarnings = 0 // 任何告警都视为失败
    }
    tasks.withType<Checkstyle>().configureEach {
        // checkstyle.xml 里的抑制文件是裸文件名，Checkstyle 按子项目目录解析，
        // 放在根 config/checkstyle/ 下的抑制文件会静默失效，必须给绝对路径
        configProperties?.putAll(
            mapOf(
                "org.checkstyle.google.suppressionfilter.config" to
                    rootProject.file("config/checkstyle/checkstyle-suppressions.xml").absolutePath,
                "org.checkstyle.google.suppressionxpathfilter.config" to
                    rootProject.file("config/checkstyle/checkstyle-xpath-suppressions.xml").absolutePath
            )
        )
    }

    // SpotBugs + FindSecBugs 与 Error Prone 都只面向 Java：无 Java 源码的模块不挂载，
    // 免得分析 Kotlin 字节码 / 空跑 javac 产生无意义开销与误报
    if (file("src/main/java").exists()) {
        apply(plugin = "com.github.spotbugs")
        configure<SpotBugsExtension> {
            toolVersion.set("4.8.6")
            ignoreFailures.set(false)
        }
        dependencies {
            "spotbugsPlugins"("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
        }

        apply(plugin = "net.ltgt.errorprone")
        dependencies {
            "errorprone"("com.google.errorprone:error_prone_core:2.50.0")
        }
    }
}