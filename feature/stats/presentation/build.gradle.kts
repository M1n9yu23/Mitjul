plugins {
    alias(libs.plugins.convention.android.feature)
}

dependencies {
    implementation(project(":feature:stats:domain"))
    implementation(project(":feature:stats:navigation"))
}
