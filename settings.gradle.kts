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
