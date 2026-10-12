plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(project(":api"))
    // TabooLib IoC 注解（@Service / @Inject 等）：插件只做接管与 relocate，不自动进编译期，
    // 用到注解的模块需自行声明。版本与插件接管的 IoC 运行时保持一致
    compileOnly("top.wcpe.taboolib.ioc:taboolib-ioc-annotation:1.1.0")
    // EasyQuery KSP 处理器 (生成 EntityProxy 类)
    compileOnly("com.easy-query:sql-api-proxy:3.2.16")
    ksp("com.easy-query:sql-ksp-processor:3.2.16")

    testImplementation(project(":api"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
