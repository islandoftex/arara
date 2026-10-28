// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.reporting

import kotlin.time.TimeMark
import kotlin.time.TimeSource
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.diagnostics.ToolDiagnostic
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.reporting.FileSummary
import org.islandoftex.arara.api.reporting.RunSummary
import org.islandoftex.arara.core.dependencies.ProjectValidation

/** Invocation-owned ledger; a renderer never owns execution state. */
class RunLedger(private val clock: TimeSource = TimeSource.Monotonic) {
    private val files = linkedMapOf<String, FileSummary>()
    private val fileDiagnostics = mutableMapOf<String, MutableList<ToolDiagnostic>>()
    private var current: String? = null
    private var started: TimeMark? = null
    /** Whether any resolved rule used the deprecated compatibility engine. */
    var deprecatedYamlUsed: Boolean = false
        private set

    /** Retain a compatibility warning across files and configuration changes. */
    fun deprecatedYaml() {
        deprecatedYamlUsed = true
    }

    /** Whether the prepared invocation contains more than one project identity. */
    val multipleProjects: Boolean get() = files.values.map { it.project to it.directory }.distinct().size > 1

    /** Replace the ledger with selected projects in execution order, including their unprocessed files. */
    fun prepare(projects: List<Project>, selected: Set<String> = projects.map { it.name }.toSet()) {
        files.clear()
        fileDiagnostics.clear()
        deprecatedYamlUsed = false
        current = null
        started = null
        ProjectValidation.executionOrder(projects).filter { it.name in selected }.forEach { project ->
            project.files.sortedWith(compareByDescending<ProjectFile> { it.priority }
                .thenBy { it.sourcePath.normalize().toString() }).forEach { file ->
                files[file.sourcePath.toString()] = FileSummary(file.sourcePath, project.name, project.workingDirectory)
            }
        }
    }

    /** Start timing and accounting for a source file. */
    fun start(file: ProjectFile, dryRun: Boolean) {
        current = file.sourcePath.toString()
        started = clock.markNow()
        update { it.copy(dryRun = dryRun) }
    }

    /** Count an evaluated task, including simulations. */
    fun task() = update { it.copy(tasks = it.tasks + 1) }

    /** Retain warnings and errors independently of diagnostic log filtering. */
    fun diagnostic(diagnostic: ToolDiagnostic) {
        if (diagnostic.level >= LogLevel.WARN) current?.let { path ->
            fileDiagnostics.getOrPut(path) { mutableListOf() }.add(diagnostic)
        }
    }

    /** Count a directive skipped by its condition. */
    fun skip() = update { it.copy(skipped = it.skipped + 1) }

    /** Finish the active file and release its timing state. */
    fun finish(exitCode: Int) {
        update { it.copy(exitCode = exitCode, elapsed = started?.elapsedNow()) }
        current = null
        started = null
    }

    /** Create an immutable snapshot in invocation order, retaining files never reached. */
    fun summary(success: Boolean, dryRun: Boolean): RunSummary = RunSummary(success, dryRun,
        files.values.map { file -> file.copy(diagnostics = fileDiagnostics[file.path.toString()]?.toList().orEmpty()) })

    private fun update(transform: (FileSummary) -> FileSummary) {
        current?.let { path -> files[path]?.let { files[path] = transform(it) } }
    }
}
