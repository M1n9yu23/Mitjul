plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    testFixtures {
        enable = true
    }
}

dependencies {
    api(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(libs.koin.core)

    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}
