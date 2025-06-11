// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        all {
            dependencies {
                api(project(":api"))
                implementation(libs.kotlin.logging)
            }
        }
        
        commonMain {
            dependencies {
                implementation(libs.korlibs.korio)
                implementation(libs.yamlkt)
            }
        }
    
        jvmMain {
            dependencies {
                implementation(libs.ztexec)
                implementation(libs.korlibs.korio)
            }
        }
    
        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
                runtimeOnly(libs.slf4j.simple)
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

