// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.session.LoggingOptions

data class LoggingOptions(
    override val enableLogging: Boolean = false,
    override val appendLog: Boolean = false,
    override val logFile: Path = Paths.get("arara.log")
) : LoggingOptions
