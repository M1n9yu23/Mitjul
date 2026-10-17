/*
 * Copyright 2026 MinGyu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.api.dsl.ApplicationExtension
import com.gyugle.mitjul.convention.ROOT_PACKAGE
import com.gyugle.mitjul.convention.catalogVersion
import com.gyugle.mitjul.convention.catalogVersionInt
import com.gyugle.mitjul.convention.configString
import com.gyugle.mitjul.convention.configureUnitTests
import com.gyugle.mitjul.convention.keystoreValue
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                namespace = ROOT_PACKAGE
                compileSdk {
                    version = release(catalogVersionInt("projectCompileSdkVersion"))
                }
                defaultConfig {
                    applicationId = catalogVersion("projectApplicationId")
                    minSdk = catalogVersionInt("projectMinSdkVersion")
                    targetSdk = catalogVersionInt("projectTargetSdkVersion")
                    versionCode = catalogVersionInt("projectVersionCode")
                    versionName = catalogVersion("projectVersionName")
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                signingConfigs.create("release").apply {
                    keystoreValue("KEYSTORE_PATH")?.let { path ->
                        storeFile = rootProject.file(path)
                        storePassword = keystoreValue("KEYSTORE_PASSWORD")
                        keyAlias = keystoreValue("KEY_ALIAS")
                        keyPassword = keystoreValue("KEY_PASSWORD")
                    }
                }
                buildTypes {
                    debug {
                        applicationIdSuffix = ".debug"
                        versionNameSuffix = "-debug"
                    }
                    release {
                        signingConfig = signingConfigs.getByName("release")
                            .takeIf { it.storeFile != null }
                        optimization {
                            enable = true
                        }
                    }
                }
                flavorDimensions += "environment"
                productFlavors {
                    create("staging") {
                        dimension = "environment"
                        applicationIdSuffix = ".staging"
                        buildConfigField("String", "BASE_URL", configString("BASE_URL_STAGING"))
                    }
                    create("production") {
                        dimension = "environment"
                        buildConfigField("String", "BASE_URL", configString("BASE_URL_PRODUCTION"))
                    }
                }
                compileOptions {
                    val java = JavaVersion.toVersion(catalogVersion("projectJavaVersion"))
                    sourceCompatibility = java
                    targetCompatibility = java
                }
                buildFeatures {
                    buildConfig = true
                }
            }
            configureUnitTests()
        }
    }
}
