// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.logging

import org.islandoftex.arara.api.files.ProjectFile

/** Diagnostic events for invocation, project and file lifecycles. */
public interface RunLifecycleLog {
    /** Record the application version greeting. */
    public fun welcome(version: String)

    /** Record input metadata when informational logging is enabled. */
    public fun fileInformation(file: ProjectFile)

    /** Record an application error with optional details and the original cause. */
    public fun exception(message: String?, details: String?, cause: Throwable? = null)

    /** Record elapsed seconds as text and milliseconds as a structured field. */
    public fun executionTime(seconds: Double)

    /** Record the project name and working directory. */
    public fun project(name: String, directory: String)

    /** Record invocation startup with the application version. */
    public fun executionStarted(version: String)

    /** Record the final exit code; unsuccessful runs use the error level. */
    public fun executionOutcome(exitCode: Int)
}
