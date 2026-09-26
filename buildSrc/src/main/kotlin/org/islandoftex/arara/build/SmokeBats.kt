// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Renders the smoke scenarios as runner files. The output is a
 * generated artifact: the scenario definitions in this helper build
 * are the single source of truth, and the files exist only to be
 * consumed by the runner.
 */
abstract class GenerateSmokeBats : DefaultTask() {

    /** The directory the runner files are rendered into. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val output = outputDirectory.get().asFile
        output.mkdirs()
        output.listFiles()?.forEach { it.delete() }
        for ((name, content) in smokeScenarios()) {
            File(output, name).writeText(content)
        }
    }
}

/**
 * The end-to-end smoke entry point: builds the runnable jar and the
 * runner files through the task graph, provisions the scratch tree,
 * probes the environment before any work, and runs the full scenario
 * set on the underlying JVM and on the floor runtime. Scenarios whose
 * tools are absent skip loudly with their reasons; strict mode
 * escalates every skip into a failure for environments that promise
 * the tools.
 */
abstract class RunSmokeBats : DefaultTask() {

    /** The scratch tree the scenarios run in. */
    @get:Input
    abstract val smokeDir: Property<String>

    /** Escalates skips into failures, for environments as contracts. */
    @get:Input
    abstract val strict: Property<Boolean>

    /** The Java version the floor leg verifies on. */
    @get:Input
    abstract val floorVersion: Property<Int>

    /** The floor runtime launcher; absent means the leg skips loudly. */
    @get:Optional
    @get:Input
    abstract val floorJavaPath: Property<String>

    /** The generated runner files. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val batsFiles: ConfigurableFileCollection

    /** The runnable jar under verification. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val appJar: RegularFileProperty

    /** The execution-test fixtures the scenarios are provisioned from. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val fixturesRoot: DirectoryProperty

    /** The authored scenario sources. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val authoredRoot: DirectoryProperty

    /** The official rule sources, staged under their installed names. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val rulesRoot: DirectoryProperty

    /** The directory the per-leg reports are written into. */
    @get:OutputDirectory
    abstract val reportsDir: DirectoryProperty

    private lateinit var runner: String
    private val skips = mutableListOf<String>()

    @TaskAction
    fun run() {
        runner = probeRunner()
        probeGit()
        provisionTree()
        val texAvailable = resolveCommand("pdflatex") != null
        val files = eligibleFiles(texAvailable)
        if (files.isEmpty()) {
            throw GradleException("no runner files were generated")
        }
        if (!texAvailable) {
            skips += "the TeX scenarios (pdflatex not found)"
            logger.warn("SKIP: the TeX scenarios; pdflatex is not on the path")
        }
        runLeg(files, underlyingJava(), "underlying")
        val floor = floorJavaPath.orNull
        if (floor == null) {
            skips += "the Java ${floorVersion.get()} floor leg (no runtime resolved)"
            logger.warn(
                    "SKIP: the Java ${floorVersion.get()} floor leg — no floor runtime " +
                            "was resolved; the artifact was not exercised on the oldest " +
                            "supported floor"
            )
        } else {
            runLeg(files, floor, "floor")
        }
        if (strict.get() && skips.isNotEmpty()) {
            throw GradleException(
                    "strict mode: the smoke run skipped ${skips.size} part(s): " +
                            skips.joinToString("; ")
            )
        }
    }

    private fun probeRunner(): String {
        val override = System.getenv("SMOKE_BATS")
        if (override != null && File(override).canExecute()) {
            return override
        }
        val resolved = resolveCommand("bats")
        if (resolved == null) {
            throw GradleException(
                    "bats is not installed or not on the path; the smoke suite " +
                            "requires bats-core (https://bats-core.readthedocs.io), " +
                            "or set SMOKE_BATS to point at the runner"
            )
        }
        return resolved
    }

    private fun probeGit() {
        if (resolveCommand("git") == null) {
            throw GradleException(
                    "git is not installed or not on the path; the runner setup " +
                            "clones the assertion helpers on first use"
            )
        }
    }

    private fun resolveCommand(tool: String): String? {
        val path = runCatching {
            val process = ProcessBuilder("bash", "-c", "command -v $tool")
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output
        }.getOrNull() ?: return null
        return if (path.isNotEmpty() && File(path).canExecute()) path else null
    }

    private fun eligibleFiles(texAvailable: Boolean): List<File> =
            batsFiles.files
                    .flatMap { element ->
                        if (element.isDirectory) {
                            element.listFiles()?.filter { it.isFile }.orEmpty()
                        } else {
                            listOf(element)
                        }
                    }
                    .filter { it.extension == "bats" }
                    .filter { texAvailable || it.name !in texScenarioFiles }
                    .sortedBy { it.name }

    private fun underlyingJava(): String {
        val home = File(System.getProperty("java.home"))
        val binary = File(home, "bin/java")
        if (binary.canExecute()) {
            return binary.absolutePath
        }
        val windowsBinary = File(home, "bin/java.exe")
        return windowsBinary.absolutePath
    }

    private fun runLeg(files: List<File>, java: String, leg: String) {
        val reportDir = File(reportsDir.get().asFile, leg)
        reportDir.mkdirs()
        val report = File(reportDir, "report.tap")
        logger.lifecycle("== scenario matrix on the $leg JVM ($java)")
        val arguments = mutableListOf(
                runner,
                "--print-output-on-failure",
                "--report-formatter",
                "tap",
                "-o",
                reportDir.absolutePath,
        )
        files.forEach { arguments.add(it.absolutePath) }
        val process = ProcessBuilder(arguments)
                .directory(files.first().parentFile)
                .apply {
                    environment()["SMOKE_DIR"] = smokeDir.get()
                    environment()["SMOKE_JAVA"] = java
                    environment()["APP"] = File(
                            File(smokeDir.get(), "app"),
                            appJar.get().asFile.name,
                    ).absolutePath
                }
                .redirectErrorStream(true)
                .start()
        logger.lifecycle(process.inputStream.bufferedReader().readText().trim())
        if (process.waitFor() != 0) {
            val report = File(reportDir, "report.tap")
            throw GradleException(
                    "the $leg leg failed; the report is at ${report.absolutePath}"
            )
        }
    }

    private fun provisionTree() {
        val tree = File(smokeDir.get())
        val rules = File(tree, "app/rules")
        rules.mkdirs()
        for (dir in listOf("run1", "run2", "run3", "run4", "run5", "tex-db", "tex-yaml", "tex-lua")) {
            File(tree, dir).mkdirs()
        }
        val fixtures = fixturesRoot.get().asFile
        copyDir(File(fixtures, "simple-directive"), File(tree, "run1"))
        copyDir(File(fixtures, "lua-engine"), File(tree, "run2"))
        copyDir(File(fixtures, "lua-engine-conditionals"), File(tree, "run3"))
        copyDir(File(fixtures, "lua-engine-conditionals"), File(tree, "run4"))
        copyDir(File(fixtures, "lua-engine-options"), File(tree, "run5"))
        val authored = authoredRoot.get().asFile
        copyFile(File(authored, "dbcheck.tex"), File(tree, "tex-db/dbcheck.tex"))
        copyFile(File(authored, "pdflatex.tex"), File(tree, "tex-yaml/tex-yaml.tex"))
        copyFile(File(authored, "pdflatex.tex"), File(tree, "tex-lua/tex-lua.tex"))
        val rulesSource = rulesRoot.get().asFile
        copyFile(File(rulesSource, "arara-rule-pdflatex.yaml"), File(rules, "pdflatex.yaml"))
        copyFile(File(rulesSource, "arara-rule-pdflatex.lua"), File(rules, "pdflatex.lua"))
        copyFile(File(fixtures, "lua-engine-options/optechoer.lua"), File(rules, "optechoer.lua"))
        copyFile(
                appJar.get().asFile,
                File(File(tree, "app"), appJar.get().asFile.name),
        )
    }

    private fun copyDir(source: File, target: File) {
        source.listFiles()?.forEach { entry ->
            if (entry.isDirectory) {
                val nested = File(target, entry.name)
                nested.mkdirs()
                copyDir(entry, nested)
            } else {
                copyFile(entry, File(target, entry.name))
            }
        }
    }

    private fun copyFile(source: File, target: File) {
        target.parentFile.mkdirs()
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
