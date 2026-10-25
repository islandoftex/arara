// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot

class ControlFlowLoggingTest {
    private fun fixture(block: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara control flow logs ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            root.resolve("arararc.lua").writeText("return { paths={cwd}, " +
                "log={format='json', file=cwd .. '/events.jsonl'} }")
            block(root)
        } finally {
            LoggingUtils.init()
            snapshot.restore()
            root.removeDirectory()
        }
    }

    private fun rule(root: MPPPath, engine: String, code: Int? = null, argument: Boolean = false) {
        val body = if (code == null || argument) "return true" else "halt($code); return true"
        root.resolve("probe.$engine").writeText(if (engine == "lua") {
            val arguments = if (argument) "{{identifier='trigger', flag=function() halt($code); return '' end}}" else "{}"
            "return {identifier='probe', name='Probe', commands={{command=function() $body end}}, arguments=$arguments}"
        } else {
            val arguments = if (argument) "\n  - identifier: trigger\n    flag: \"@{ halt($code); return ''; }\"" else " []"
            "!config\nidentifier: probe\nname: Probe\ncommands:\n  - command: '@{ $body; }'\narguments:$arguments\n"
        })
    }

    private fun run(root: MPPPath, engine: String, flags: List<String> = emptyList()): Int =
        assertFailsWith<ProgramResult> {
            CLI().parse(listOf("--engine", engine, "--language", "fr", "-d", root.toString()) + flags + "run.tex")
        }.statusCode

    @Test
    fun skipsAreLoggedOnlyWhenConditionsAreActuallyEvaluated() = fixture { root ->
        for (engine in listOf("lua", "yaml")) {
            rule(root, engine)
            root.resolve("run.tex").writeText("% arara: probe if false\n")
            assertEquals(0, run(root, engine))
            val events = root.resolve("events.jsonl").readLines().map { Json.parseToJsonElement(it).jsonObject }
            val skipped = events.single { it["event"]?.jsonPrimitive?.content == "directive.skipped" }
            assertTrue("skipped as its condition" in skipped.getValue("message").jsonPrimitive.content)
            val data = skipped.getValue("data").jsonObject
            assertEquals("false", data.getValue("condition").jsonPrimitive.content)
            assertEquals("if", data.getValue("conditionalType").jsonPrimitive.content)
            assertEquals(root.resolve("run.tex").toString(), data.getValue("inputFile").jsonPrimitive.content)
            assertEquals(engine, data.getValue("engine").jsonPrimitive.content)
            assertEquals(0, run(root, engine, listOf("--dry-run")))
            assertTrue("directive.skipped" !in root.resolve("events.jsonl").readText())
        }
    }

    @Test
    fun commandAndArgumentHaltsAreRecordedOnceWithTheirExactStatus() = fixture { root ->
        for (engine in listOf("lua", "yaml")) {
            for (argument in listOf(false, true)) {
                for (code in listOf(0, 7)) {
                    rule(root, engine, code, argument)
                    val parameters = if (argument) ": { trigger: true }" else ""
                    root.resolve("run.tex").writeText("% arara: probe$parameters\n% arara: missing\n")
                    assertEquals(code, run(root, engine))
                    val events = root.resolve("events.jsonl").readLines().map { Json.parseToJsonElement(it).jsonObject }
                    val halted = events.single { it["event"]?.jsonPrimitive?.content == "halt.requested" }
                    val data = halted.getValue("data").jsonObject
                    assertEquals(code, data.getValue("exitCode").jsonPrimitive.int)
                    assertEquals("probe", data.getValue("rule").jsonPrimitive.content)
                    assertTrue(events.none { it["event"]?.jsonPrimitive?.content == "run.exception" })
                }
            }
        }
    }
}
