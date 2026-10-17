pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Mitjul"
include(":app")
include(":core:camera")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:logging")
include(":core:ml")
include(":core:navigation")
include(":core:network")
include(":core:presentation")
include(":core:session")
include(":feature:auth:data")
include(":feature:auth:domain")
include(":feature:auth:navigation")
include(":feature:auth:presentation")
include(":feature:library:data")
include(":feature:library:domain")
include(":feature:library:navigation")
include(":feature:library:presentation")
include(":feature:quote:data")
include(":feature:quote:domain")
include(":feature:quote:navigation")
include(":feature:quote:presentation")
include(":feature:settings:data")
include(":feature:settings:domain")
include(":feature:settings:navigation")
include(":feature:settings:presentation")
include(":feature:stats:data")
include(":feature:stats:domain")
include(":feature:stats:navigation")
include(":feature:stats:presentation")
include(":server:app")
include(":server:core:database")
include(":server:core:domain")
include(":server:core:security")
include(":server:feature:auth")
include(":server:feature:book")
include(":server:feature:media")
include(":server:feature:quote")
