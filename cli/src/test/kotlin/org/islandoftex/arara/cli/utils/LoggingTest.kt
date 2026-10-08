// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.core.configuration.LoggingOptions

/**
 * Verifies the logging setup [Logging] performs: the log file path
 * registers in the logging context, and the setup is undone
 * afterwards so the context values cannot leak into later tests
 * of the same machine.
 */
class LoggingTest {
    @Test
    fun `properly sets the log file path`() {
        val defaultEnable = LoggingOptions(true)
        assertNull(
            ThreadContext.get("araraLogFile"),
            "no log file path is registered before logging is set up"
        )
        try {
            Logging.setupLogging(defaultEnable)
            assertEquals(
                defaultEnable.logFile.normalize().toString(),
                ThreadContext.get("araraLogFile"),
                "log file path registered for the logging context"
            )
        } finally {
            // the context values would leak into every later test of
            // the same virtual machine, so the setup is undone
            Logging.setupLogging(LoggingOptions())
        }
    }
}
