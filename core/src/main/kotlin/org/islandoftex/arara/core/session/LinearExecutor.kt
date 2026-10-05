// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.Executor
import org.islandoftex.arara.core.dependencies.ProjectGraph
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements arara's linear executor, running the directives of every
 * project file sequentially, driven by the executor hooks the command
 * line application installs.
 */
object LinearExecutor : Executor {
    /**
     * The logger of the linear executor.
     */
    private val logger = KotlinLogging.logger { }

    /**
     * Specify custom user hooks to run.
     */
    var hooks = ExecutorHooks()

    /**
     * The project this executor currently executes, if any. This will always
     * be set before the [ExecutorHooks.executeBeforeProject] hook is executed
     * and unset after the [ExecutorHooks.executeAfterProject] hook.
     */
    var currentProject: Project? = null
        private set

    /**
     * The file this executor currently works on, if any. This will always
     * be set after the [ExecutorHooks.executeBeforeFile] hook is executed
     * and unset before the [ExecutorHooks.executeAfterFile] hook.
     */
    var currentFile: ProjectFile? = null
        private set

    /**
     * The setup for all executions run by this executor. The
     * execution options should not change while a file is under
     * execution - the setter rejects exactly that window.
     */
    override var executionOptions: ExecutionOptions =
        org.islandoftex.arara.core.configuration.ExecutionOptions()
        set(value) {
                if (currentFile != null)
                    throw AraraException(
                        // this is a developer contract violation (an
                        // embedding changing the options mid-file), not
                        // a user error, so it deliberately stays an
                        // English exception outside the localized
                        // catalog
                        "Cannot change execution options while " +
                            "executing the file ${currentFile?.path}."
                    )
                if (value.parallelExecution)
                    throw AraraException(
                        "This executor does not support " +
                            "parallel execution."
                    )
                field = value
            }

    /**
     * Execute rules based on the projects. Should roughly implement the
     * following steps:
     *
     * 1. Resolve project dependencies and create an appropriate execution
     *    order.
     * 2. Run arara's main routines on each project sequentially. Within a
     *    project, follow the file priorities.
     * 3. Wait for all tasks to finish.
     *
     * @param projects The projects to act on.
     */
    override fun execute(projects: List<Project>): ExecutionReport {
        val projectsInOrder = ProjectGraph().apply { addAll(projects) }.kahn()
        // Kahn's algorithm silently omits vertices involved in a cycle, so
        // the topological order has to be verified to cover every project;
        // otherwise cyclic projects would simply not be executed at all
        if (projectsInOrder.size < projects.size) {
            val cyclic = projects
                .filter { project -> projectsInOrder.none { it.name == project.name } }
                .map { it.name }
                .joinToString(", ", "(", ")")
            throw AraraException(
                LanguageController.messages.errorProjectCycle(cyclic)
            )
        }
        hooks.executeBeforeExecution()
        // the resolved order is the run's own narrative: the log
        // records it once, before any project starts, so the
        // reader knows the sequence before the details follow
        logger.info {
            "Project execution order resolved as: " +
                projectsInOrder.joinToString(", ") { it.name }
        }
        val executionStarted = TimeSource.Monotonic.markNow()
        var exitCode = 0
        for (project in projectsInOrder) {
            val projectCode = executeProject(project)
            // the run remembers the last error it saw: without the
            // error-halt option a failure anywhere is still what
            // the run reports, whatever succeeded after it
            if (projectCode != 0) exitCode = projectCode
            if (executionOptions.haltOnErrors && projectCode != 0)
                break
        }
        val executionEnded = TimeSource.Monotonic.markNow()
        val executionReport = ExecutionReport(executionStarted, executionEnded, exitCode)
        hooks.executeAfterExecution(executionReport)
        return executionReport
    }

    /**
     * Executes the files of a project in priority order, running the
     * per-project and per-file hooks around each file and halting
     * on the first failure when the error-halt option is set. The
     * project is unset even when an exception escapes, as a stale
     * project would make the executor reject all future execution
     * option changes. A file the run stops at skips the closing
     * hooks of the file and the project: the stop returns before
     * they run.
     *
     * @param project The project to execute.
     * @return The exit code the project reports: the last error
     *   seen among its files, or zero when they all succeeded.
     */
    internal fun executeProject(project: Project): Int {
        var exitCode = 0
        currentProject = project
        try {
            hooks.executeBeforeProject(project)
            // the priority decides the order, and the path breaks the
            // ties: the map form of the specification has no
            // declaration order to keep, so the resolved path gives
            // every run the same deterministic sequence
            for (file in project.files.sortedWith(
                compareByDescending<ProjectFile> { it.priority }
                    .thenBy { it.path.toString() }
            )) {
                hooks.executeBeforeFile(file)
                val executionReport = execute(file)
                // the last error of the project survives a later
                // success when the error-halt option is off
                if (executionReport.exitCode != 0)
                    exitCode = executionReport.exitCode
                if (executionOptions.haltOnErrors &&
                    executionReport.exitCode != 0
                ) {
                    return executionReport.exitCode
                }
                hooks.executeAfterFile(executionReport)
            }
            hooks.executeAfterProject(project)
        } finally {
            // the project is observable state the run exposes to the
            // helpers - the original reference and the working
            // directory of the interpreter's directives read it -
            // so it is unset even when an exception escapes a hook
            // or a file execution
            currentProject = null
        }
        return exitCode
    }

    /**
     * Builds the session key under which the halt request of a file
     * is recorded: the full reference path tells the requests of
     * same-named files apart, and the request lives exactly as
     * long as the file's own processing.
     *
     * @param file The file the request belongs to.
     * @return The session key of the file's halt request.
     */
    fun haltKeyFor(file: ProjectFile): String =
        "arara:${file.path}:halt"

    /**
     * Performs arara's main routine to run a file.
     *
     * A halt request raised by the file's rules dies with the
     * file's own processing: its verdict has been spoken, and a
     * later file - even a same-named one - starts with a clean
     * sheet.
     *
     * @param file The file to run.
     */
    override fun execute(file: ProjectFile): ExecutionReport {
        currentFile = file
        try {
            val executionStarted = TimeSource.Monotonic.markNow()
            val directives = hooks.processDirectives(
                file,
                file.fetchDirectives(executionOptions.parseOnlyHeader)
            )
            var exitCode = 0
            for (directive in directives) {
                val directiveCode = directive.execute()
                // the file remembers the last error its directives
                // produced, so a success after a failure cannot
                // mask it
                if (directiveCode != 0) exitCode = directiveCode
                if (executionOptions.haltOnErrors && directiveCode != 0) {
                    break
                }
            }
            return ExecutionReport(
                executionStarted, TimeSource.Monotonic.markNow(), exitCode
            )
        } finally {
            // the file has to be unset even when directive processing or
            // execution throws, as a stale file would make the executor
            // reject all future execution option changes
            if (Session.contains(haltKeyFor(file))) {
                Session.remove(haltKeyFor(file))
            }
            currentFile = null
        }
    }
}
