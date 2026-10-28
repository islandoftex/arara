// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.ui.ConsoleRenderer
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.localization.EnglishMessages
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.LinearExecutor

/** Runs argument processing and conditional execution through both rule parsers. */
class RuleExecutionRegressionTest {
    private class Fixture(val root: MPPPath, val engine: RuleEngine) {
        val entries = mutableListOf<String>()
        val results = mutableListOf<Boolean>()
        val messages = mutableListOf<String>()
        val input = root.resolve("run.tex").apply { writeText("% arara: probe\n") }
        val trace = root.resolve("trace.txt")
        val options = ExecutionOptions(rulePaths = setOf(root), haltOnErrors = false)

        fun rule(
            mvel: String,
            lua: String,
            arguments: Pair<String, String> = "" to "",
            exit: Pair<String, String>? = null
        ) {
            val yamlArguments = if (arguments.first.isBlank()) "arguments: []"
                else "arguments:\n" + arguments.first.trimIndent().prependIndent("  ")
            root.resolve("probe.yaml").writeText(
                "!config\nidentifier: probe\nname: Probe\ncommands:\n  - name: Task\n" +
                    "    command: >\n      @{ $mvel }\n" +
                    (exit?.let { "    exit: >\n      ${it.first}\n" } ?: "") + yamlArguments + "\n"
            )
            root.resolve("probe.lua").writeText(
                "return { identifier='probe', name='Probe', commands={{name='Task', " +
                    "command=function(ctx) $lua end" +
                    (exit?.let { ", exit=function(code) ${it.second} end" } ?: "") +
                    "}}, arguments={${arguments.second}} }\n"
            )
        }

        fun execute(
            parameters: Map<String, Any> = emptyMap(),
            conditional: DirectiveConditional = DirectiveConditional(),
            executionOptions: ExecutionOptions = options
        ): Int {
            LinearExecutor.executionOptions = executionOptions
            val directive = DirectiveImpl(
                "probe", parameters + ("reference" to input), conditional, listOf(1), engine
            )
            return Interpreter(executionOptions, ProjectFile(input, FileType("tex", "^%")), root, engine)
                .execute(directive).exitCode
        }

        fun nativeCommand(): Pair<String, String> {
            val name = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
            val executable = MPPPath(System.getProperty("java.home")).resolve("bin").resolve(name)
            fun quoted(path: MPPPath): String = path.toString().replace("\\", "\\\\").replace("'", "\\'")
            return "return getCommandWithWorkingDirectory('${quoted(root)}', '${quoted(executable)}', '-version');" to
                "return get_command_with_working_directory('${quoted(root)}', '${quoted(executable)}', '-version')"
        }

        fun recordBody(): Pair<String, String> {
            val path = trace.toString().replace("\\", "\\\\").replace("'", "\\'")
            return "writeToFile('$path', 'x', true); return true;" to
                "write_to_file('$path', 'x', true); return true"
        }
    }

    private fun eachEngine(test: (Fixture) -> Unit) {
        for (engine in RuleEngine.entries) {
            val root = Files.createTempDirectory("arara interpreter regression ").toMPPPath()
            val previousOptions = LinearExecutor.executionOptions
            val previousRenderer = DisplayUtils.renderer
            val previousMessages = LanguageController.messages
            val previousHalt = HaltRequest.exitCode
            try {
                val fixture = Fixture(root, engine)
                HaltRequest.clear()
                LanguageController.messages = EnglishMessages
                DisplayUtils.renderer = object : ConsoleRenderer by previousRenderer {
                    override fun printEntry(name: String, task: String) { fixture.entries.add(task) }
                    override fun printEntryResult(value: Boolean) { fixture.results.add(value) }
                    override fun printWrapped(text: String) { fixture.messages.add(text) }
                }
                test(fixture)
            } finally {
                LinearExecutor.executionOptions = previousOptions
                DisplayUtils.renderer = previousRenderer
                LanguageController.messages = previousMessages
                HaltRequest.clear()
                previousHalt?.let { HaltRequest.request(it) }
                root.removeDirectory()
            }
        }
    }

    @Test
    fun `unknown arguments fail before the command expression runs`() = eachEngine { fixture ->
        val (mvel, lua) = fixture.recordBody()
        fixture.rule(mvel, lua)
        val failure = assertFailsWith<AraraException> { fixture.execute(mapOf("bogus" to "x")) }
        assertEquals(EnglishMessages.errorInterpreterUnknownKeys("(bogus)"), failure.message)
        assertTrue(!fixture.trace.exists, fixture.engine.name)
    }

    @Test
    fun `required arguments cannot be satisfied by their defaults`() = eachEngine { fixture ->
        val (mvel, lua) = fixture.recordBody()
        fixture.rule(mvel, lua, """
            - identifier: marker
              required: true
              default: draft
        """ to "{identifier='marker', required=true, default='draft'}")
        val failure = assertFailsWith<AraraException> { fixture.execute() }
        assertEquals(EnglishMessages.errorInterpreterArgumentIsRequired("marker"), failure.message)
        assertTrue(!fixture.trace.exists, fixture.engine.name)
    }

    @Test
    fun `omitted optional arguments skip their processors and contribute no flags`() = eachEngine { fixture ->
        fixture.rule(
            "return marker.isEmpty();", "return #ctx.marker == 0", """
                - identifier: marker
                  flag: "@{ throw new RuntimeException('must not run'); }"
            """ to "{identifier='marker', flag=function(ctx, value) error('must not run') end}"
        )
        assertEquals(0, fixture.execute(), fixture.engine.name)
    }

    @Test
    fun `default arguments reach flag processors and the command context`() = eachEngine { fixture ->
        fixture.rule(
            "return marker.size() == 1 && marker[0].equals('--draft');",
            "return #ctx.marker == 1 and ctx.marker[1] == '--draft'", """
                - identifier: marker
                  default: draft
                  flag: "@{ return '--' + parameters.marker; }"
            """ to "{identifier='marker', default='draft', flag=function(ctx, value) return '--' .. value end}"
        )
        assertEquals(0, fixture.execute(), fixture.engine.name)
    }

    @Test
    fun `nested flag lists are flattened without losing their order`() = eachEngine { fixture ->
        fixture.rule(
            "return marker.size() == 2 && marker[0].equals('a') && marker[1].equals('b');",
            "return #ctx.marker == 2 and ctx.marker[1] == 'a' and ctx.marker[2] == 'b'", """
                - identifier: marker
                  flag: "@{ return [['a', null], [], 'b']; }"
            """ to "{identifier='marker', flag=function(ctx, value) return {{'a'}, {}, 'b'} end}"
        )
        assertEquals(0, fixture.execute(mapOf("marker" to "ignored")), fixture.engine.name)
    }

    @Test
    fun `numeric flag results retain their string conversion`() = eachEngine { fixture ->
        fixture.rule(
            "return marker.size() == 1 && marker[0].equals('42');",
            "return #ctx.marker == 1 and ctx.marker[1] == '42'", """
                - identifier: marker
                  flag: "@{ return 42; }"
            """ to "{identifier='marker', flag=function(ctx, value) return 42 end}"
        )
        assertEquals(0, fixture.execute(mapOf("marker" to "ignored")), fixture.engine.name)
    }

    @Test
    fun `literal defaults without flag closures contribute one argument`() = eachEngine { fixture ->
        fixture.rule(
            "return marker.size() == 1 && marker[0].equals('draft');",
            "return #ctx.marker == 1 and ctx.marker[1] == 'draft'", """
                - identifier: marker
                  default: draft
            """ to "{identifier='marker', default='draft'}"
        )
        assertEquals(0, fixture.execute(), fixture.engine.name)
    }

    @Test
    fun `flag failures keep their localized context`() = eachEngine { fixture ->
        fixture.rule("return true;", "return true", """
            - identifier: marker
              flag: "@{ throw new RuntimeException('flag sentinel'); }"
        """ to "{identifier='marker', flag=function(ctx, value) error('flag sentinel') end}")
        val failure = assertFailsWith<AraraException> { fixture.execute(mapOf("marker" to "x")) }
        assertTrue(EnglishMessages.errorInterpreterFlagRuntimeException in failure.message.orEmpty())
        assertNotNull(failure.exception, fixture.engine.name)
        assertTrue(fixture.entries.isEmpty(), fixture.engine.name)
    }

    @Test
    fun `command expression failures retain rule diagnostics and their cause`() = eachEngine { fixture ->
        fixture.rule("throw new RuntimeException('command sentinel');", "error('command sentinel')")
        val failure = assertFailsWith<AraraException> { fixture.execute() }
        assertTrue("probe" in failure.message.orEmpty() + failure.exception?.message.orEmpty(), fixture.engine.name)
        assertTrue(EnglishMessages.errorInterpreterCommandRuntimeError in failure.message.orEmpty())
        assertNotNull(failure.exception, fixture.engine.name)
    }

    @Test
    fun `unsupported command results are rejected rather than treated as successful`() = eachEngine { fixture ->
        fixture.rule("return 42;", "return 42")
        assertFailsWith<AraraException>(fixture.engine.name) { fixture.execute() }
        assertTrue(fixture.results.isEmpty(), fixture.engine.name)
    }

    @Test
    fun `exit closure failures retain their localized context and cause`() = eachEngine { fixture ->
        val (mvel, lua) = fixture.nativeCommand()
        fixture.rule(mvel, lua, exit =
            "throw new RuntimeException('exit sentinel');" to "error('exit sentinel')")
        val failure = assertFailsWith<AraraException> { fixture.execute() }
        assertTrue(EnglishMessages.errorInterpreterExitRuntimeError in failure.message.orEmpty())
        assertNotNull(failure.exception, fixture.engine.name)
        assertTrue(fixture.results.isEmpty(), fixture.engine.name)
    }

    @Test
    fun `exit closures must return booleans rather than truthy values`() = eachEngine { fixture ->
        val (mvel, lua) = fixture.nativeCommand()
        fixture.rule(mvel, lua, exit = "return 'not boolean';" to "return 'not boolean'")
        val failure = assertFailsWith<AraraException> { fixture.execute() }
        assertTrue(EnglishMessages.errorInterpreterWrongExitClosureReturn in failure.message.orEmpty())
        assertTrue(fixture.results.isEmpty(), fixture.engine.name)
    }

    @Test
    fun `each task in a nested command list receives its own result`() = eachEngine { fixture ->
        fixture.rule("return [true, [false, true]];", "return {true, {false, true}}")
        assertEquals(1, fixture.execute(), fixture.engine.name)
        assertEquals(listOf("Task", "Task", "Task"), fixture.entries, fixture.engine.name)
        assertEquals(listOf(true, false, true), fixture.results, fixture.engine.name)
    }

    @Test
    fun `Lua command labels override task names without changing fallback labels`() = eachEngine { fixture ->
        val (mvel, lua) = fixture.nativeCommand()
        val expression = lua.removePrefix("return ")
        fixture.rule(mvel, "return {named_command('Custom', $expression), " +
            "named_command(' ', $expression), $expression, true}")
        assertEquals(0, fixture.execute())
        assertEquals(if (fixture.engine == RuleEngine.LUA) listOf("Custom", "Task", "Task", "Task")
            else listOf("Task"), fixture.entries)
    }

    @Test
    fun `loop caps and one shot conditionals control the number of executions`() = eachEngine { fixture ->
        val (mvel, lua) = fixture.recordBody()
        fixture.rule(mvel, lua)
        val cases = listOf(
            Triple(DirectiveConditionalType.WHILE, "true", 2),
            Triple(DirectiveConditionalType.UNTIL, "false", 2),
            Triple(DirectiveConditionalType.IF, "true", 1),
            Triple(DirectiveConditionalType.UNLESS, "false", 1),
            Triple(DirectiveConditionalType.WHILE, "false", 0),
            Triple(DirectiveConditionalType.IF, "false", 0),
            Triple(DirectiveConditionalType.UNLESS, "true", 0),
            Triple(DirectiveConditionalType.UNTIL, "true", 1)
        )
        for ((type, condition, expected) in cases) {
            fixture.trace.writeText("")
            assertEquals(0, fixture.execute(conditional = DirectiveConditional(type, condition),
                executionOptions = fixture.options.copy(maxLoops = 2)))
            assertEquals(expected, fixture.trace.readText().length, "${fixture.engine}: $type $condition")
        }
    }

    @Test
    fun `dry run displays boolean tasks without evaluating their conditionals`() = eachEngine { fixture ->
        fixture.rule("return true;", "return true")
        assertEquals(0, fixture.execute(
            conditional = DirectiveConditional(DirectiveConditionalType.WHILE, "invalid expression ???"),
            executionOptions = fixture.options.copy(executionMode = ExecutionMode.DRY_RUN)
        ))
        assertEquals(listOf(EnglishMessages.infoInterpreterDryRunModeBooleanMode(true)), fixture.messages)
        assertEquals(listOf(true), fixture.results)
    }
}
