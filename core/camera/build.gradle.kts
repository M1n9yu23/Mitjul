plugins {
    alias(libs.plugins.convention.android.library.compose)
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.bundles.camerax)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kermit)
}
