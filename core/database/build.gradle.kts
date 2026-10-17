plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
    implementation(libs.koin.android)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.room3.testing)
    androidTestImplementation(libs.assertk)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
