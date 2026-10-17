plugins {
    alias(libs.plugins.convention.android.library)
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.mlkit.text.recognition.korean)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.koin.core)
    implementation(libs.kermit)
}
