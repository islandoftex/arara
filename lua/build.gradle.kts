// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {

        all {
            with (languageSettings) {
                optIn("org.islandoftex.arara.api.localization.AraraMessages")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.RequiresOptIn")
            }
        }

        commonMain {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(libs.korlibs.luak)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test-junit5"))
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
    }
}
