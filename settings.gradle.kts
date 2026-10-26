// automatic toolchain resolution
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "arara"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Shadow's optional R8 configuration is also resolved by dependency scanning.
        google {
            content { includeModule("com.android.tools", "r8") }
        }
    }
}

include(
    "api",
    "core",
    "lua",
    "mvel",
    "cli",
    "docs",
    "smoke-tests"
)

// CI passes documentation cache entries between jobs as artifacts.
buildCache {
    local { directory = file(".gradle/build-cache") }
}
