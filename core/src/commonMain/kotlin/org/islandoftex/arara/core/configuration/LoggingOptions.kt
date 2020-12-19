// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.files.MPPPath

data class LoggingOptions(
    override val enableLogging: Boolean = false,
    override val appendLog: Boolean = false,
    override val logFile: MPPPath = MPPPath("arara.log")
) : LoggingOptions
