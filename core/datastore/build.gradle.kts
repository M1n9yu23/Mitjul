plugins {
    alias(libs.plugins.convention.android.library)
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.koin.android)
}
