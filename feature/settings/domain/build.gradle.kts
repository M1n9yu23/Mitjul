plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    api(project(":core:domain"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
}
