plugins {
    alias(libs.plugins.convention.android.feature)
}

dependencies {
    implementation(project(":feature:auth:domain"))
    implementation(project(":feature:auth:navigation"))

    testImplementation(testFixtures(project(":feature:auth:domain")))
    testImplementation(libs.kotlinx.coroutines.test)
}
