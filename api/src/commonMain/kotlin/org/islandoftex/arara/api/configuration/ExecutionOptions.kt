// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.configuration

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * arara's different execution modes. They have to be chosen before a run is
 * initialized.
 */
public enum class ExecutionMode {
    /**
     * In dry run mode, arara performs syntax checks of the rules and outputs
     * debug information. No command will be called.
     */
    DRY_RUN,

    /**
     * In safe run mode, arara will take measures not to execute potentially
     * unwanted actions like cascading out of the working directory or
     * executing arbitrary commands.
     */
    SAFE_RUN,

    /**
     * In normal run mode, arara executes everything it is able to giving the
     * run the full flexibility.
     */
    NORMAL_RUN,
}

/**
 * Configure arara's runtime behavior. These options will be frozen for an
 * execution once it has been started.
 */
@ExperimentalTime
public interface ExecutionOptions {
    /**
     * This is the maximum number of loops arara will run for a tool.
     * You cannot allow infinite runs (while true) with this.
     */
    public val maxLoops: Int

    /**
     * After how long a run should time out. Choose 0 milliseconds to
     * indicate you do not want a timeout.
     */
    public val timeoutValue: Duration

    /**
     * Whether arara should parallelize the execution.
     */
    public val parallelExecution: Boolean

    /**
     * Whether arara will halt on errors.
     */
    public val haltOnErrors: Boolean

    /**
     * The database name. The database is used to track changes for files
     * in a project. If it is not absolute it will be resolved against the
     * project's working directory.
     */
    public val databaseName: MPPPath

    /**
     * Whether arara will output the commands' standard out.
     */
    public val verbose: Boolean

    /**
     * arara's current execution mode. This is used to restrict arara's
     * abilities.
     */
    public val executionMode: ExecutionMode

    /**
     * The paths arara will search for rules. Should default to the rules arara
     * ships with. For safe runs, a restrictions seems useful.
     */
    public val rulePaths: Set<MPPPath>

    /**
     * The file types arara will look for and understand including patterns.
     * List entries should be unique and in order of their priority with the
     * most common file type as first element.
     */
    public val fileTypes: List<FileType>

    /**
     * Whether only to parse a contiguous block of comments at the start
     * of a file.
     */
    public val parseOnlyHeader: Boolean
}
