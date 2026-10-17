plugins {
    alias(libs.plugins.convention.android.feature)
}

dependencies {
    implementation(project(":feature:library:domain"))
    implementation(project(":feature:library:navigation"))
    implementation(project(":feature:quote:navigation"))
    implementation(project(":feature:settings:navigation"))
    implementation(project(":feature:stats:navigation"))
    implementation(libs.androidx.activity.compose)

    testImplementation(testFixtures(project(":feature:library:domain")))
    testImplementation(libs.kotlinx.coroutines.test)
}
