plugins {
    alias(libs.plugins.convention.jvm.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass = "com.gyugle.mitjul.server.app.ApplicationKt"
    applicationDefaultJvmArgs = listOf("-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
}

dependencies {
    implementation(project(":server:core:database"))
    implementation(project(":server:core:domain"))
    implementation(project(":server:core:security"))
    implementation(project(":server:feature:auth"))
    implementation(project(":server:feature:book"))
    implementation(project(":server:feature:media"))
    implementation(project(":server:feature:quote"))
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.koin.server)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
}
