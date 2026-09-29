// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.DirectiveProcessing
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Verifies the interpreter's halt contract: when a halt was requested for
 * the file being processed, [org.islandoftex.arara.cli.interpreter.Interpreter]
 * returns the requested status as the exit code without running any further
 * commands of the rule.
 *
 * The halt request itself (the `halt()` helper storing the status under the
 * halt key of the original file) requires the executor's project state, so
 * it is exercised end-to-end by the pipeline scenarios in
 * [org.islandoftex.arara.cli.utils.ExecutionTest]; this spec seeds the
 * session explicitly to observe the interpreter's decision in isolation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterpreterTest {
    private val texFile = FileType("tex", "^\\s*%\\s+")

    @BeforeAll
    fun setUp() {
        // the directive machinery has to be initialized, as the directive
        // construction relies on the installed hooks
        DirectiveProcessing.initializeDirectiveCore()
    }

    @AfterAll
    fun tearDown() {
        // the scenarios seed halt keys to observe the interpreter's
        // decision; leaving them behind would short-circuit the halt
        // scenarios of the other specs
        Session.clear()
    }

    private fun haltDirectiveFor(filePath: MPPPath): org.islandoftex.arara.api.rules.Directive =
        Directives.extractDirectives(
            listOf("% arara: halt"),
            false, texFile
        ).single().run {
            DirectiveImpl(
                identifier, parameters.plus("reference" to filePath.toString()),
                conditional, lineNumbers
            )
        }

    private fun executeWithHaltStatus(
        rulePath: MPPPath,
        filePath: MPPPath,
        haltStatus: Int
    ): Int {
        // a fresh session guarantees the observed halt status is the one
        // this scenario seeds, not a leftover of a previous scenario
        Session.clear()
        Session.put("arara:${filePath.fileName}:halt", haltStatus)
        return Interpreter(
            ExecutionOptions(rulePaths = setOf(rulePath)),
            ProjectFile(filePath, texFile), filePath.parent
        ).execute(haltDirectiveFor(filePath)).exitCode
    }

    @Test
    fun `returns a zero exit code for an ordinary halt`() {
        val rulePath = MPPPath("../rules").normalize()
        val filePath = MPPPath("src/test/resources/executiontests/halt")
            .normalize() / "halt.tex"
        assertEquals(
            0,
            executeWithHaltStatus(rulePath, filePath, 0),
            "an ordinary halt exits with zero"
        )
    }

    @Test
    fun `returns a non-zero exit code for an error halt`() {
        val rulePath = MPPPath("src/test/resources/executiontests/halt-error")
            .normalize()
        val filePath = MPPPath(rulePath / "halt-error.tex")
        assertNotEquals(
            0,
            executeWithHaltStatus(rulePath, filePath, 1),
            "an error halt exits with a non-zero code"
        )
    }

    @Test
    fun `rejects non numeric halt statuses`() {
        Session.clear()
        val rulePath = MPPPath("../rules").normalize()
        val filePath = MPPPath("src/test/resources/executiontests/halt")
            .normalize() / "halt.tex"
        Session.put("arara:${filePath.fileName}:halt", "not-a-number")
        assertFailsWith<AraraException>(
            "a non numeric halt status must be rejected"
        ) {
            Interpreter(
                ExecutionOptions(rulePaths = setOf(rulePath)),
                ProjectFile(filePath, texFile), filePath.parent
            ).execute(haltDirectiveFor(filePath))
        }
    }

    @Test
    fun `reports rules that cannot be found`() {
        Session.clear()
        val rulePath = MPPPath("../rules").normalize()
        val filePath = MPPPath("src/test/resources/executiontests/halt")
            .normalize() / "halt.tex"
        val directive = Directives.extractDirectives(
            listOf("% arara: notarealrulequack"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        assertFailsWith<AraraException>(
            "an unknown rule must be reported, not silently skipped"
        ) {
            Interpreter(
                ExecutionOptions(rulePaths = setOf(rulePath)),
                ProjectFile(filePath, texFile), filePath.parent
            ).execute(directive)
        }
    }

    @Test
    fun `names the sought rule file in the engine's own format`() {
        Session.clear()
        val filePath = MPPPath("src/test/resources/executiontests/halt")
            .normalize() / "halt.tex"
        val directive = Directives.extractDirectives(
            listOf("% arara: notarealrulequack"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        listOf(
            Pair(RuleEngine.YAML, ".yaml"),
            Pair(RuleEngine.LUA, ".lua")
        ).forEach { (engine, extension) ->
            RuleEngine.current = engine
            val exception = assertFailsWith<AraraException>(
                "an unknown rule must be reported under $engine"
            ) {
                Interpreter(
                    ExecutionOptions(
                        rulePaths = setOf(MPPPath("../rules").normalize())
                    ),
                    ProjectFile(filePath, texFile), filePath.parent
                ).execute(directive)
            }
            assertTrue(
                exception.message?.contains(extension) == true,
                "the message names the $extension file: ${exception.message}"
            )
            val other = if (engine == RuleEngine.YAML) ".lua" else ".yaml"
            assertTrue(
                exception.message?.contains(other) == false,
                "the message does not name the other format: ${exception.message}"
            )
        }
        RuleEngine.current = RuleEngine.YAML
    }

    @Test
    fun `reports wrong rule return types`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-broken.yaml"),
            """
            !config
            identifier: broken
            name: Broken
            authors:
            - Island of TeX
            commands:
            - name: The broken tool
              command: >
                @{ return 42; }
            arguments: []
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: broken"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        assertFailsWith<AraraException>(
            "a rule returning neither a command nor a boolean is wrong"
        ) {
            Interpreter(
                ExecutionOptions(rulePaths = setOf(MPPPath(directory.toString()))),
                ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
        }
    }

    @Test
    fun `skips command execution in dry run mode`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-dry.yaml"),
            """
            !config
            identifier: dry
            name: Dry
            authors:
            - Island of TeX
            commands:
            - name: The dry tool
              command: >
                @{ return true; }
            arguments: []
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: dry"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        val options = ExecutionOptions(
            rulePaths = setOf(MPPPath(directory.toString())),
            executionMode = ExecutionMode.DRY_RUN
        )
        val output = ByteArrayOutputStream()
        val sysout = System.out
        try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            val status = Interpreter(
                options, ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
            assertEquals(
                0,
                status.exitCode,
                "a dry run does not execute commands"
            )
        } finally {
            System.setOut(sysout)
        }
        assertTrue(
            output.toString(Charsets.UTF_8).isNotBlank(),
            "the dry run displays the skipped entry"
        )
    }

    @Test
    fun `halts on unsuccessful commands`() {
        Session.clear()
        val status = executeFailingRule(haltOnErrors = true)
        assertNotEquals(
            0,
            status,
            "an unsuccessful command halts the run when errors halt"
        )
    }

    @Test
    fun `continues past failures without halt on errors`() {
        Session.clear()
        val status = executeFailingRule(haltOnErrors = false)
        assertNotEquals(
            0,
            status,
            "the failure surfaces as the exit code without halting"
        )
    }

    /**
     * Runs a directive whose rule command exits non-zero through a
     * stateless command expression, returning the exit code the
     * interpreter reports; the halt expectation surfaces as an
     * exception, the lenient mode as the exit code.
     */
    private fun executeFailingRule(haltOnErrors: Boolean): Int {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        val java = ProcessHandle.current().info().command().orElse("java")
        Files.writeString(
            directory.resolve("arara-rule-failing.yaml"),
            """
            !config
            identifier: failing
            name: Failing
            authors:
            - Island of TeX
            commands:
            - name: The failing tool
              command: >
                @{ return getCommandWithWorkingDirectory(
                '${forMvel(directory)}', '${forMvel(java)}', '-wrongflag-quack'); }
            arguments: []
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: failing"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        val options = ExecutionOptions(
            rulePaths = setOf(MPPPath(directory.toString())),
            haltOnErrors = haltOnErrors
        )
        val status = Interpreter(
            options, ProjectFile(MPPPath(filePath.toString()), texFile),
            MPPPath(filePath.parent.toString())
        ).execute(directive)
        return status.exitCode
    }

    /**
     * Renders a path for embedding into a single-quoted MVEL string:
     * the expression language processes backslash escapes inside its
     * strings, so Windows separators have to be doubled.
     */
    private fun forMvel(path: Any): String =
        path.toString().replace("\\", "\\\\")

    /**
     * Runs a directive whose rule command appends one mark per
     * execution to a trace file, under the provided conditional, and
     * returns how many marks the loop machinery allowed.
     */
    private fun loopTrace(conditionalText: String, maxLoops: Int): Int {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        val trace = Files.createTempFile("arara-loop-trace", ".txt")
        Files.writeString(trace, "")
        val escapedTrace = trace.toString().replace("\\", "\\\\")
        Files.writeString(
            directory.resolve("arara-rule-looper.yaml"),
            """
            !config
            identifier: looper
            name: Looper
            authors:
            - Island of TeX
            commands:
            - name: The looping tool
              command: >
                @{ writeToFile("$escapedTrace", "x", true); return true; }
            arguments: []
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: looper $conditionalText"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        val status = Interpreter(
            ExecutionOptions(
                rulePaths = setOf(MPPPath(directory.toString())),
                maxLoops = maxLoops
            ),
            ProjectFile(MPPPath(filePath.toString()), texFile),
            MPPPath(filePath.parent.toString())
        ).execute(directive)
        assertEquals(
            0,
            status.exitCode,
            "a loop that hits its cap reports success"
        )
        return Files.readString(trace).length
    }

    @Test
    fun `caps while loops at the configured maximum`() {
        assertEquals(
            2,
            loopTrace("while true", maxLoops = 2),
            "the while body runs exactly the configured maximum of times"
        )
    }

    @Test
    fun `evaluates rule flags into the command context`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-flagged.yaml"),
            """
            !config
            identifier: flagged
            name: Flagged
            authors:
            - Island of TeX
            commands:
            - name: The echoer
              command: >
                @{ return isWindows(getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'cmd', '/c', 'echo',
                'marker=', marker, 'items=', joined),
                getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'echo', 'marker=', marker,
                'items=', joined)); }
            arguments:
            - identifier: marker
              flag: '@{ return parameters.marker; }'
            - identifier: joined
              flag: '@{ return ["a", "b"]; }'
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: flagged: { marker: 'x', joined: 'ignored' }"),
            false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        val output = ByteArrayOutputStream()
        val sysout = System.out
        val originalOptions = LinearExecutor.executionOptions
        try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            // the command output tee follows the global verbose flag, so
            // the global has to see it as well, not only the interpreter
            LinearExecutor.executionOptions =
                ExecutionOptions.from(originalOptions).copy(verbose = true)
            val status = Interpreter(
                ExecutionOptions(
                    rulePaths = setOf(MPPPath(directory.toString())),
                    verbose = true
                ),
                ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
            assertEquals(0, status.exitCode, "the flagged rule runs")
        } finally {
            LinearExecutor.executionOptions = originalOptions
            System.setOut(sysout)
        }
        val echoed = output.toString(Charsets.UTF_8)
        assertTrue(
            "marker= x" in echoed,
            "a string flag flows into the command: $echoed"
        )
        assertTrue(
            "items= a b" in echoed,
            "a list flag flows into the command: $echoed"
        )
    }

    @Test
    fun `warns on unsupported flag return types through the string fallback`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-numeric.yaml"),
            """
            !config
            identifier: numeric
            name: Numeric
            authors:
            - Island of TeX
            commands:
            - name: The echoer
              command: >
                @{ return isWindows(getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'cmd', '/c', 'echo', 'value=', value),
                getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'echo', 'value=', value)); }
            arguments:
            - identifier: value
              flag: '@{ return 42; }'
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: numeric: { value: 'ignored' }"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        val output = ByteArrayOutputStream()
        val sysout = System.out
        val originalOptions = LinearExecutor.executionOptions
        try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            LinearExecutor.executionOptions =
                ExecutionOptions.from(originalOptions).copy(verbose = true)
            Interpreter(
                ExecutionOptions(
                    rulePaths = setOf(MPPPath(directory.toString())),
                    verbose = true
                ),
                ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
        } finally {
            LinearExecutor.executionOptions = originalOptions
            System.setOut(sysout)
        }
        assertTrue(
            "value= 42" in output.toString(Charsets.UTF_8),
            "an unsupported flag return falls back to its string form"
        )
    }

    @Test
    fun `wraps failing flag expressions`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-brokenflag.yaml"),
            """
            !config
            identifier: brokenflag
            name: BrokenFlag
            authors:
            - Island of TeX
            commands:
            - name: The echoer
              command: >
                @{ return getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'echo', 'never'); }
            arguments:
            - identifier: value
              flag: '@{ value + }'
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: brokenflag: { value: 'x' }"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        assertFailsWith<AraraException>(
            "a failing flag expression reports the runtime error"
        ) {
            Interpreter(
                ExecutionOptions(rulePaths = setOf(MPPPath(directory.toString()))),
                ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
        }
    }

    @Test
    fun `reports exit closure runtime errors`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-exitbroken.yaml"),
            """
            !config
            identifier: exitbroken
            name: ExitBroken
            authors:
            - Island of TeX
            commands:
            - name: The echoer
              command: >
                @{ return isWindows(getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'cmd', '/c', 'echo', 'ok'),
                getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'echo', 'ok')); }
              exit: 'value +'
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: exitbroken"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        assertFailsWith<AraraException>(
            "a broken exit closure reports the runtime error"
        ) {
            Interpreter(
                ExecutionOptions(rulePaths = setOf(MPPPath(directory.toString()))),
                ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
        }
    }

    @Test
    fun `rejects non boolean exit closures`() {
        Session.clear()
        val directory = Files.createTempDirectory("arara-interpreter-rule")
        Files.writeString(
            directory.resolve("arara-rule-exitwrong.yaml"),
            """
            !config
            identifier: exitwrong
            name: ExitWrong
            authors:
            - Island of TeX
            commands:
            - name: The echoer
              command: >
                @{ return isWindows(getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'cmd', '/c', 'echo', 'ok'),
                getCommandWithWorkingDirectory(
                '${forMvel(directory)}', 'echo', 'ok')); }
              exit: 'return "not boolean";'
            """.trimIndent()
        )
        val filePath = Files.createTempFile("arara-interpreter-file", ".tex")
        val directive = Directives.extractDirectives(
            listOf("% arara: exitwrong"), false, texFile
        ).single().run {
            DirectiveImpl(
                identifier,
                parameters.plus("reference" to filePath.toString()),
                conditional,
                lineNumbers
            )
        }
        assertFailsWith<AraraException>(
            "an exit closure has to evaluate to a boolean"
        ) {
            Interpreter(
                ExecutionOptions(rulePaths = setOf(MPPPath(directory.toString()))),
                ProjectFile(MPPPath(filePath.toString()), texFile),
                MPPPath(filePath.parent.toString())
            ).execute(directive)
        }
    }

    @Test
    fun `caps until loops at the configured maximum`() {
        assertEquals(
            3,
            loopTrace("until false", maxLoops = 3),
            "the until body runs exactly the configured maximum of times"
        )
    }

    @Test
    fun `runs if conditionals inside loops exactly once`() {
        assertEquals(
            1,
            loopTrace("if true", maxLoops = 10),
            "an if conditional runs its body exactly once inside the loop"
        )
    }

    @Test
    fun `runs unless conditionals inside loops exactly once`() {
        assertEquals(
            1,
            loopTrace("unless false", maxLoops = 10),
            "an unless conditional runs its body exactly once inside the loop"
        )
    }
}
