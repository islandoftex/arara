// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.styled.ProgressTestTerminal
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.BuildWorkspaceEvent
import org.islandoftex.arara.core.files.BuildWorkspaceOperation
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session

class BuildWorkspaceFeedbackTest {

    @Test
    fun consoleIsLocalizedWhileLogsKeepTypedMetricsAndEnglishLabels() {
        val snapshot = ConfigurationSnapshot.capture()
        val root = Files.createTempDirectory("arara-workspace-feedback").toMPPPath()
        val previousRenderer = DisplayUtils.renderer
        val sink = ProgressTestTerminal(interactive = false)
        val renderer = StyledConsoleRenderer(sink.terminal)
        try {
            DisplayUtils.renderer = renderer
            val locale = MPPLocale("de")
            Session.userInterfaceOptions = UserInterfaceOptions(locale)
            LanguageController.loadMessagesFor(locale)
            val project = Project("book", root, emptySet(), buildDirectory = BuildDirectory(root.resolve("build")))
            for (format in listOf(LogFormat.TEXT, LogFormat.JSON)) {
                val log = root.resolve("$format.log")
                LoggingUtils.setupLogging(LoggingOptions(format, false, log))
                for (operation in BuildWorkspaceOperation.entries) {
                    reportBuildWorkspace(BuildWorkspaceEvent.Started(project, operation))
                    reportBuildWorkspace(BuildWorkspaceEvent.Finished(project, operation, true, 1234.milliseconds, 2))
                }
                reportBuildWorkspace(BuildWorkspaceEvent.Finished(project, BuildWorkspaceOperation.PREPARE,
                    false, 50.milliseconds, 1))
                LoggingUtils.init()
                val text = log.readText()
                assertTrue("Build directory prepare completed" in text)
                assertTrue("copied 2 input files in 1234 ms" in text)
                if (format == LogFormat.JSON) {
                    val events = log.readLines().map { Json.parseToJsonElement(it).jsonObject }
                    val prepared = events.single { it["event"]?.jsonPrimitive?.content == "build_directory.completed" &&
                        it.getValue("data").jsonObject["operation"]?.jsonPrimitive?.content == "prepare" }
                    val data = prepared.getValue("data").jsonObject
                    assertEquals(1234L, data.getValue("durationMs").jsonPrimitive.long)
                    assertEquals(2, data.getValue("copiedFiles").jsonPrimitive.int)
                    assertTrue(data.getValue("success").jsonPrimitive.boolean)
                    assertEquals(project.executionDirectory.toString(), data.getValue("directory").jsonPrimitive.content)
                    val failed = events.last().getValue("data").jsonObject
                    assertFalse(failed.getValue("success").jsonPrimitive.boolean)
                    assertEquals(1, failed.getValue("copiedFiles").jsonPrimitive.int)
                }
            }
            assertTrue("Build-Verzeichnis wird geprüft" in sink.output())
            assertTrue("Build-Verzeichnis bereit: 2 Eingabedateien in 1,234 s kopiert." in sink.output())
            assertTrue("Build-Verzeichnis wird bereinigt" in sink.output())
        } finally {
            LoggingUtils.init()
            DisplayUtils.renderer = previousRenderer
            renderer.close()
            snapshot.restore()
            root.removeDirectory()
        }
    }
}
