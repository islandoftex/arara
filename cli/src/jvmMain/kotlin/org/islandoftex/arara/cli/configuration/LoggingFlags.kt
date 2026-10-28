// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.choice
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.configuration.LogRetention

/** Parsed logging options; registration remains owned by the command. */
internal class LoggingFlags : OptionGroup() {
    val log by option("-l", "--log")
        .help("Write a text diagnostic log (shorthand for --log-format=text)")
        .switch("-l" to LogFormat.TEXT, "--log" to LogFormat.TEXT)

    val logFormat by option("--log-format")
        .help("Set the diagnostic log format: none (default), text or json")
        .choice("none" to LogFormat.NONE, "text" to LogFormat.TEXT, "json" to LogFormat.JSON)

    val logLevel by option("--log-level")
        .help("Set the minimum diagnostic log level: trace (default), debug, info, warn or error")
        .choice("trace" to LogLevel.TRACE, "debug" to LogLevel.DEBUG, "info" to LogLevel.INFO,
            "warn" to LogLevel.WARN, "error" to LogLevel.ERROR)

    val logRetention by option("--log-retention")
        .help("Keep diagnostic logs always (default) or only on-failure")
        .choice("always" to LogRetention.ALWAYS, "on-failure" to LogRetention.ON_FAILURE)
}
