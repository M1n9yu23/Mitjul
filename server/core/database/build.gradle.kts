plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    implementation(libs.bundles.exposed)
    implementation(libs.kotlinx.coroutines.core)
    runtimeOnly(libs.h2)
}
