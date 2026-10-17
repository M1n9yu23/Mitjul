plugins {
    alias(libs.plugins.convention.android.library.compose)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
}
