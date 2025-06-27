// automatic toolchain resolution
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "arara"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

include(
    "api",
    "core",
    "lua",
    "mvel",
    "kotlin-dsl",
    "cli",
    "docs"
)
