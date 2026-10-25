// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

class RuntimeDiagnosticsTest {
    @Test
    fun executedProjectsRecordEffectiveSettingsWithoutLeakingConfigurationNames() {
        val root = Files.createTempDirectory("arara runtime context ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        val home = System.getProperty("user.home")
        try {
            System.setProperty("user.home", root.toString())
            val log = root.resolve("events.jsonl")
            Session.loggingOptions = LoggingOptions(LogFormat.JSON, false, log)
            LinearExecutor.executionOptions = ExecutionOptions(rulePaths = setOf(root))
            for (name in listOf("first", "second")) {
                root.resolve(name).createDirectories()
                root.resolve("$name/run.tex").writeText("% arara: probe\n")
            }
            val configuration = root.resolve("first/arararc.lua")
            configuration.writeText("return { console = 'styled' }")
            root.resolve("probe.lua").writeText("return { identifier='probe', name='Probe', " +
                "commands={{ command=function() return true end }} }")
            root.resolve("projects.proj.lua").writeText("""
                return {
                  { name='first', working_directory='first', files={ ['run.tex']={} } },
                  { name='second', working_directory='second', files={ ['run.tex']={} } }
                }
            """.trimIndent())
            assertEquals(0, assertFailsWith<ProgramResult> {
                CLI().parse(listOf("-d", root.toString(), "--console", "plain", "projects.proj.lua"))
            }.statusCode)
            LoggingUtils.init()
            val events = log.readLines().map { Json.parseToJsonElement(it).jsonObject }
                .filter { it["event"]?.jsonPrimitive?.content == "run.context" }
            assertEquals(2, events.size)
            val contexts = events.associate { event ->
                val data = event.getValue("data").jsonObject
                data.getValue("project").jsonPrimitive.content to data
            }
            assertEquals(configuration.toString(), contexts.getValue("first")
                .getValue("configurationFile").jsonPrimitive.content)
            assertFalse("configurationFile" in contexts.getValue("second"))
            for ((name, data) in contexts) {
                assertEquals(root.resolve(name).toString(), data.getValue("workingDirectory").jsonPrimitive.content)
                assertEquals("lua", data.getValue("engine").jsonPrimitive.content)
                assertEquals("plain", data.getValue("console").jsonPrimitive.content)
                assertEquals(System.getProperty("java.version"), data.getValue("javaVersion").jsonPrimitive.content)
            }
            val text = root.resolve("events.log")
            LoggingUtils.setupLogging(LoggingOptions(LogFormat.TEXT, false, text))
            RuntimeDiagnostics.record(Project("text", root, emptySet()), "yaml", "styled", null)
            LoggingUtils.init()
            assertTrue("engine=yaml" in text.readText())
            assertTrue("console=styled" in text.readText())
        } finally {
            if (home == null) System.clearProperty("user.home") else System.setProperty("user.home", home)
            snapshot.restore()
            root.removeDirectory()
        }
    }
}
