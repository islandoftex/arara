// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.core.configuration.LoggingOptions

class LoggingUtilsTest : ShouldSpec({
    should("properly set log file path") {
        val options = LoggingOptions(LogFormat.TEXT)
        ThreadContext.get("araraLogFile") shouldBe null
        LoggingUtils.setupLogging(options)
        ThreadContext.get("araraLogFile") shouldBe options.logFile
            .normalize().toString()
    }
})
