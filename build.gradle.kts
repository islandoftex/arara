// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import java.nio.file.Path
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.tasks.Jar as JvmJar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.islandoftex.arara.build.AraraPackaging
import org.islandoftex.arara.build.AraraPublication
import org.islandoftex.arara.build.VerifyPublicationArtifacts
import org.islandoftex.arara.build.VerifyPublicationConsumer
import org.islandoftex.arara.build.VerifyRuntimeDependencies
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("jvm-toolchains")
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false // Apache 2.0
    alias(libs.plugins.kotlin.serialization) apply false // Apache 2.0
    alias(libs.plugins.shadow) apply false // Apache 2.0
    alias(libs.plugins.versions) // Apache 2.0
    alias(libs.plugins.spotlessChangelog) // Apache 2.0
    alias(libs.plugins.dokka) // Apache 2.0
    alias(libs.plugins.detekt) // Apache 2.0
    alias(libs.plugins.kover) // Apache 2.0
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
                "settings.gradle.kts",
                "buildSrc/build.gradle.kts",
                "api/build.gradle.kts",
                "core/build.gradle.kts",
                "lua/build.gradle.kts",
                "mvel/build.gradle.kts",
                "cli/build.gradle.kts",
                "docs/build.gradle.kts",
                "smoke-tests/build.gradle.kts"
        )
        // Only import ordering is enabled; surrounding Kotlin formatting stays unchanged.
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_standard" to "disabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ij_kotlin_imports_layout" to "*"
            )
        )
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
        // Only import ordering is enabled; surrounding Kotlin formatting stays unchanged.
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_standard" to "disabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ij_kotlin_imports_layout" to "*"
            )
        )
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    format("yamlRules") {
        target("rules/*.yaml")
        licenseHeader(
            """
            !config
            # Arara, the cool TeX automation tool
            # Copyright (c) ${'$'}YEAR, Island of TeX
            # All rights reserved.
            #
            # This rule is part of arara.
            """.trimIndent(),
            "identifier:"
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("luaRules") {
        target("rules/*.lua")
        licenseHeader(
            """
            -- arara, the cool TeX automation tool
            -- Copyright (c) ${'$'}YEAR, Island of TeX
            -- All rights reserved.
            --
            -- This rule is part of arara.
            """.trimIndent(),
            "return \\{"
        )
        trimTrailingWhitespace()
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


}

version = spotlessChangelog.versionNext

// Compiler settings are required properties; invalid values fail during configuration.
val araraJVMString = providers.gradleProperty("arara.jvm.target").get()
val araraJVMTarget = JvmTarget.fromTarget(araraJVMString)
val araraJVMInt = araraJVMString.toInt()
val araraKotlinAPI = KotlinVersion.fromVersion(providers.gradleProperty("arara.kotlin.api").get())
val araraKotlinLanguage = KotlinVersion.fromVersion(providers.gradleProperty("arara.kotlin.language").get())
val warningsAsErrors = providers.gradleProperty("warningsAsErrors").map(String::toBooleanStrict).orElse(false)

tasks.withType<Detekt>().configureEach {
    jvmTarget = araraJVMString
}

allprojects {
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.allWarningsAsErrors.set(warningsAsErrors)
    }
}

configure<DokkaExtension> {
    moduleName.set("arara")
}

dependencies {
    listOf("api", "core", "lua", "mvel", "cli").forEach { module ->
        add("dokka", project(":$module"))
    }
}

val publicationModules = listOf("api", "core", "lua", "mvel", "cli")

// Aggregate tests across module boundaries as well as each module's own suite.
dependencies {
    publicationModules.forEach { add("kover", project(":$it")) }
}
val publicationRepository = layout.buildDirectory.dir("publication-check/repository")
val preparePublicationCheck = tasks.register<Delete>("preparePublicationCheck") {
    delete(publicationRepository)
}

val runPublicationConsumer = tasks.register<VerifyPublicationConsumer>("runPublicationConsumer") {
    group = "verification"
    description = "Build and run a Kotlin consumer of staged Maven publications on Java 21."
    dependsOn(publicationModules.map { ":$it:publishAllPublicationsToPublicationCheckRepository" })
    repositoryDirectory.set(publicationRepository)
    publicationVersion.set(project.version.toString())
    kotlinVersion.set(libs.versions.kotlin)
    fixtureDirectory.set(layout.projectDirectory.dir("buildSrc/src/test/resources/publication"))
    workingDirectory.set(layout.buildDirectory.dir("publication-check/consumer"))
    testKitDirectory.set(layout.projectDirectory.dir(".gradle/publication-test-kit"))
    gradleInstallation.set(gradle.gradleHomeDir)
    gradleUserHome.set(gradle.gradleUserHomeDir)
    consumerJavaHome.set(project.extensions.getByType<JavaToolchainService>().launcherFor {
        languageVersion.set(JavaLanguageVersion.of(araraJVMInt))
    }.map { it.metadata.installationPath })
}

tasks.register<VerifyPublicationArtifacts>("checkPublications") {
    group = "verification"
    description = "Stage and verify Maven artifacts and run a Kotlin library consumer on Java 21."
    dependsOn(runPublicationConsumer)
    repositoryDirectory.set(publicationRepository)
    moduleNames.set(publicationModules)
    publicationVersion.set(project.version.toString())
}

val checkRuntimeDependencies = tasks.register<VerifyRuntimeDependencies>("checkRuntimeDependencies") {
    group = "verification"
    description = "Check shipped dependency bytecode against the minimum supported JVM."
    javaVersion.set(araraJVMInt)
    reportFile.set(layout.buildDirectory.file("reports/runtime-dependencies/result.txt"))
}

val multiplatformPluginId = libs.plugins.kotlin.multiplatform.get().pluginId

subprojects {
    plugins.withId(multiplatformPluginId) {

        apply(plugin = "org.jetbrains.dokka")

        val mainAttributes = mutableMapOf(
            "Implementation-Title" to "arara-${project.name}",
            "Implementation-Version" to version,
            "Multi-Release" to "true",
            "Automatic-Module-Name" to "${rootProject.group}.${project.name}"
        ).apply {
            if (project.name == "cli") put("Main-Class", "org.islandoftex.arara.cli.CLIKt")
        }

        configure<KotlinMultiplatformExtension> {
            jvmToolchain(araraJVMInt)
            compilerOptions {
                apiVersion.set(araraKotlinAPI)
                languageVersion.set(araraKotlinLanguage)
            }

            jvm {
                compilerOptions {
                    jvmTarget.set(araraJVMTarget)
                }
            }
        }

        if (project.name == "cli") {
            val externalRuntime = configurations.named("jvmRuntimeClasspath").map {
                it.incoming.artifactView {
                    componentFilter { component -> component is ModuleComponentIdentifier }
                }.files
            }
            checkRuntimeDependencies.configure { dependencies.from(externalRuntime) }
            tasks.named("check") { dependsOn(checkRuntimeDependencies) }
        }

        // With one platform, KMP does not populate the root sources jar itself.
        val commonSources = extensions.getByType<KotlinMultiplatformExtension>()
            .sourceSets.named("commonMain").map { it.kotlin }
        tasks.named<JvmJar>("sourcesJar") {
            from(commonSources) { into("commonMain") }
        }

        configure<DokkaExtension> {
            moduleName.set("arara (${project.name})")
            dokkaPublications.named("html") {
                suppressInheritedMembers.set(true)
                suppressObviousFunctions.set(true)
                failOnWarning.set(project.name in setOf("api", "core", "lua", "mvel"))
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

            withType<Jar>().configureEach {
                archiveBaseName.set("arara-${project.name}")
                manifest.attributes(mainAttributes)
            }

            withType<JavaCompile>().configureEach {
                options.release.set(araraJVMInt)
            }
        }

        apply<AraraPublication>()
        tasks.withType<PublishToMavenRepository>().configureEach {
            if (repository.name == "PublicationCheck") dependsOn(preparePublicationCheck)
        }
    }
}

// Exercise the packaged application without requiring a TeX installation.
tasks.register<Exec>("processSmoke") {
    group = "verification"
    description = "Run portable process checks against the runnable jar (JDK 21+)."
    dependsOn(":cli:shadowJar", ":cli:stageNativeRuntime", ":smoke-tests:runnerJar")
    val smokeJava = providers.gradleProperty("smokeJava").orElse(
        providers.systemProperty("java.home").map {
            Path.of(it, "bin", if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java")
                .toString()
        }
    )
    val report = layout.buildDirectory.file("test-results/processSmoke/results.xml")
    inputs.files(project(":smoke-tests").tasks.named("runnerJar"))
    inputs.files(project(":cli").tasks.named("shadowJar"))
    inputs.file(project(":cli").layout.buildDirectory.file("native/jna.jar"))
    inputs.property("smokeJava", smokeJava)
    outputs.file(report)
    outputs.upToDateWhen { false }
    doFirst {
        val jar = project(":cli").tasks.named<Jar>("shadowJar").get().archiveFile.get().asFile
        val runner = project(":smoke-tests").tasks.named<Jar>("runnerJar").get().archiveFile.get().asFile
        commandLine(
            smokeJava.get(), "-jar", runner.absolutePath,
            "--jar", jar.absolutePath, "--java", smokeJava.get(),
            "--jna", project(":cli").layout.buildDirectory.file("native/jna.jar").get().asFile.absolutePath,
            "--report", report.get().asFile.absolutePath
        )
    }
}

tasks.register<Exec>("texIntegration") {
    group = "verification"
    description = "Test official rules with pdflatex and bibtex on PATH."
    dependsOn(":cli:shadowJar", ":cli:stageNativeRuntime", ":smoke-tests:runnerJar")
    val smokeJava = providers.gradleProperty("smokeJava").orElse(
        providers.systemProperty("java.home").map {
            Path.of(it, "bin", if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java")
                .toString()
        }
    )
    val report = layout.buildDirectory.file("test-results/texIntegration/results.xml")
    outputs.file(report)
    outputs.upToDateWhen { false }
    doFirst {
        commandLine(smokeJava.get(), "-jar",
            project(":smoke-tests").tasks.named<Jar>("runnerJar").get().archiveFile.get().asFile.absolutePath,
            "--suite", "tex", "--jar", project(":cli").tasks.named<Jar>("shadowJar")
                .get().archiveFile.get().asFile.absolutePath,
            "--java", smokeJava.get(), "--jna",
            project(":cli").layout.buildDirectory.file("native/jna.jar").get().asFile.absolutePath,
            "--work-dir", layout.buildDirectory.dir("texIntegration").get().asFile.absolutePath,
            "--report", report.get().asFile.absolutePath)
    }
}

// Give runtime-only CI jobs the application, native library and smoke runner.
tasks.register<Copy>("stageProcessSmokeJar") {
    group = "verification"
    description = "Stage the application and smoke runner jars for a separate runtime job."
    dependsOn(":cli:shadowJar", ":cli:stageNativeRuntime", ":smoke-tests:runnerJar")
    from(provider { project(":cli").tasks.named<Jar>("shadowJar").get().archiveFile.get().asFile }) {
        rename { "arara.jar" }
    }
    from(provider { project(":smoke-tests").tasks.named<Jar>("runnerJar").get().archiveFile.get().asFile }) {
        rename { "runner.jar" }
    }
    from(project(":cli").layout.buildDirectory.file("native/jna.jar"))
    into(layout.buildDirectory.dir("processSmoke"))
}

apply<AraraPackaging>()

// Aggregate execution includes tests that exercise production code in other modules.
kover {
    reports {
        total {
            verify {
                onCheck.set(false)
                rule {
                    minBound(90)
                    minBound(70, kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
                }
            }
        }
    }
}
