plugins {
    alias(libs.plugins.convention.android.feature)
}

dependencies {
    implementation(project(":feature:settings:domain"))
    implementation(project(":feature:settings:navigation"))

    testImplementation(libs.kotlinx.coroutines.test)
}
