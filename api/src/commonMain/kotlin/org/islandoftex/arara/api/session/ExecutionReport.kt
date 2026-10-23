// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import org.islandoftex.arara.api.configuration.ExecutionOptions

/**
 * A [ExecutionReport] is intended to present the result of a completed
 * execution.
 */
public interface ExecutionReport {
    /**
     * When the execution started. This should be measured after all
     * initialization work (logging etc.) has been done to really measure
     * the execution with lowest overhead.
     */
    @ExperimentalTime
    public val executionStarted: TimeMark

    /**
     * When the execution stopped. This should be measured before cleaning up
     * and making nice output to only measure the execution with the lowest
     * overhead.
     */
    @ExperimentalTime
    public val executionStopped: TimeMark

    /**
     * The exit code reported by the execution. It will propagate the first
     * error exit code on [ExecutionOptions.haltOnErrors], the last error exit
     * code if [ExecutionOptions.haltOnErrors] is falsy and the success code
     * `0` if the run completes successfully.
     */
    public val exitCode: Int
}
