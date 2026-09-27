// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Action
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.islandoftex.arara.build.AraraPublication
import org.islandoftex.arara.build.GenerateSmokeBats
import org.islandoftex.arara.build.PrintSmokeJavaRuntime
import org.islandoftex.arara.build.RunSmokeBats
import org.islandoftex.chronoheader.ChronoField
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

// the license header both Kotlin formatting steps enforce, with the
// delimiter bounding the replaceable header region; it covers every
// token that can legally open either Kotlin file kind
private val spdxHeader = "// SPDX-License-Identifier: BSD-3-Clause"
private val spdxDelimiter = "(@file:|package |import |plugins |buildscript |pluginManagement )"

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
    alias(libs.plugins.chronoheader)
    `java-base`
}

if (
    !project.hasProperty("jobToken") &&
    gradle.startParameter.taskNames.any { requested ->
        val name = requested.substringAfterLast(':')
        name.contains("publish", ignoreCase = true) ||
                name.lowercase().startsWith("generatepomfile")
    }
) {
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
        licenseHeader(spdxHeader, spdxDelimiter)
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
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard" to "disabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ij_kotlin_imports_layout" to "*"
            )
        )
        licenseHeader(spdxHeader, spdxDelimiter)
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    format("yamlRules") {
        target("rules/*.yaml")
        val header = $$"""
            !config
            # arara, the cool TeX automation tool
            # Copyright (c) 2012-$YEAR, Island of TeX
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
            -- Copyright (c) 2012-$YEAR, Island of TeX
            -- All rights reserved.
            --
            -- This rule is part of arara.
        """.trimIndent()
        licenseHeader(header, "return \\{")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

chrono {
    file("LICENSE") { update(ChronoField.YEAR_INTERVAL) }
    pattern("rules/*.yaml") { update(ChronoField.YEAR_INTERVAL) }
    pattern("rules/*.lua") { update(ChronoField.YEAR_INTERVAL) }
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

// the smoke runtime is resolved through the toolchain service, which
// provisions on demand; the resolution happens eagerly here so that a
// missing floor degrades to a loud skip instead of failing every
// build, and the smoke tasks consume the resolved value
val smokeToolchainService = the<JavaToolchainService>()
val smokeFloorJava: String? = runCatching {
    smokeToolchainService.launcherFor(
            Action<JavaToolchainSpec> {
                languageVersion.set(JavaLanguageVersion.of(
                        providers.gradleProperty("smoke.jvm.version").get().toInt()))
                vendor.set(JvmVendorSpec.matching(providers.gradleProperty("smoke.jvm.vendor").get()))
            }
    ).get().executablePath.asFile.absolutePath
}.getOrNull()

tasks {

    withType<DependencyUpdatesTask> {
        rejectVersionIf {
            fun isNonStable(version: String) = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                    .any { qualifier ->
                        version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                    }

            // the standard library is managed by the compatibility
            // plugin, so upgrade suggestions for its requested pin are
            // noise rather than opportunities
            val pinnedElsewhere = setOf("org.jetbrains.kotlin:kotlin-stdlib")

            (isNonStable(candidate.version) && !isNonStable(currentVersion)) ||
                    ("${candidate.group}:${candidate.module}" in pinnedElsewhere &&
                            candidate.version != currentVersion)
        }

        checkForGradleUpdate = false
    }

    named("spotlessYamlRulesCheck") {
        enabled = validateYamlRules
    }

    named("spotlessYamlRulesApply") {
        enabled = validateYamlRules
    }

    named("spotlessLuaRulesCheck") {
        enabled = validateLuaRules
    }

    named("spotlessLuaRulesApply") {
        enabled = validateLuaRules
    }

    register<PrintSmokeJavaRuntime>("printSmokeJavaRuntime") {
        smokeVersion.set(providers.gradleProperty("smoke.jvm.version").map { it.toInt() })
        smokeVendor.set(providers.gradleProperty("smoke.jvm.vendor"))
        buildFloor.set(providers.gradleProperty("arara.jvm.target").map { it.toInt() })
        if (smokeFloorJava != null) {
            launcherPath.set(smokeFloorJava)
        }
    }

    val generateSmokeBats = register<GenerateSmokeBats>("generateSmokeBats") {
        outputDirectory.set(layout.buildDirectory.dir("smoke-bats"))
    }

    register<RunSmokeBats>("smoke") {
        dependsOn(":cli:shadowJar")
        dependsOn(generateSmokeBats)
        batsFiles.from(generateSmokeBats.flatMap { it.outputDirectory })
        appJar.set(project(":cli").tasks.named<Jar>("shadowJar").flatMap { it.archiveFile })
        fixturesRoot.set(layout.projectDirectory.dir("cli/src/test/resources/executiontests"))
        rulesRoot.set(layout.projectDirectory.dir("rules"))
        smokeDir.set(providers.environmentVariable("SMOKE_DIR").orElse("/tmp/arara-smoke"))
        strict.set(providers.gradleProperty("smoke.strict").map { it.toBoolean() }.orElse(false))
        floorVersion.set(providers.gradleProperty("smoke.jvm.version").map { it.toInt() })
        if (smokeFloorJava != null) {
            floorJavaPath.set(smokeFloorJava)
        }
        reportsDir.set(layout.buildDirectory.dir("reports/smoke"))
        outputs.upToDateWhen { false }
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

// the linting task parses against the build's JVM target
tasks.withType<Detekt>().configureEach {
    jvmTarget = if (targetJvm <= 8) "1.$targetJvm" else "$targetJvm"
}

allprojects {
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version
}

// the coverage plugin applied here aggregates the modules; each module
// joins the aggregate as a plain project dependency. The main entry
// point is excluded from the report: it only runs in the real process,
// where the generated suite through the runnable jar exercises it
kover {
    reports {
        filters {
            excludes {
                classes("org.islandoftex.arara.cli.CLIKt")
            }
        }
    }
}

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
