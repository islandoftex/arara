// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.session.Session

class SessionLogTest {
    @Test
    fun mutationsAreRecordedWithoutValuesAndReadsStaySilent() {
        val snapshot = ConfigurationSnapshot.capture()
        val root = Files.createTempDirectory("arara session log ").toMPPPath()
        try {
            LoggingUtils.init()
            Session.clear()
            val log = root.resolve("session.log")
            LoggingUtils.setupLogging(LoggingOptions(LogFormat.TEXT, false, log))
            val opaque = object {
                override fun toString(): String = error("Session tracing must not inspect values")
            }
            Session.put("counter", opaque)
            assertSame(opaque, Session["counter"])
            assertTrue("counter" in Session)
            Session.put("counter", "SESSION_VALUE_MUST_NOT_APPEAR")
            Session.remove("counter")
            assertFailsWith<AraraException> { Session.remove("missing") }
            Session.put("environment:sentinel", "ENVIRONMENT_VALUE_MUST_NOT_APPEAR")
            Session.updateEnvironmentVariables(additionFilter = { false })
            Session.put("remaining", 1)
            Session.clear()
            Session.clear()
            LoggingUtils.init()
            val text = log.readText()
            assertTrue("Session variable 'counter' set." in text)
            assertTrue("Session variable 'counter' overwritten." in text)
            assertTrue("Session variable 'counter' removed." in text)
            assertTrue("Session variable 'environment:sentinel' removed." in text)
            assertTrue("Session environment refreshed (0 variables)." in text)
            assertTrue("Session cleared (1 variable)." in text)
            assertTrue("Session cleared (0 variables)." in text)
            assertFalse("Session variable 'missing' removed." in text)
            assertFalse("SESSION_VALUE_MUST_NOT_APPEAR" in text)
            assertFalse("ENVIRONMENT_VALUE_MUST_NOT_APPEAR" in text)
            assertEquals(3, Regex("Session variable 'counter'").findAll(text).count())
        } finally {
            LoggingUtils.init()
            Session.clear()
            root.removeDirectory()
            snapshot.restore()
        }
    }

    @Test
    fun disablingThenReenablingLoggingKeepsTheSessionLoggerWorking() {
        val snapshot = ConfigurationSnapshot.capture()
        val root = Files.createTempDirectory("arara resumed session log ").toMPPPath()
        try {
            LoggingUtils.init()
            Session.clear()
            val first = root.resolve("first.log")
            val second = root.resolve("second.log")
            LoggingUtils.setupLogging(LoggingOptions(LogFormat.TEXT, false, first))
            Session.put("first", 1)
            LoggingUtils.init()
            Session.put("disabled", 2)
            LoggingUtils.setupLogging(LoggingOptions(LogFormat.TEXT, false, second))
            Session.put("resumed", 3)
            LoggingUtils.init()
            assertTrue("Session variable 'first' set." in first.readText())
            assertFalse("'disabled'" in first.readText())
            assertTrue("Session variable 'resumed' set." in second.readText())
        } finally {
            LoggingUtils.init()
            Session.clear()
            root.removeDirectory()
            snapshot.restore()
        }
    }
}
