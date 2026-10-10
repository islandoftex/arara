// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import java.io.File
import org.gradle.api.Action
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.islandoftex.arara.build.AraraPublication
import org.islandoftex.arara.build.DocsConversion
import org.islandoftex.arara.build.GenerateSmokeBats
import org.islandoftex.arara.build.PrintSmokeJavaRuntime
import org.islandoftex.arara.build.RunSmokeBats
import org.islandoftex.chronoheader.ChronoField
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask
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
    alias(libs.plugins.solstice)
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
    // the major bump recognizes the documented phrase and the bold
    // marker of the current changelog convention; the match is a
    // plain, case sensitive contains, so the spellings cover the
    // phrase in prose, at sentence start, and as the marker in both
    // casings
    ifFoundBumpBreaking(
        "breaking change",
        "Breaking change",
        "**Breaking:**",
        "**BREAKING**",
    )
    tagPrefix("v")
    commitMessage("Release v{{version}}")
    remote("origin")
    branch("master")
}

// the promotion tool of the release flow: promoteChangelog is run by
// hand to promote the unreleased section and rewrite the diff links;
// CI never runs it - the promotion declares the release
solstice {
    branch = "master"
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

            (isNonStable(candidate.version) && !isNonStable(currentVersion)) ||
                    ("${candidate.group}:${candidate.module}" in pinnedModules &&
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
        // the scratch tree must resolve to one location for both the
        // virtual machine and the shell: the POSIX-style default splits
        // into two directories on Windows, where the shell maps /tmp to
        // its own tree while the machine writes to the drive root, so
        // Windows falls back to the machine's temp directory
        smokeDir.set(
            providers.environmentVariable("SMOKE_DIR").orElse(
                if (OperatingSystem.current().isWindows)
                    File(System.getProperty("java.io.tmpdir"), "arara-smoke")
                        .absolutePath
                else
                    "/tmp/arara-smoke"
            )
        )
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

// JVM and Kotlin compatibility targets + pinned modules, defined in
// gradle.properties; the compatibility plugin derives every compiler
// flag from the two targets, and manages the standard library the
// pinned set names, so their upgrade suggestions are noise rather
// than opportunities
val targetJvm = providers.gradleProperty("arara.jvm.target").get().toInt()
val targetKotlin = providers.gradleProperty("arara.kotlin.target").get()
val pinnedModules =
    providers.gradleProperty("arara.pinned").get().split(",").map(String::trim)

// the version macro the manual's LaTeX skeleton inputs: the number
// flows from the changelog through the project version, the same
// single source that drives the banner, the API object and the Gradle
// version itself — one truth, many faces
val generateVersionMacro = tasks.register("generateVersionMacro") {
    val target = layout.buildDirectory.file("generated/docs/arara-version.tex")
    val version = project.version.toString()
    inputs.property("version", version)
    outputs.file(target)
    doLast {
        target.get().asFile.writeText("\\def\\araraversion{$version}\n")
    }
}

// the conversion of the manual's Markdown into the LaTeX the
// skeleton compiles: each chapter's front matter names it, the
// headings and prose ride CoralMark's tag rules, the code fences
// ride the listings environments, the mermaid fences are
// extracted for the diagram pass, the devlab callout components
// render through the native component registry, and the tables
// ride the GFM element tree with their mixed content intact
val convertDocs = tasks.register("convertDocs") {
    val docsDir = file("docs")
    val outputDir = layout.buildDirectory.dir("generated/docs").get().asFile
    val assetsDir = outputDir.resolve("assets")
    inputs.dir(docsDir)
    outputs.dir(outputDir)
    doLast {
        // the committed assets — the static art and the diagram
        // checkpoints — join the output first, so a conversion without
        // the diagram pass still compiles complete books; the chapter
        // extraction below then rewrites the extracted sources from
        // the fences, which remain the single truth
        docsDir.resolve("tex/assets").takeIf { it.isDirectory }?.let { committed ->
            committed.copyRecursively(assetsDir, overwrite = true)
        }
        // the hand-written skeletons join the generated chapters, so
        // the compile step runs in one directory with no path juggling
        docsDir.resolve("tex").listFiles { f -> f.extension == "tex" }!!
            .forEach { skeleton ->
                skeleton.copyTo(outputDir.resolve(skeleton.name), overwrite = true)
            }
        docsDir.listFiles { f -> f.extension == "md" }!!
            .sortedBy { it.name }
            .forEach { source ->
                DocsConversion.convertChapter(source, outputDir, assetsDir)
            }
    }
}

// the diagram pass: the extracted mermaid sources become the vector
// figures the generated chapters reference. The endpoint rides a
// property so CI can point at its own Kroki while local runs fall
// back to the public service; the task needs the conversion's
// output, so it chains after it
val renderDiagrams: TaskProvider<Task> = tasks.register("renderDiagrams") {
    val generatedDir = layout.buildDirectory.dir("generated/docs")
    val checkpointDir = file("docs/tex/assets")
    val endpoint = providers.gradleProperty("arara.docs.kroki").orElse("https://kroki.io")
    dependsOn(convertDocs)
    inputs.property("endpoint", endpoint)
    doLast {
        val figures =
            DocsConversion.renderDiagrams(generatedDir.get().asFile.resolve("assets"), checkpointDir, endpoint.get())
        logger.lifecycle("Rendered {} diagram(s) through {}", figures.size, endpoint.get())
    }
}

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
// where the generated suite through the runnable jar exercises it.
// The aggregate floor guards the merged truth across all tests; the
// module builds carry floors for their own suites
kover {
    reports {
        filters {
            excludes {
                classes("org.islandoftex.arara.cli.CLIKt")
            }
        }
        verify {
            rule {
                minBound(92)
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

// the aggregate documentation: the plugin applied to the root collects
// the module source sets into one unified site, next to the per-module
// publications the modules configure themselves
apply(plugin = pluginId("dokka"))

configure<DokkaExtension> {
    moduleName.set("arara")
    dokkaPublications.named("html") {
        suppressInheritedMembers.set(true)
        suppressObviousFunctions.set(true)
        failOnWarning.set(true)
    }
    pluginsConfiguration.named(
        "html",
        DokkaHtmlPluginParameters::class.java
    ) {
        footerMessage.set("Island of TeX")
    }
}

// the plugin wires aggregation explicitly: the root declares the
// subprojects on the dedicated configuration, so its publication
// collects their module descriptors and the unified site covers all
// of arara at once
dependencies {
    add("dokka", project(":api"))
    add("dokka", project(":core"))
    add("dokka", project(":lua"))
    add("dokka", project(":mvel"))
    add("dokka", project(":cli"))
}

// the aggregated documentation as one distribution-ready artifact; it
// stays out of the Maven publications on purpose, as an all-modules
// artifact does not belong to a single library's coordinates
val htmlDocsJar = tasks.register<Jar>("htmlDocsJar") {
    description = "Assembles the aggregated Dokka HTML documentation jar."
    from(
        tasks
            .named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml")
            .flatMap { it.outputDirectory }
    )
    archiveClassifier.set("html-docs")
}

subprojects {

    apply(plugin = pluginId("dokka"))

    // the modules apply the Kotlin plugin through their own plugins
    // blocks; the root reacts to each application instead of applying
    // the plugin itself, so the plugin has a single application point
    plugins.withId(pluginId("kotlin-jvm")) {

        configure<KotlinJvmProjectExtension> {

            compilerOptions {
                allWarningsAsErrors.set(warningsAsErrors)
                optIn.addAll(
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

        // the CLI module is the application, not a library: it ships as
        // the runnable jar under its documented name and its thin jar is
        // a build intermediate. Maven coordinates for it would serve
        // neither embedders (the api module is the embedding surface)
        // nor runners (the fat jar is the distribution channel), and no
        // module depends on it - so the library modules alone publish
        if (name != "cli") {
            apply<AraraPublication>()
        }
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
                // every jar carries its own module name: a shared one
                // would put two arara jars on the module path in conflict
                attributes["Automatic-Module-Name"] = "${rootProject.group}.${project.name}"
            }
        }

        // the per-module documentation contract: an undocumented public
        // member is a warning and failOnWarning turns it into a build
        // failure, so the documented surface cannot decay; inherited
        // members and obvious functions are suppressed to keep the gate
        // honest, and deprecated members stay documented
        configure<DokkaExtension> {
            moduleName.set("arara (${project.name})")
            dokkaPublications.named("html") {
                suppressInheritedMembers.set(true)
                suppressObviousFunctions.set(true)
                failOnWarning.set(true)
            }
            pluginsConfiguration.named(
                "html",
                DokkaHtmlPluginParameters::class.java
            ) {
                footerMessage.set("Island of TeX")
            }
            dokkaSourceSets.configureEach {
                moduleName.set("arara (${project.name})")
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
