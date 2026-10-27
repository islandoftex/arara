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

    /** Minimum diagnostic severity; console verbosity is configured separately. */
    public val level: LogLevel get() = LogLevel.TRACE

    /** Whether successful invocations keep their diagnostic output. */
    public val retention: LogRetention get() = LogRetention.ALWAYS
}

/** Selection and encoding of a diagnostic log file, independently of console presentation. */
public enum class LogFormat {
    NONE,
    TEXT,
    JSON
}

/** Minimum severity recorded in diagnostic logs, independently of console verbosity. */
public enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/** Whether diagnostic output is retained after a successful invocation. */
public enum class LogRetention {
    ALWAYS,
    ON_FAILURE
}
