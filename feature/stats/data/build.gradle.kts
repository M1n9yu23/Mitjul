plugins {
    alias(libs.plugins.convention.android.library)
}

dependencies {
    implementation(project(":feature:stats:domain"))
    implementation(project(":core:database"))
    implementation(libs.koin.core)
}
