plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    implementation(libs.spring.security.crypto)
    implementation(libs.java.jwt)
    implementation(libs.koin.core)
    runtimeOnly(libs.spring.core)
    runtimeOnly(libs.bouncycastle.provider)
    runtimeOnly(libs.jcl.over.slf4j)
}
