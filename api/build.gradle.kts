// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    // explicitApi()

    sourceSets.commonMain.dependencies {
        api("com.soywiz.korlibs.klock:klock:${Versions.korlibs}")
    }

    sourceSets.jvmMain.dependencies {
        implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
    }

    sourceSets.jvmTests.dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
        implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
    }   
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