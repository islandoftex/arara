// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.logging.log4j.LogManager
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration
import org.islandoftex.arara.mvel.configuration.YamlLocalConfiguration

class JsonLoggingTest {
    @Test
    fun bothConfigurationFormatsValidateLevelsAndRetentionAndPreserveInheritedSettings() {
        val root = Files.createTempDirectory("arara log policies ").toMPPPath()
        try {
            val lua = root.resolve("config.lua")
            val yaml = root.resolve("config.yaml")
            val base = LoggingOptions(LogFormat.JSON, true, root.resolve("inherited.log"),
                LogLevel.DEBUG, LogRetention.ON_FAILURE)
            assertEquals(base, LocalConfiguration().toLoggingOptions(base))
            for (level in LogLevel.entries) {
                for ((retention, name) in listOf(LogRetention.ALWAYS to "always", LogRetention.ON_FAILURE to "on-failure")) {
                    lua.writeText("return {log={level='${level.name.lowercase()}',retention='$name'}}")
                    yaml.writeText("!config\nloglevel: ${level.name.lowercase()}\nlogretention: $name\n")
                    val expected = base.copy(level = level, retention = retention)
                    assertEquals(expected, LuaLocalConfiguration.load(lua, root).toLoggingOptions(base))
                    assertEquals(expected, YamlLocalConfiguration.load(yaml).toLoggingOptions(base))
                }
            }
            for ((field, value) in listOf("level" to "verbose", "retention" to "sometimes")) {
                lua.writeText("return {log={$field='$value'}}")
                yaml.writeText("!config\nlog$field: $value\n")
                assertFailsWith<AraraException> { LuaLocalConfiguration.load(lua, root).toLoggingOptions() }
                assertFailsWith<AraraException> { YamlLocalConfiguration.load(yaml).toLoggingOptions() }
            }
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun jsonLinesEscapeMessagesAndExceptionsAndAppendWithoutTruncation() {
        val root = Files.createTempDirectory("arara json log ").toMPPPath()
        try {
            LoggingUtils.init()
            val file = root.resolve("events.jsonl")
            val options = LoggingOptions(LogFormat.JSON, false, file)
            LoggingUtils.setupLogging(options)
            val logger = LogManager.getLogger("json-test")
            val message = "quotes \\\" and slash \\\\ and line\nUnicode ação 目录\tend"
            logger.info(message)
            LoggingUtils.setupLogging(options)
            logger.error("failure", IllegalStateException("cause\nwith quotes \""))
            LoggingUtils.init()
            LoggingUtils.setupLogging(options.copy(appendLog = true))
            logger.info("third")
            LoggingUtils.init()
            val records = file.readLines().map { Json.parseToJsonElement(it).jsonObject }
            assertEquals(3, records.size)
            assertEquals(message, records[0].getValue("message").jsonPrimitive.content)
            assertEquals("INFO", records[0].getValue("level").jsonPrimitive.content)
            assertEquals("json-test", records[0].getValue("logger").jsonPrimitive.content)
            assertTrue(records[0].getValue("timestamp").jsonPrimitive.content.endsWith("Z"))
            assertTrue("IllegalStateException" in records[1].getValue("exception").jsonPrimitive.content)
            assertTrue("cause\nwith quotes" in records[1].getValue("exception").jsonPrimitive.content)
            assertEquals("third", records[2].getValue("message").jsonPrimitive.content)
            LoggingUtils.setupLogging(options.copy(format = LogFormat.NONE))
            logger.info("must not be logged")
            assertEquals(3, file.readLines().size)
            LoggingUtils.setupLogging(options.copy(appendLog = true))
            logger.info("before format change")
            LoggingUtils.setupLogging(options.copy(format = LogFormat.TEXT))
            logger.info("plain text")
            LoggingUtils.init()
            assertTrue("plain text" in file.readText())
            assertEquals(1, file.readLines().size)
        } finally {
            LoggingUtils.init()
            root.removeDirectory()
        }
    }

    @Test
    fun bothConfigurationFormatsSelectEachModeAndRejectUnknownFormats() {
        val root = Files.createTempDirectory("arara json configuration ").toMPPPath()
        try {
            val base = LoggingOptions(LogFormat.JSON)
            for (format in LogFormat.entries) {
                val name = format.name.lowercase()
                val yaml = root.resolve("arararc.yaml").apply { writeText("!config\nlogformat: $name\n") }
                val lua = root.resolve("arararc.lua").apply { writeText("return { log = { format = '$name' } }") }
                assertEquals(format, YamlLocalConfiguration.load(yaml).toLoggingOptions(base).format)
                assertEquals(format, LuaLocalConfiguration.load(lua, root).toLoggingOptions(base).format)
            }
            assertEquals(LogFormat.NONE, LocalConfiguration().toLoggingOptions().format)
            assertEquals(base, LocalConfiguration().toLoggingOptions(base))
            assertFailsWith<AraraException> { LocalConfiguration(logformat = "invalid").toLoggingOptions() }
        } finally {
            root.removeDirectory()
        }
    }
}
