// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.AraraPublication
import org.islandoftex.arara.build.CTANTreeBuilderTask
import org.islandoftex.arara.build.CTANZipBuilderTask
import org.islandoftex.arara.build.DocumentationSourceZipBuilderTask
import org.islandoftex.arara.build.SourceZipBuilderTask
import org.islandoftex.arara.build.TDSTreeBuilderTask
import org.islandoftex.arara.build.TDSZipBuilderTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.nio.file.Path

plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("multiplatform") version libs.versions.kotlin apply false // Apache 2.0
    kotlin("plugin.serialization") version libs.versions.kotlin apply false // Apache 2.0
    alias(libs.plugins.shadow) apply false // Apache 2.0
    alias(libs.plugins.versions) // Apache 2.0
    alias(libs.plugins.spotlessChangelog) // Apache 2.0
    alias(libs.plugins.dokka) // Apache 2.0
    alias(libs.plugins.detekt) // Apache 2.0
    alias(libs.plugins.spotless) // Apache 2.0
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
                "cli/build.gradle.kts",
                "docs/build.gradle.kts",
                "smoke-tests/build.gradle.kts"
        )
        // ktlint updated its rule set so that a majority of our files newly
        // triggers lints that are not as easy to fix, therefore we disabled
        // it for now
        // -----------------------------------------------------------------
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
                "cli/src/**/*.kt",
                "buildSrc/src/**/*.kt",
                "smoke-tests/src/**/*.kt"
        )
        // ktlint updated its rule set so that a majority of our files newly
        // triggers lints that are not as easy to fix, therefore we disabled
        // it for now
        // -----------------------------------------------------------------
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
            "lua/src/jvmMain/kotlin",
            "mvel/src/commonMain/kotlin",
            "mvel/src/jvmMain/kotlin",
            "cli/src/commonMain/kotlin",
            "cli/src/jvmMain/kotlin",
            "buildSrc/src/main/kotlin",
            "smoke-tests/src/main/kotlin"
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

configure<DokkaExtension> {
    moduleName.set("arara")
}

dependencies {
    listOf("api", "core", "lua", "mvel", "cli").forEach { module ->
        add("dokka", project(":$module"))
    }
}

subprojects {

    if (!path.contains("docs") && name != "smoke-tests") {

        apply(plugin = "org.jetbrains.dokka")

        val mainAttributes = mutableMapOf(
            "Implementation-Title" to "arara-${project.name}",
            "Implementation-Version" to version,
            "Multi-Release" to "true",
            "Automatic-Module-Name" to "${rootProject.group}.${project.name}"
        ).apply {
            if (project.name == "cli") put("Main-Class", "org.islandoftex.arara.cli.CLIKt")
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

        configure<DokkaExtension> {
            moduleName.set("arara (${project.name})")
            dokkaPublications.named("html") {
                suppressInheritedMembers.set(true)
                suppressObviousFunctions.set(true)
            }
            dokkaSourceSets.configureEach {
                jdkVersion.set(araraJVMInt)
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                sourceLink {
                    localDirectory.set(file("./"))
                    remoteUrl.set(uri("https://gitlab.com/islandoftex/arara/-/blob/development/${project.name}"))
                    remoteLineSuffix.set("#L")
                }
            }
        }

        tasks {

            named<Test>("jvmTest") {
                useJUnitPlatform()
                providers.gradleProperty("testJavaVersion").orNull?.let { version ->
                    javaLauncher.set(project.extensions.getByType<JavaToolchainService>().launcherFor {
                        languageVersion.set(JavaLanguageVersion.of(version.toInt()))
                    })
                }

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
                manifest.attributes(mainAttributes)
            }

            withType<JavaCompile> {
                sourceCompatibility = araraJVMString
                targetCompatibility = araraJVMString
            }
        }

        apply<AraraPublication>()
    }
}

// Exercise the packaged application without requiring a TeX installation.
tasks.register<Exec>("processSmoke") {
    group = "verification"
    description = "Run portable process checks against the runnable jar (JDK 11+)."
    dependsOn(":cli:shadowJar", ":smoke-tests:runnerJar")
    val smokeJava = providers.gradleProperty("smokeJava").orElse(
        providers.systemProperty("java.home").map {
            Path.of(it, "bin", if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java")
                .toString()
        }
    )
    val report = layout.buildDirectory.file("test-results/processSmoke/results.xml")
    inputs.files(project(":smoke-tests").tasks.named("runnerJar"))
    inputs.files(project(":cli").tasks.named("shadowJar"))
    inputs.property("smokeJava", smokeJava)
    outputs.file(report)
    outputs.upToDateWhen { false }
    doFirst {
        val jar = project(":cli").tasks.named<Jar>("shadowJar").get().archiveFile.get().asFile
        val runner = project(":smoke-tests").tasks.named<Jar>("runnerJar").get().archiveFile.get().asFile
        commandLine(
            smokeJava.get(), "-jar", runner.absolutePath,
            "--jar", jar.absolutePath, "--java", smokeJava.get(),
            "--report", report.get().asFile.absolutePath
        )
    }
}

// Give runtime-only CI jobs both jars, independent of versioned filenames.
tasks.register<Copy>("stageProcessSmokeJar") {
    group = "verification"
    description = "Stage the application and smoke runner jars for a separate runtime job."
    dependsOn(":cli:shadowJar", ":smoke-tests:runnerJar")
    from(provider { project(":cli").tasks.named<Jar>("shadowJar").get().archiveFile.get().asFile }) {
        rename { "arara.jar" }
    }
    from(provider { project(":smoke-tests").tasks.named<Jar>("runnerJar").get().archiveFile.get().asFile }) {
        rename { "runner.jar" }
    }
    into(layout.buildDirectory.dir("processSmoke"))
}
