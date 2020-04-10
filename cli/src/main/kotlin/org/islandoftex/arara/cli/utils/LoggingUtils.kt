// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import java.io.InputStream
import org.islandoftex.arara.Arara
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.slf4j.LoggerFactory

/**
 * Implements the logging controller. This class actually sets the logging
 * configuration in order to allow appending results to a file.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object LoggingUtils {
    // configuration resource as an input stream
    // the configuration is actually a XML file.
    private val resource: InputStream by lazy {
        LoggingUtils::class.java
                .getResourceAsStream("/org/islandoftex/arara/cli/configuration/logback.xml")
    }

    /**
     * Sets the logging configuration according to the provided boolean value.
     * If the value is set to true, the log entries will be appended to a file,
     * otherwise the logging feature will keep silent.
     * @param enable A boolean value that indicates the logging behaviour
     * throughout the application.
     */
    fun enableLogging(enable: Boolean) {
        // get the logger context from a factory, set a
        // new context and reset it
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        try {
            // get a new configuration and set
            // the context
            val configurator = JoranConfigurator()
            configurator.context = loggerContext
            loggerContext.reset()

            // if enabled, the log entries will be
            // appended to a file, otherwise it will
            // remain silent
            if (enable) {
                // set the file name and configure
                // the logging controller to append
                // entries to the file
                val name = Arara.config[AraraSpec.loggingOptions].logFile.toString()
                loggerContext.putProperty("name", name)
                configurator.doConfigure(resource)
            }
        } catch (_: JoranException) {
            // quack, quack, quack!
        }
    }

    /**
     * Initializes the logging controller by disabling it. I don't want an odd
     * behaviour out of the box.
     */
    fun init() {
        enableLogging(false)
    }
}
