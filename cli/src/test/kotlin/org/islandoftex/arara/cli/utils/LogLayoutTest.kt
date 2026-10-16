// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.DirectiveConditional

class LogLayoutTest {
    private fun fixture(block: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara log layout ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            LoggingUtils.init()
            org.islandoftex.arara.core.session.Session.userInterfaceOptions = UserInterfaceOptions()
            block(root)
        } finally {
            LoggingUtils.init()
            snapshot.restore()
            root.removeDirectory()
        }
    }

    @Test
    fun multilineDirectiveListsHaveNoTemplateIndentationInEitherLogFormat() = fixture { root ->
        val file = ProjectFile(root.resolve("run.tex"), FileType("tex", "^%"))
        val directives = listOf("first", "second").map {
            DirectiveImpl(it, emptyMap(), DirectiveConditional(), listOf(1))
        }
        for (format in listOf(LogFormat.TEXT, LogFormat.JSON)) {
            val destination = root.resolve("directives.$format")
            LoggingUtils.setupLogging(LoggingOptions(format = format, logFile = destination))
            val processed = DirectiveUtils.process(file, directives)
            LoggingUtils.init()
            val expected = "All directives were validated.\n" + RunLog.outputSeparator("DIRECTIVES") + "\n" +
                processed.joinToString("\n") + "\n" + RunLog.separator()
            if (format == LogFormat.JSON) {
                val record = Json.parseToJsonElement(destination.readLines().single()).jsonObject
                assertEquals(expected, record.getValue("message").jsonPrimitive.content)
            } else {
                assertTrue(expected in destination.readText())
            }
        }
    }

    @Test
    fun outputFramesStayFlushLeftWithoutChangingToolWhitespace() = fixture { root ->
        val outputs = listOf("first\nsecond", "  first  \n\tsecond \n", "\nfirst\r\nsecond\r\n")
        for (format in listOf(LogFormat.TEXT, LogFormat.JSON)) {
            for ((index, output) in outputs.withIndex()) {
                val destination = root.resolve("output-$format-$index.log")
                LoggingUtils.setupLogging(LoggingOptions(format = format, logFile = destination))
                RunLog.commandOutput(output)
                LoggingUtils.init()
                val expected = RunLog.outputSeparator("BEGIN OUTPUT BUFFER") + "\n" + output +
                    (if (output.endsWith('\n')) "" else "\n") + RunLog.outputSeparator("END OUTPUT BUFFER")
                if (format == LogFormat.JSON) {
                    val record = Json.parseToJsonElement(destination.readLines().single()).jsonObject
                    assertEquals(expected, record.getValue("message").jsonPrimitive.content)
                    assertEquals(output, record.getValue("data").jsonObject.getValue("output").jsonPrimitive.content)
                } else {
                    assertTrue(expected in destination.readText())
                }
            }
        }
    }
}
