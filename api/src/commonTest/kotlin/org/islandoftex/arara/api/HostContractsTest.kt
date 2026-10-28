// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.diagnostics.ToolDiagnostic
import org.islandoftex.arara.api.files.BuildWorkspaceEvent
import org.islandoftex.arara.api.files.BuildWorkspaceOperation
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.reporting.FileSummary
import org.islandoftex.arara.api.reporting.RunSummary
import org.islandoftex.arara.api.reporting.WatchRun
import org.islandoftex.arara.api.reporting.WatchSummary
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.rules.DirectiveFetchingHooks
import org.islandoftex.arara.api.rules.RuleDiagnostic
import org.islandoftex.arara.api.rules.RuleDiscoveryResult
import org.islandoftex.arara.api.rules.RuleFormat
import org.islandoftex.arara.api.rules.RuleLocation
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.ExecutorHooks
import org.islandoftex.arara.api.ui.ConsoleProgress
import org.islandoftex.arara.api.ui.ConsoleSummaries

class HostContractsTest {
    private val directory = MPPPath(".").normalize()
    private val project = object : Project {
        override val name = "host"
        override val workingDirectory = directory
        override val files = emptySet<ProjectFile>()
        override val dependencies = emptySet<String>()
    }
    private val file = object : ProjectFile {
        override val path = directory.resolve("book.tex")
        override val priority = 0
        override val fileType = object : FileType {
            override val extension = "tex"
            override val pattern = "^%"
        }
        override fun fetchDirectives(parseOnlyHeader: Boolean) = emptyList<Directive>()
    }
    private val conditional = object : DirectiveConditional {
        override val type = DirectiveConditionalType.IF
        override val condition = "host condition"
    }

    @Test
    fun richSummariesBridgeToLegacyHostsWithoutLosingUnprocessedStatusesOrOrder() {
        var received = emptyList<Pair<String, Int?>>()
        var result = true
        var context = ""
        val sink = object : ConsoleSummaries {
            override fun printWrapped(text: String) = Unit
            override fun printProjectInformation(name: String, directory: String) { context = "$name@$directory" }
            override fun printRunSummary(success: Boolean, files: List<Pair<String, Int?>>) {
                received = files
                result = success
            }
            override fun printWatchSummary(summary: WatchSummary) = Unit
        }
        val warning = ToolDiagnostic("tex", LogLevel.WARN, "warning", 1, "book.tex", 12)
        val files = listOf(
            FileSummary(file.path, project.name, directory, 7, 10.milliseconds, diagnostics = listOf(warning)),
            FileSummary(directory.resolve("pending.tex"), project.name, directory)
        )
        sink.printProjectInformation(project.name, directory.toString(), true)
        sink.printRunSummary(RunSummary(false, false, files))
        assertFalse(result)
        assertEquals(files.map { it.path.toString() to it.exitCode }, received)
        assertEquals("${project.name}@$directory", context)
    }

    @Test
    fun progressDefaultsPreserveMessageOrderAndLeaveTheHostUsableAfterClose() {
        val lines = mutableListOf<String>()
        val host = object : ConsoleProgress {
            override fun printWrapped(text: String) { lines.add(text) }
        }
        host.startBuildDirectory("preparing")
        host.suspendProgress()
        host.finishBuildDirectory("finished")
        host.close()
        host.printWrapped("still open")
        assertEquals(listOf("preparing", "finished", "still open"), lines)
    }

    @Test
    fun defaultHooksDoNotConsumeDirectivesAndWorkspaceObserversRetainPartialFailureDetails() {
        val defaults = ExecutorHooks()
        val events = mutableListOf<BuildWorkspaceEvent>()
        val hooks = defaults.copy(buildWorkspace = events::add)
        val mark = TimeSource.Monotonic.markNow()
        val report = object : ExecutionReport {
            override val executionStarted = mark
            override val executionStopped = mark
            override val exitCode = 7
        }
        defaults.executeBeforeExecution()
        defaults.executeBeforeProject(project)
        defaults.executeBeforeFile(file)
        defaults.executeAfterFile(report)
        defaults.executeAfterProject(project)
        defaults.executeAfterExecution(report)
        val directives = emptyList<Directive>()
        assertSame(directives, defaults.processDirectives(file, directives))
        assertFalse(defaults.shouldStopExecution())
        val started = BuildWorkspaceEvent.Started(project, BuildWorkspaceOperation.PREPARE)
        val failed = BuildWorkspaceEvent.Finished(project, BuildWorkspaceOperation.PREPARE, false, 12.milliseconds, 2)
        hooks.buildWorkspace(started)
        hooks.buildWorkspace(failed)
        assertEquals(listOf(started, failed), events)
        val watched = WatchSummary(listOf(WatchRun(1, setOf(project.name), report.exitCode, failed.elapsed)), report.exitCode)
        assertEquals(failed.elapsed, watched.runs.single().elapsed)
    }

    @Test
    fun incompleteDirectiveFactoriesFailAtTheHostBoundary() {
        val hooks = DirectiveFetchingHooks()
        assertEquals("raw", hooks.processPotentialDirective(1, "raw"))
        assertFailsWith<NotImplementedError> { hooks.buildDirectiveRaw("tex", null, conditional, listOf(1)) }
        assertFailsWith<NotImplementedError> { hooks.buildDirective("tex", emptyMap(), conditional, listOf(1)) }
    }

    @Test
    fun discoveryLocationsDeriveTheirFormatFromTheCurrentPathAndRetainShadowedCandidates() {
        val selected = RuleLocation("probe", directory.resolve("probe.${RuleFormat.LUA.extension}"), true)
        val fallback = selected.copy(path = directory.resolve("probe.${RuleFormat.MVEL.extension}"), effective = false)
        val result = RuleDiscoveryResult(listOf(selected, fallback), listOf(RuleDiagnostic(null, "unavailable directory")))
        assertEquals(listOf("lua", "yaml"), result.rules.map { it.format })
        assertEquals(listOf(true, false), result.rules.map { it.effective })
        assertEquals("unavailable directory", result.diagnostics.single().message)
    }
}
