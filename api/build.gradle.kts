// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.korlibs.klock)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.korlibs.korio)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks {
    register("createAraraAPIObject") {
        listOf(
                "src/nativeCommonMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt",
                "src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt"
        ).forEach {
            file(it).writeText(
                    """
                        // SPDX-License-Identifier: BSD-3-Clause
                        package org.islandoftex.arara.api
    
                        public actual object AraraAPI {
                            @Suppress("MayBeConst")
                            public actual val version: String = "${project.version}"
                        }
    
                    """.trimIndent()
            )
        }
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
