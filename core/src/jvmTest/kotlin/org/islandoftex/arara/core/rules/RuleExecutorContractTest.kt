// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile as APIProjectFile
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.LinearExecutor
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RuleExecutorContractTest {
    /** Observes helpers where directives run, with a real current project and file. */
    private fun inProject(
        files: List<Pair<String, Int>> = listOf("main.tex" to 0),
        options: ExecutionOptions = ExecutionOptions(),
        work: (MPPPath, APIProjectFile) -> Unit
    ) {
        val root = Files.createTempDirectory("arara helper project ").toMPPPath()
        val previousHooks = LinearExecutor.hooks
        val previousOptions = LinearExecutor.executionOptions
        val previousHalt = HaltRequest.exitCode
        try {
            val inputs = files.map { (name, priority) ->
                val input = root.resolve(name).apply { writeText("no directives\n") }
                object : ProjectFile(input, FileType("tex", "^%"), priority) {
                    override fun fetchDirectives(parseOnlyHeader: Boolean) =
                        emptyList<org.islandoftex.arara.api.rules.Directive>()
                }
            }
            LinearExecutor.executionOptions = options
            LinearExecutor.hooks = ExecutorHooks(processDirectives = { file, directives ->
                work(root, file)
                directives
            })
            assertEquals(0, LinearExecutor.execute(listOf(Project("helpers", root, inputs.toSet()))).exitCode)
        } finally {
            LinearExecutor.hooks = previousHooks
            LinearExecutor.executionOptions = previousOptions
            HaltRequest.clear()
            previousHalt?.let { HaltRequest.request(it) }
            root.removeDirectory()
        }
        assertNull(LinearExecutor.currentFile)
        assertNull(LinearExecutor.currentProject)
    }

    @Test
    fun commandAndReferenceHelpersUseTheCurrentProjectAndFile() {
        var visited = 0
        inProject { root, file ->
            visited++
            assertEquals("main.tex", RuleMethods.originalFile)
            assertEquals(file.path, RuleMethods.originalReference)
            assertEquals(file.path, ConditionalMethods.currentFile())
            val commands = listOf(
                RuleMethods.getCommand(listOf("pdflatex", "-opt")),
                RuleMethods.getCommand("pdflatex", listOf("-opt"), null, "")
            )
            commands.forEach { command ->
                assertEquals(listOf("pdflatex", "-opt"), command.elements)
                assertEquals(root, command.workingDirectory)
            }
        }
        assertEquals(1, visited)
    }

    @Test
    fun originalReferenceRemainsTheLowestPriorityFileWhileDependenciesRun() {
        val visited = mutableListOf<String>()
        inProject(listOf("main.tex" to 0, "dependency.tex" to 1)) { root, file ->
            visited.add(file.path.fileName)
            assertEquals(root.resolve("main.tex"), RuleMethods.originalReference)
            assertEquals("main.tex", RuleMethods.originalFile)
            assertEquals(file.path, ConditionalMethods.currentFile())
        }
        assertEquals(listOf("dependency.tex", "main.tex"), visited)
    }

    @Test
    fun extensionChecksResolveAgainstEachCurrentFile() {
        inProject(listOf("main.tex" to 0, "dependency.tex" to 1)) { root, file ->
            val log = root.resolve(file.path.fileName.removeSuffix(".tex") + ".log")
            log.writeText("error in line 3")
            assertTrue(ConditionalMethods.exists("log"))
            assertFalse(ConditionalMethods.missing("log"))
            assertFalse(ConditionalMethods.exists("aux"))
            assertTrue(ConditionalMethods.missing("aux"))
            assertTrue(ConditionalMethods.found("log", "error.*3"))
            assertFalse(ConditionalMethods.found("log", "warning"))
        }
    }

    @Test
    fun changeChecksPersistHashesAndDetectSubsequentWrites() {
        inProject { root, _ ->
            val auxiliary = root.resolve("main.aux").apply { writeText("first") }
            assertTrue(ConditionalMethods.changed("aux"))
            assertFalse(ConditionalMethods.changed(auxiliary))
            assertTrue(ConditionalMethods.unchanged("aux"))
            auxiliary.writeText("second")
            assertFalse(ConditionalMethods.unchanged(auxiliary))
            assertTrue(ConditionalMethods.unchanged(auxiliary))
            val watched = root.resolve("watch.log").apply { writeText("watched") }
            assertTrue(ConditionalMethods.changed(watched))
            assertFalse(ConditionalMethods.changed(watched))
            assertTrue(root.resolve("arara.yaml").isRegularFile)
        }
    }

    @Test
    fun subdirectoryChecksUseTheProjectRootAndRequireDirectories() {
        inProject { root, _ ->
            val nested = root.resolve("nested").apply { createDirectories() }
            assertTrue(RuleMethods.isSubdirectory(nested))
            assertFalse(RuleMethods.isSubdirectory(root.parent))
            assertFalse(RuleMethods.isSubdirectory(root.resolve("main.tex")))
            assertFalse(RuleMethods.isSubdirectory(root.resolve("missing")))
        }
    }

    @Test
    fun verboseChecksReflectTheExecutionOptions() {
        for (verbose in listOf(false, true)) {
            inProject(options = ExecutionOptions(verbose = verbose)) { _, _ ->
                assertEquals(verbose, RuleMethods.isVerboseMode())
            }
        }
    }

    @Test
    fun safeModeRejectsSystemCommandsAndEveryClassLoadingOverload() {
        inProject(options = ExecutionOptions(executionMode = ExecutionMode.SAFE_RUN)) { root, _ ->
            val command = RuleMethods.getCommandWithWorkingDirectory(root, "must-not-run")
            assertFailsWith<SafeRunViolationException> { RuleMethods.unsafelyExecuteSystemCommand(command) }
            val missing = root.resolve("must-not-open.jar")
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadClass(missing, "Plugin") }
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadClass(missing.toString(), "Plugin") }
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadObject(missing, "Plugin") }
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadObject(missing.toString(), "Plugin") }
        }
    }

    @Test
    fun systemCommandHelpersRetainSuccessAndLaunchFailureResults() {
        inProject { root, _ ->
            val name = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
            val java = MPPPath(System.getProperty("java.home")).resolve("bin").resolve(name)
            val success = RuleMethods.unsafelyExecuteSystemCommand(
                RuleMethods.getCommandWithWorkingDirectory(root, java.toString(), "-version")
            )
            assertEquals(0, success.first)
            val missing = root.resolve("missing-executable").toString()
            val failure = RuleMethods.unsafelyExecuteSystemCommand(
                RuleMethods.getCommandWithWorkingDirectory(root, missing)
            )
            assertEquals(Environment.ERROR_EXIT_STATUS, failure.first)
            assertTrue(missing in failure.second)
        }
    }
}
