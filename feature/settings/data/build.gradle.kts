plugins {
    alias(libs.plugins.convention.android.library)
}

dependencies {
    implementation(project(":feature:settings:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(libs.koin.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.androidx.workmanager)
}
