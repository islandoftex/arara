// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.AraraPublication
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import tapmoc.TapmocExtension

// resolves plugin ids from the version catalog, so the ids live in one
// place only; reacting to a plugin by its catalog-declared id can never
// drift apart from the id the modules apply
fun pluginId(alias: String): String =
        the<VersionCatalogsExtension>()
                .named("libs")
                .findPlugin(alias)
                .get()
                .get()
                .pluginId

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.tapmoc) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.spotless.changelog)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
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
                "settings.gradle.kts",
                "buildSrc/build.gradle.kts",
                "api/build.gradle.kts",
                "core/build.gradle.kts",
                "lua/build.gradle.kts",
                "mvel/build.gradle.kts",
                "cli/build.gradle.kts"
        )
        targetExclude("src/test/**/*.kts")
        ktlint().editorConfigOverride(
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
                "buildSrc/src/**/*.kt"
        )
        targetExclude("src/test/**/*.kts")
        ktlint().editorConfigOverride(
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
    yaml {
        target("rules/*.yaml")
        val header = $$"""
            !config
            # arara, the cool TeX automation tool
            # Copyright (c) $YEAR, Island of TeX
            # All rights reserved.
            #
            # This rule is part of arara.
        """.trimIndent()
        licenseHeader(header, "identifier:")
    }
    format("luaRules") {
        target("rules/*.lua")
        val header = $$"""
            -- arara, the cool TeX automation tool
            -- Copyright (c) $YEAR, Island of TeX
            -- All rights reserved.
            --
            -- This rule is part of arara.
        """.trimIndent()
        licenseHeader(header, "return \\{")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    allRules = false
    source.from(files(
            "api/src/main/kotlin",
            "core/src/main/kotlin",
            "lua/src/main/kotlin",
            "mvel/src/main/kotlin",
            "cli/src/main/kotlin",
            "buildSrc/src/main/kotlin"
    ))
    buildUponDefaultConfig = true
    config.from(files("detekt-config.yml"))
}

val validateYamlRules: Boolean = providers
        .gradleProperty("validateYamlRules")
        .map { it.toBoolean() }
        .getOrElse(false)

val validateLuaRules: Boolean = providers
        .gradleProperty("validateLuaRules")
        .map { it.toBoolean() }
        .getOrElse(false)

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

    named("spotlessYamlCheck") {
        enabled = validateYamlRules
    }

    named("spotlessYamlApply") {
        enabled = validateYamlRules
    }

    named("spotlessLuaRulesCheck") {
        enabled = validateLuaRules
    }

    named("spotlessLuaRulesApply") {
        enabled = validateLuaRules
    }
}

version = spotlessChangelog.versionNext

// JVM and Kotlin compatibility targets, defined in gradle.properties;
// the compatibility plugin derives every compiler flag from these two
// values
val targetJvm = providers.gradleProperty("arara.jvm.target").get().toInt()

val targetKotlin = providers.gradleProperty("arara.kotlin.target").get()

// opt-in gate: run the build with -PwarningsAsErrors=true to prove that
// the tree compiles without a single warning
val warningsAsErrors = providers.gradleProperty("warningsAsErrors")
        .map { it.toBoolean() }
        .getOrElse(false)

allprojects {
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version
}

// the coverage plugin applied here aggregates the modules; each module
// joins the aggregate as a plain project dependency
dependencies {
    kover(project(":api"))
    kover(project(":core"))
    kover(project(":lua"))
    kover(project(":mvel"))
    kover(project(":cli"))
}

subprojects {

    apply(plugin = "org.jetbrains.dokka")

    // the modules apply the Kotlin plugin through their own plugins
    // blocks; the root reacts to each application instead of applying
    // the plugin itself, so the plugin has a single application point
    plugins.withId(pluginId("kotlin-jvm")) {

        configure<KotlinJvmProjectExtension> {

            compilerOptions {
                allWarningsAsErrors.set(warningsAsErrors)
                optIn.addAll(
                        "org.islandoftex.arara.api.localization.AraraMessages",
                        "kotlin.time.ExperimentalTime"
                )
            }
        }

        tasks {

            named<Test>("test") {
                useJUnitPlatform()

                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    events(
                            TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                            TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED
                    )
                }
            }
        }

        apply<AraraPublication>()
    }

    // the compatibility plugin derives every compiler flag from the two
    // target keys; the dependency check stays off because the current
    // stable release misreads module descriptors as bytecode violations —
    // once the fix ships in a newer stable release, add checkDependencies()
    // here and the gates take the floor verification over from the smoke's
    // Java 11 leg
    plugins.withId(pluginId("tapmoc")) {
        configure<TapmocExtension> {
            java(targetJvm)
            kotlin(targetKotlin)
        }
    }

    tasks {

        withType<Jar>().configureEach {
            archiveBaseName.set("arara-${project.name}")
            manifest {
                attributes["Implementation-Title"] = "arara-${project.name}"
                attributes["Implementation-Version"] = version

                if (project.name == "cli") {
                    attributes["Main-Class"] = "org.islandoftex.arara.cli.CLIKt"
                }

                // for Java 9+ compatibility of log4j
                attributes["Multi-Release"] = "true"
                attributes["Automatic-Module-Name"] = rootProject.group
            }
        }

        // DGP v2 configuration (the v1 dokkaHtml task is gone as of Dokka
        // 2.2); non-public members stay undocumented by default, and the
        // platform is derived from the Kotlin JVM plugin
        configure<DokkaExtension> {
            moduleName.set("arara (${project.name})")
            dokkaSourceSets.configureEach {
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                jdkVersion.set(targetJvm)
                sourceLink {
                    localDirectory.set(file("./"))
                    remoteUrl.set(uri("https://gitlab.com/islandoftex/arara"))
                    remoteLineSuffix.set("#L")
                }
                enableKotlinStdLibDocumentationLink.set(true)
                enableJdkDocumentationLink.set(true)
            }
        }
    }
}
