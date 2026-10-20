// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.session.Command
import org.islandoftex.arara.core.session.Session
import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.session.Command as APICommand

class StructuredLoggingTest {
    @Test
    fun eventFieldsKeepTheirTypesAndLiteralValues() {
        val root = Files.createTempDirectory("arara typed events ").toMPPPath()
        val previous = LoggingUtils.beginRun()
        try {
            val file = root.resolve("events.jsonl")
            LoggingUtils.setupLogging(LoggingOptions(LogFormat.JSON, false, file))
            val argv = listOf("tool", "quote \" and slash \\ and newline\n目录")
            val path = root.toString() + "/rule\"with\nnewline.lua"
            val duration = 9876543210L
            RunLog.rule("probe", path, "lua")
            RunLog.commandCompleted(Command(argv, root), 7, duration)
            RunLog.commandFailed(Command(argv, root), "IO", duration, IOException("literal cause\n\""))
            RunLog.taskResult(true)
            RunLog.executionOutcome(7)
            LoggingUtils.init()
            val records = file.readLines().map { Json.parseToJsonElement(it).jsonObject }
            assertEquals(5, records.size)
            assertTrue(records.all { it.getValue("schemaVersion").jsonPrimitive.int == 1 })
            assertEquals(1, records.map { it.getValue("runId").jsonPrimitive.content }.distinct().size)
            assertEquals(path, records[0].getValue("data").jsonObject.getValue("path").jsonPrimitive.content)
            val completed = records.single { it.getValue("event").jsonPrimitive.content == "command.completed" }
            val data = completed.getValue("data").jsonObject
            assertEquals(7, data.getValue("exitCode").jsonPrimitive.int)
            assertFalse(data.getValue("exitCode").jsonPrimitive.isString)
            assertEquals(duration, data.getValue("durationMs").jsonPrimitive.long)
            assertEquals(argv, data.getValue("argv").jsonArray.map { it.jsonPrimitive.content })
            assertEquals("System command exited with value 7 after $duration ms",
                completed.getValue("message").jsonPrimitive.content)
            val failed = records.single { it.getValue("event").jsonPrimitive.content == "command.failed" }
            assertFalse("exitCode" in failed.getValue("data").jsonObject)
            assertTrue("IOException" in failed.getValue("exception").jsonPrimitive.content)
            val task = records.single { it.getValue("event").jsonPrimitive.content == "task.result" }
            assertTrue(task.getValue("data").jsonObject.getValue("success").jsonPrimitive.boolean)
            val outcome = records.last().getValue("data").jsonObject
            assertEquals(7, outcome.getValue("exitCode").jsonPrimitive.int)
            assertFalse(outcome.getValue("success").jsonPrimitive.boolean)
        } finally {
            LoggingUtils.init()
            LoggingUtils.restoreRun(previous)
            root.removeDirectory()
        }
    }

    @Test
    fun disabledLoggingDoesNotReadCommandArgumentsOrWorkingDirectories() {
        LoggingUtils.init()
        val command = object : APICommand {
            override val elements: List<String> get() = error("Arguments must not be read")
            override val workingDirectory: MPPPath?
                get() = error("Working directory must not be read")
        }
        RunLog.commandCompleted(command, 0, 1L)
        RunLog.commandFailed(command, "IO", 1L, IOException("not logged"))
    }

    @Test
    fun cliUsesANewRunIdAndRestoresTheEmbeddingHostContext() {
        val root = Files.createTempDirectory("arara run identifier ").toMPPPath()
        val previous = ThreadContext.get("araraRunId")
        ThreadContext.put("araraRunId", "host-run")
        try {
            root.resolve("arararc.lua").writeText(
                "return { paths = { cwd }, log = { format = 'json', " +
                    "file = cwd .. '/events.jsonl' } }")
            root.resolve("run.tex").writeText("% arara: probe\n")
            root.resolve("probe.lua").writeText("return { identifier='probe', name='Probe', " +
                "commands={{ command=function(ctx) return true end }} }")
            val ids = (1..2).map {
                assertEquals(0, assertFailsWith<ProgramResult> {
                    CLI().parse(listOf("--engine", "lua", "-d", root.toString(), "run.tex"))
                }.statusCode)
                assertEquals("host-run", ThreadContext.get("araraRunId"))
                val events = root.resolve("events.jsonl").readLines().map { Json.parseToJsonElement(it).jsonObject }
                events.single { it["event"]?.jsonPrimitive?.content == "run.completed" }
                    .getValue("runId").jsonPrimitive.content
            }
            assertEquals(2, ids.distinct().size)
            assertTrue(ids.none { it == "host-run" })
        } finally {
            if (previous == null) ThreadContext.remove("araraRunId") else ThreadContext.put("araraRunId", previous)
            LoggingUtils.init()
            Session.clear()
            root.removeDirectory()
        }
    }
}
