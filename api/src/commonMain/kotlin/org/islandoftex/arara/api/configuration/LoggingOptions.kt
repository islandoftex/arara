// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.configuration

import org.islandoftex.arara.api.files.MPPPath

/**
 * Configure arara's logging facilities.
 */
public interface LoggingOptions {
    /**
     * Whether to append to existing log files or to overwrite them. Should
     * be `false` by default in every implementation.
     */
    public val appendLog: Boolean

    /**
     * The log file to write to.
     */
    public val logFile: MPPPath

    /** Diagnostic log format; [LogFormat.NONE] disables logging. */
    public val format: LogFormat
}
