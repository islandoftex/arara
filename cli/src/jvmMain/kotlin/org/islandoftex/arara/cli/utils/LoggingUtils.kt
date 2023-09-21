// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.configuration.LoggingOptions

/**
 * Implements the logging controller. This class actually sets the logging
 * configuration in order to allow appending results to a file.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object LoggingUtils {
    /**
     * Sets the logging configuration according to the provided boolean value.
     * If the value is set to true, the log entries will be appended to a file,
     * otherwise the logging feature will keep silent.
     *
     * @param loggingOptions The configuration of the logging behavior.
     */
    fun setupLogging(loggingOptions: LoggingOptions) {
        if (loggingOptions.enableLogging) {
            // TODO: check for multi-threading
            ThreadContext.put(
                "araraLogFile",
                loggingOptions.logFile.normalize().toString(),
            )
            val loggerContext = LogManager.getContext(false)
                as org.apache.logging.log4j.core.LoggerContext
            loggerContext.configLocation = LoggingUtils::class.java
                .getResource("/org/islandoftex/arara/cli/configuration/log4j2.xml")
                .toURI()
            loggerContext.reconfigure()
        } else {
            Configurator.setRootLevel(Level.OFF)
        }
    }

    /**
     * Initializes the logging controller by disabling it. I don't want an odd
     * behaviour out of the box.
     */
    fun init() {
        setupLogging(org.islandoftex.arara.core.configuration.LoggingOptions())
    }
}
