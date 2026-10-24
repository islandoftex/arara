// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.files.MPPPath

/** Logging settings; logging is disabled by default. */
data class LoggingOptions(
    /** Output format, including NONE to disable logging. */
    override val format: LogFormat = LogFormat.NONE,
    /** Whether an existing log is appended to instead of replaced. */
    override val appendLog: Boolean = false,
    /** Destination path for the selected log format. */
    override val logFile: MPPPath = MPPPath("arara.log")
) : LoggingOptions
