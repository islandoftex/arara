// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    // explicitApi()

    sourceSets {
        commonMain {
            dependencies {
                api("com.soywiz.korlibs.klock:klock:2.7.0")
            }
        }

        jvmMain {
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio:2.7.0")
            }
        }    
    }
    
//    sourceSets.jvmTests.dependencies {
//        implementation("io.kotest:kotest-runner-junit5-jvm:4.6.3")
//        implementation("io.kotest:kotest-assertions-core-jvm:4.6.3")
//    }   
}

// tasks {
//     create("createAraraAPIObject") {
//         listOf(
//             "src/nativeCommonMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt",
//             "src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt"
//         ).forEach {
//             file(it).writeText(
//                 """
//                     // SPDX-License-Identifier: BSD-3-Clause
//                     package org.islandoftex.arara.api

//                     public actual object AraraAPI {
//                         @Suppress("MayBeConst")
//                         public actual val version: String = "${project.version}"
//                     }

//                 """.trimIndent()
//             )
//         }
//     }
// }
