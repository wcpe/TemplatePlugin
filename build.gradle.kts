import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    java
    id("io.izzel.taboolib") version "2.0.38-38f563b" apply false
    id("org.jetbrains.kotlin.jvm") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("top.wcpe.taboolib.ioc") version "0.0.6" apply false
    // 代码质量插件：仅声明到 classpath，由 subprojects 按需 apply（详见 docs/CODE_QUALITY.md）
    id("com.github.spotbugs") version "6.5.8" apply false
    id("net.ltgt.errorprone") version "4.1.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
}

// Error Prone 需在 JDK 17 上运行 javac（编译产物仍用 --release 8 兼容 Java 8）。
// 默认 toolchain 维持 Java 8 以保证 JDK 8 的 ci.yml 不受影响；
// 仅当显式传入 -PenableErrorProne（如 quality.yml，运行在 JDK 17）时才切到 JDK 17 并启用 Error Prone。
val enableErrorProne = providers.gradleProperty("enableErrorProne").isPresent

// 质量任务中用于聚合的任务名（无对应源码时这些任务不会被创建，matching 自动跳过）
val qualityTaskNames = setOf(
    "pmdMain", "pmdTest",
    "checkstyleMain", "checkstyleTest",
    "spotbugsMain", "spotbugsTest",
    "detekt", "ktlintCheck",
    "jacocoTestReport", "koverXmlReport", "koverHtmlReport"
)

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
        version { taboolib = "6.3.0-75b18a2" }
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

    // ==================== 代码质量工具 ====================
    // 这些工具运行在 Gradle 守护进程的 JVM（需 JDK 11+，推荐 17）上分析代码，
    // 与编译目标（Java 8 字节码）无关。统一在此应用，并通过下方逻辑与 check 解耦。
    val hasJavaMain = file("src/main/java").exists()
    val hasKotlinMain = file("src/main/kotlin").exists()

    // Kotlin 质量：detekt（缺陷/异味/复杂度）、ktlint（样式/格式化）—— 仅扫描 .kt，无源码时空过
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Java 质量：PMD（源码规则）、Checkstyle（样式）—— 仅扫描 src/*/java，无源码时空过
    apply(plugin = "pmd")
    apply(plugin = "checkstyle")

    // 测试覆盖率：Kotlin 模块用 Kover，Java 模块用 JaCoCo（二者都挂测试 JVM 代理，互斥应用避免冲突）
    if (hasKotlinMain) {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    } else {
        apply(plugin = "jacoco")
    }

    // SpotBugs(+FindSecBugs)：分析字节码，仅对含 Java 主源码的模块启用，避免误报 Kotlin 字节码
    if (hasJavaMain) {
        apply(plugin = "com.github.spotbugs")
        configure<SpotBugsExtension> {
            toolVersion.set("4.8.6")
            ignoreFailures.set(false) // 严格阻断
        }
        dependencies {
            // 安全审查：FindSecBugs 作为 SpotBugs 插件挂载
            "spotbugsPlugins"("com.h3xstream.findsecbugs:findsecbugs-plugin:1.13.0")
        }
        tasks.withType<SpotBugsTask>().configureEach {
            onlyIf { JavaVersion.current().isJava11Compatible } // JDK 8 上自动跳过
            reports.maybeCreate("html").required.set(true)
        }
    }

    // Error Prone：编译期缺陷检测，需 JDK 17 javac，仅在开启且含 Java 源码时应用
    if (enableErrorProne && hasJavaMain) {
        apply(plugin = "net.ltgt.errorprone")
        dependencies {
            "errorprone"("com.google.errorprone:error_prone_core:2.36.0")
        }
    }

    // detekt 配置：使用官方默认规则集，严格阻断
    configure<DetektExtension> {
        parallel = true
        ignoreFailures = false
    }
    tasks.withType<Detekt>().configureEach {
        onlyIf { JavaVersion.current().isJava11Compatible }
        // 排除构建产物与 KSP 生成代码，避免对生成代码误报
        exclude("**/build/**", "**/generated/**")
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }

    // ktlint 配置：官方默认规则集，严格阻断，排除构建产物
    configure<KtlintExtension> {
        ignoreFailures.set(false)
        filter {
            exclude { element -> element.file.path.replace('\\', '/').contains("/build/") }
        }
    }
    tasks.matching { it.name.startsWith("ktlint") }.configureEach {
        onlyIf { JavaVersion.current().isJava11Compatible }
    }
    // 不对 Gradle 构建脚本（*.kts）做 ktlint 检查：构建脚本沿用 TabooLib DSL 习惯（含通配符 import），
    // 仅对源码（src/**/*.kt）执行样式门禁
    tasks.matching { it.name.contains("KotlinScript") }.configureEach { enabled = false }

    // PMD 配置：官方 quickstart 规则集，严格阻断（ignoreFailures 默认 false）
    configure<PmdExtension> {
        toolVersion = "7.7.0"
        isConsoleOutput = true
        ruleSets = listOf("rulesets/java/quickstart.xml")
    }
    tasks.withType<Pmd>().configureEach {
        onlyIf { JavaVersion.current().isJava11Compatible }
    }

    // Checkstyle 配置：内置 Google 官方规则（config/checkstyle/checkstyle.xml），严格阻断
    configure<CheckstyleExtension> {
        toolVersion = "10.21.0"
        maxWarnings = 0 // 任何告警都视为失败
    }
    tasks.withType<Checkstyle>().configureEach {
        onlyIf { JavaVersion.current().isJava11Compatible }
    }

    // 质量任务与 check/build 解耦：保证 JDK 8 的 ci.yml 仅做纯编译+测试，不触发质量门禁。
    // 质量门禁统一由根项目的 qualityCheck 聚合任务驱动（见文件末尾），在 JDK 17 的 quality.yml 中运行。
    afterEvaluate {
        val isQualityName = { n: String ->
            n.startsWith("pmd") || n.startsWith("checkstyle") || n.startsWith("spotbugs") ||
                n.startsWith("detekt") || n.startsWith("ktlint")
        }
        // 递归解析依赖项中的任务名：依赖可能是 Task / TaskProvider / String / 集合 /
        // Callable / Provider（PMD、Checkstyle 即以 Callable 形式挂入 check）。
        fun depNames(dep: Any?): List<String> = when (dep) {
            is Task -> listOf(dep.name)
            is TaskProvider<*> -> listOf(dep.name)
            is String -> listOf(dep)
            is Iterable<*> -> dep.flatMap { depNames(it) }
            is java.util.concurrent.Callable<*> -> depNames(dep.call())
            is Provider<*> -> depNames(dep.orNull)
            else -> emptyList()
        }
        tasks.findByName("check")?.let { check ->
            check.setDependsOn(
                check.dependsOn.filterNot { dep ->
                    val names = depNames(dep)
                    names.isNotEmpty() && names.all { isQualityName(it) }
                }
            )
        }
    }

    // Java 编码与目标：启用 Error Prone 时用 JDK 17 + --release 8，否则维持 Java 8 toolchain
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (enableErrorProne) {
            options.release.set(8)
        }
    }

    // 默认路径固定 Java 8 toolchain，保证 JDK 8 的 ci.yml 行为不变；
    // Error Prone 路径不固定 toolchain，直接用运行 Gradle 的 JVM（CI 为 JDK 17、本机需 JDK 17+），
    // 仅靠 --release 8 约束目标级别，避免对某个具体小版本 JDK 的硬依赖。
    if (!enableErrorProne) {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

}

// 聚合任务：运行全部代码质量门禁与覆盖率报告。需在 JDK 17（或更高）上执行，
// 启用 Error Prone 时追加 -PenableErrorProne。CI 见 .github/workflows/quality.yml。
tasks.register("qualityCheck") {
    group = "verification"
    description = "运行全部代码质量门禁（PMD/Checkstyle/SpotBugs/detekt/ktlint）与覆盖率报告，需 JDK 17"
}

gradle.projectsEvaluated {
    tasks.named("qualityCheck") {
        dependsOn(
            subprojects.flatMap { sp ->
                sp.tasks.matching { it.name in qualityTaskNames }
            }
        )
    }
}
