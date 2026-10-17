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

import com.android.build.api.dsl.LibraryExtension
import com.gyugle.mitjul.convention.catalogVersion
import com.gyugle.mitjul.convention.catalogVersionInt
import com.gyugle.mitjul.convention.configureUnitTests
import com.gyugle.mitjul.convention.pathToPackageName
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                namespace = pathToPackageName()
                compileSdk {
                    version = release(catalogVersionInt("projectCompileSdkVersion"))
                }
                defaultConfig {
                    minSdk = catalogVersionInt("projectMinSdkVersion")
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    val java = JavaVersion.toVersion(catalogVersion("projectJavaVersion"))
                    sourceCompatibility = java
                    targetCompatibility = java
                }
            }
            configureUnitTests()
        }
    }
}
