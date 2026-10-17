plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":feature:quote:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(libs.koin.core)
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.androidx.workmanager)

    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}
