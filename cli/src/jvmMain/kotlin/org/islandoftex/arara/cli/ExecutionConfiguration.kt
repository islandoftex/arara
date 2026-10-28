// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.session.ExecutorHooks
import org.islandoftex.arara.cli.console.ConsoleMode
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.cli.utils.RuntimeDiagnostics
import org.islandoftex.arara.cli.utils.printFileInformation
import org.islandoftex.arara.cli.utils.reportBuildWorkspace
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.session.Session

internal class ExecutionConfiguration(
    val project: (Project) -> Unit,
    val refresh: () -> Unit,
    val console: () -> ConsoleMode
)

internal fun executionHooks(
    engine: RuleEngine, watching: Boolean, preamble: String?, configuration: ExecutionConfiguration
) = ExecutorHooks(
    shouldStopExecution = { watching && Thread.currentThread().isInterrupted },
    buildWorkspace = ::reportBuildWorkspace,
    executeBeforeExecution = {
        RunLog.executionStarted(AraraAPI.version)
        Session.updateEnvironmentVariables()
        // directive processing has to be initialized, so that the core
        // component respects our MVEL processing
        DirectiveUtils.initializeDirectiveCore(engine)
    },
    executeBeforeProject = { project ->
        configuration.project(project)
        RuntimeDiagnostics.record(
            project, engine.name.lowercase(), configuration.console().name.lowercase(),
            DisplayUtils.configurationFileName.takeUnless { it == "[none]" })
        DisplayUtils.renderer.printProjectInformation(project.name, project.workingDirectory.toString(),
            DisplayUtils.presentation.multipleProjects)
    },
    executeBeforeFile = {
        configuration.refresh()
        it.printFileInformation()
    },
    executeAfterFile = { report ->
        DisplayUtils.printFileResult(report.exitCode)
        // add an empty line between file executions
        println()
    },
    processDirectives = { file, list ->
        DirectiveUtils.process(
            file,
            list
                .takeIf {
                    list.isNotEmpty() && !PreambleState.prependPreambleIfDirectivesGiven &&
                        preamble == null
                }
                ?: prependPreambleDirectives(preamble, file.fileType, list),
            engine
        )
    }
)
