// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.files.MPPPath

/**
 * Implements the logging options model, deciding whether arara logs to a
 * file at all and which file it appends to.
 */
data class LoggingOptions(
    override val enableLogging: Boolean = false,
    override val appendLog: Boolean = false,
    override val logFile: MPPPath = MPPPath("arara.log")
) : LoggingOptions
