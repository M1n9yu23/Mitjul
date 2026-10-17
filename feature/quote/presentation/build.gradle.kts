plugins {
    alias(libs.plugins.convention.android.feature)
}

dependencies {
    implementation(project(":core:camera"))
    implementation(project(":core:ml"))
    implementation(project(":feature:quote:domain"))
    implementation(project(":feature:quote:navigation"))
    implementation(libs.androidx.activity.compose)

    testImplementation(testFixtures(project(":feature:quote:domain")))
    testImplementation(libs.kotlinx.coroutines.test)
}
