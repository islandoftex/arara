// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.AraraPublication
import org.islandoftex.arara.build.CTANTreeBuilderTask
import org.islandoftex.arara.build.CTANZipBuilderTask
import org.islandoftex.arara.build.DocumentationSourceZipBuilderTask
import org.islandoftex.arara.build.SourceZipBuilderTask
import org.islandoftex.arara.build.TDSTreeBuilderTask
import org.islandoftex.arara.build.TDSZipBuilderTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false // Apache 2.0
    kotlin("plugin.serialization") version libs.versions.kotlin apply false // Apache 2.0
    alias(libs.plugins.shadow) apply false // Apache 2.0
    alias(libs.plugins.versions) // Apache 2.0
    alias(libs.plugins.spotlessChangelog) // Apache 2.0
    alias(libs.plugins.dokka) apply false // Apache 2.0
    alias(libs.plugins.detekt) // Apache 2.0
    alias(libs.plugins.spotless) // Apache 2.0
}

if (!project.hasProperty("JobToken")) {
    logger.warn(
            """
            Island of TeX ----------------------------------------------
            Will be unable to publish (jobToken missing)
            Ignore this warning if you are not running the publish task
            for the GitLab package registry.
            ------------------------------------------------------------
        """.trimIndent()
    )
}

spotlessChangelog {
    changelogFile("CHANGELOG.md")
    setAppendDashSnapshotUnless_dashPrelease(true)
    ifFoundBumpBreaking("breaking change")
    tagPrefix("v")
    commitMessage("Release v{{version}}")
    remote("origin")
    branch("master")
}

spotless {
    kotlinGradle {
        target(
                "build.gradle.kts",
                "buildSrc/build.gradle.kts",
                "api/build.gradle.kts",
                "core/build.gradle.kts",
                "lua/build.gradle.kts",
                "mvel/build.gradle.kts",
                "kotlin-dsl/build.gradle.kts",
                "cli/build.gradle.kts"
        )
        targetExclude("src/test/**/*.kts")
        // ktlint is a static code analysis tool for Kotlin that enforces
        // code style conventions -- we will disable it for now
        // ktlint()
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin {
        target(
                "api/src/**/*.kt",
                "core/src/**/*.kt",
                "lua/src/**/*.kt",
                "mvel/src/**/*.kt",
                "kotlin-dsl/src/**/*.kt",
                "cli/src/**/*.kt",
                "buildSrc/src/**/*.kt"
        )
        targetExclude("src/test/**/*.kts")
        // ktlint is a static code analysis tool for Kotlin that enforces
        // code style conventions -- we will disable it for now
        // ktlint()
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}

detekt {
    allRules = false
    source.from(files(
            "api/src/commonMain/kotlin",
            "api/src/jvmMain/kotlin",
            "core/src/commonMain/kotlin",
            "core/src/jvmMain/kotlin",
            "lua/src/commonMain/kotlin",
            "mvel/src/commonMain/kotlin",
            "mvel/src/jvmMain/kotlin",
            "kotlin-dsl/src/main/kotlin",
            "cli/src/commonMain/kotlin",
            "cli/src/jvmMain/kotlin",
            "buildSrc/src/main/kotlin"
    ))
    buildUponDefaultConfig = true
    config.from(files("detekt-config.yml"))
}

tasks {

    withType<DependencyUpdatesTask> {
        rejectVersionIf {
            fun isNonStable(version: String) = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                    .any { qualifier ->
                        version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                    }

            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }

        checkForGradleUpdate = false
    }

    register("assembleCTANSourceZip", SourceZipBuilderTask::class.java)

    register("assembleDocumentationSourceZip", DocumentationSourceZipBuilderTask::class.java) {
        dependsOn(":docs:buildDocs")
    }

    register("assembleTDSTree", TDSTreeBuilderTask::class.java) {
        dependsOn(":cli:shadowJar")
        dependsOn(":docs:buildDocs")
        dependsOn("assembleCTANSourceZip")
        dependsOn("assembleDocumentationSourceZip")
    }

    register("assembleTDSZip", TDSZipBuilderTask::class.java) {
        dependsOn("assembleTDSTree")
    }

    register("assembleCTANTree", CTANTreeBuilderTask::class.java) {
        dependsOn("assembleTDSZip")
    }

    register("assembleCTAN", CTANZipBuilderTask::class.java) {
        dependsOn("assembleCTANTree")
    }

}

version = spotlessChangelog.versionNext

// ----------------------------------------------------------------------
// JVM targets and Kotlin API and language versions, with corresponding
// defaults as fallback, defined in gradle.properties (see keys)
// ----------------------------------------------------------------------
val araraJVMString = runCatching { extra["arara.jvm.target"] as String }
        .getOrDefault("11")

val araraJVMTarget = runCatching { JvmTarget.fromTarget(araraJVMString) }
        .getOrDefault(JvmTarget.JVM_11)

val araraJVMInt = araraJVMString.toIntOrNull() ?: 11

val araraKotlinAPI = runCatching { extra["arara.kotlin.api"] as String }
        .getOrDefault("2.1")

val araraKotlinLanguage = runCatching { extra["arara.kotlin.language"] as String }
        .getOrDefault("2.1")
// ----------------------------------------------------------------------

allprojects {
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version
}

// accessing `libs` does not work inside `subprojects` blocks (wont-fix issue),
// workaround from https://github.com/gradle/gradle/issues/18237#issuecomment-928074251
val engine = libs.junit.jupiter.engine

subprojects {

    if (!path.contains("docs")) {

        apply(plugin = "org.jetbrains.dokka")

        val mainManifest: Manifest = DefaultManifest((project as ProjectInternal).fileResolver)
                .apply {
                    attributes["Implementation-Title"] = "arara-${project.name}"
                    attributes["Implementation-Version"] = version

                    if (project.name == "cli") {
                        attributes["Main-Class"] = "org.islandoftex.arara.cli.CLIKt"
                    }

                    // for Java 9+ compatibility of log4j
                    attributes["Multi-Release"] = "true"
                    attributes["Automatic-Module-Name"] = rootProject.group
                }

        apply(plugin = "org.jetbrains.kotlin.multiplatform")

        configure<KotlinMultiplatformExtension> {

            jvm {
                compilerOptions {
                    jvmTarget.set(araraJVMTarget)
                }
            }

            sourceSets {
                all {
                    languageSettings.apply {
                        languageVersion = araraKotlinLanguage
                        apiVersion = araraKotlinAPI
                    }
                }

                val commonMain by getting {
                    dependencies {
                        implementation(kotlin("stdlib-common"))
                    }
                }

                val commonTest by getting {
                    dependencies {
                        implementation(kotlin("test-common"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }

                val jvmTest by getting {
                    dependencies {
                        implementation(kotlin("test-junit5"))
                        runtimeOnly(engine)
                    }
                }
            }
        }

        tasks {

            named<Test>("jvmTest") {
                useJUnitPlatform()

                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    events(
                            TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                            TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED
                    )
                }
            }

            withType<Jar> {
                archiveBaseName.set("arara-${project.name}")
                manifest.attributes.putAll(mainManifest.attributes)
            }

            withType<JavaCompile> {
                sourceCompatibility = araraJVMString
                targetCompatibility = araraJVMString
            }

            named<DokkaTask>("dokkaHtml").configure {
                dokkaSourceSets.configureEach {
                    jdkVersion.set(araraJVMInt)
                    moduleName.set("arara (${project.name})")
                    includeNonPublic.set(false)
                    skipDeprecated.set(false)
                    reportUndocumented.set(true)
                    skipEmptyPackages.set(true)
                    platform.set(org.jetbrains.dokka.Platform.common)
                    sourceLink {
                        localDirectory.set(file("./"))
                        remoteUrl.set(uri("https://gitlab.com/islandoftex/arara").toURL())
                        remoteLineSuffix.set("#L")
                    }
                    noStdlibLink.set(false)
                    noJdkLink.set(false)
                }
            }
        }

        apply<AraraPublication>()
    }
}
