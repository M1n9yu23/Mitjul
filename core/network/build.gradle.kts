plugins {
    alias(libs.plugins.convention.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core:domain"))
    api(libs.bundles.ktor.client)
    api(libs.kermit)
    api(libs.koin.core)
    implementation(libs.ktor.client.okhttp)

    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}
