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
                implementation(project(":core"))
                implementation(libs.kotlinx.serialization)
            }
        }

        jvmMain {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(kotlin("stdlib"))
                implementation(libs.slf4j.api)
                implementation(libs.mvel)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kaml)
            }
        }
    }
}
