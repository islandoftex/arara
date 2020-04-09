// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

import java.nio.file.Path

/**
 * Configure arara's logging facilities.
 */
interface LoggingOptions {
    /**
     * Whether to enable logging. This means logging to a file and in verbose
     * mode to the terminal output.
     */
    val enableLogging: Boolean

    /**
     * Whether to append to existing log files or to overwrite them. Should
     * be `false` by default in every implementation.
     */
    val appendLog: Boolean

    /**
     * The log file to write to.
     */
    val logFile: Path
}
