// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.CommandEvaluator
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.Command
import org.islandoftex.arara.core.session.HaltRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CommandAdapterTest {
    private class Adapter(
        private val evaluateBody: (Map<String, Any>) -> Any?,
        private val exitCheck: (Int) -> Boolean = { it == 0 }
    ) : RuleCommand, CommandEvaluator {
        override val name = "Adapter"
        var directCalls = 0
        override val command: () -> Int = { directCalls++; 0 }
        override fun evaluate(parameters: Map<String, Any>): Any? = evaluateBody(parameters)
        override fun isSuccessful(exitCode: Int): Boolean = exitCheck(exitCode)
    }

    @BeforeTest
    fun setup() = HaltRequest.clear()

    @AfterTest
    fun restore() = HaltRequest.clear()

    private fun execute(
        adapter: Adapter,
        mode: ExecutionMode = ExecutionMode.NORMAL_RUN,
        arguments: List<RuleArgument<*>> = emptyList()
    ): Int {
        val path = MPPPath("run.tex")
        val rule = object : Rule {
            override val identifier = "probe"
            override val displayName = "Probe"
            override val authors = emptyList<String>()
            override val commands = listOf(adapter)
            override val arguments = arguments
        }
        val directive = DirectiveImpl(
            "probe", mapOf("reference" to path), DirectiveConditional(), listOf(1)
        )
        return Interpreter(
            ExecutionOptions(executionMode = mode, haltOnErrors = false),
            ProjectFile(path, FileType("tex", "^%")), path.parent
        ).executeRule(rule, directive, MPPPath("probe.rule")).exitCode
    }

    @Test
    fun engineNeutralResultsKeepFailuresAcrossNestedCommandLists() {
        val adapter = Adapter({ listOf(false, listOf(true)) })
        assertEquals(1, execute(adapter))
        assertEquals(0, adapter.directCalls)
    }

    @Test
    fun evaluatorsReceiveResolvedArgumentsWithoutMvelBindings() {
        var received = emptyMap<String, Any>()
        val argument = object : RuleArgument<String> {
            override val identifier = "mode"
            override val isRequired = false
            override val defaultValue = "draft"
            override val processor: (String, Map<String, Any>) -> List<String> = { value, _ -> listOf(value) }
        }
        val adapter = Adapter({ received = it; true })
        assertEquals(0, execute(adapter, arguments = listOf(argument)))
        assertEquals(setOf("reference", "mode"), received.keys)
        assertEquals(listOf("draft"), received["mode"])
    }

    @Test
    fun dryRunDoesNotLaunchCommandsOrEvaluateTheirExitPredicates() {
        var checked = false
        val adapter = Adapter(
            { Command(listOf("missing-arara-adapter-executable")) },
            { checked = true; false }
        )
        assertEquals(0, execute(adapter, ExecutionMode.DRY_RUN))
        assertEquals(false, checked)
    }

    @Test
    fun adaptersCanAcceptANonzeroProcessExitCode() {
        val name = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        val java = MPPPath(System.getProperty("java.home")).resolve("bin").resolve(name)
        val checked = mutableListOf<Int>()
        val adapter = Adapter(
            { Command(listOf(java.toString(), "--arara-invalid-option")) },
            { checked.add(it); it == 1 }
        )
        assertEquals(0, execute(adapter))
        assertEquals(listOf(1), checked)
    }

    @Test
    fun evaluatorErrorsReceiveTheRuleContext() {
        val cause = AraraExceptionWithHeader("adapter sentinel")
        val error = assertFailsWith<AraraException> { execute(Adapter({ throw cause })) }
        assertTrue(error.message.orEmpty().contains("probe"))
        assertTrue(error.message.orEmpty().contains("adapter sentinel"))
        assertSame(cause, error.exception)
    }
}
