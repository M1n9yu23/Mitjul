plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":feature:auth:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:session"))
    implementation(libs.koin.core)

    testImplementation(testFixtures(project(":core:session")))
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}
