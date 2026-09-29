// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.files.MPPPath

/**
 * Implements the logging options model, deciding whether arara logs to a
 * file at all and which file it writes to.
 */
data class LoggingOptions(
    /**
     * Whether arara writes a log file at all.
     */
    override val enableLogging: Boolean = false,
    /**
     * Whether the log appends to the previous run instead of
     * overwriting it.
     */
    override val appendLog: Boolean = false,
    /**
     * The log file, relative to the working directory.
     */
    override val logFile: MPPPath = MPPPath("arara.log")
) : LoggingOptions
