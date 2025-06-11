plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "arara"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

include(
    "api",
    "cli",
    "core",
    "kotlin-dsl",
    "lua",
    "mvel",
)
