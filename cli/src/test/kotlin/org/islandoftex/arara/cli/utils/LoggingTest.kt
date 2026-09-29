// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.core.configuration.LoggingOptions

/**
 * Verifies the log4j2 setup performed by [Logging].
 */
class LoggingTest {
    @Test
    fun `properly sets the log file path`() {
        val defaultEnable = LoggingOptions(true)
        assertNull(
            ThreadContext.get("araraLogFile"),
            "no log file path is registered before logging is set up"
        )
        Logging.setupLogging(defaultEnable)
        assertEquals(
            defaultEnable.logFile.normalize().toString(),
            ThreadContext.get("araraLogFile"),
            "log file path registered for the logging context"
        )
    }
}
