// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlin.time.Duration
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath

/**
 * Implements the execution options model, i.e. the knobs controlling how
 * arara runs: loop caps, timeouts, rule paths, file types, the execution
 * mode and the file database settings.
 */
data class ExecutionOptions(
    /**
     * The maximum number of loops `while` and `until` conditionals may
     * take.
     */
    override val maxLoops: Int = 10,
    /**
     * The wall clock span after which a running system command is
     * aborted.
     */
    override val timeoutValue: Duration = Duration.ZERO,
    /**
     * Whether projects may run their files in parallel.
     */
    override val parallelExecution: Boolean = false,
    /**
     * Whether an unsuccessful command halts the whole run.
     */
    override val haltOnErrors: Boolean = true,
    /**
     * The file the database is kept in - the value names the file
     * exactly, and a relative name resolves against the working
     * directory.
     */
    override val databaseName: MPPPath = MPPPath("arara.db"),
    /**
     * Whether the terminal echoes the output of system commands live.
     */
    override val verbose: Boolean = false,
    /**
     * The mode arara runs in: the normal run, the dry run or the safe
     * run.
     */
    override val executionMode: ExecutionMode = ExecutionMode.NORMAL_RUN,
    /**
     * The directories rules are looked up in, in order of priority.
     */
    override val rulePaths: Set<MPPPath> = setOf(
        DefaultConfiguration.applicationPath / "rules"
    ),
    /**
     * The known file types, mapped by extension and pattern.
     */
    override val fileTypes: List<FileType> = DefaultConfiguration.defaultFileTypes,
    /**
     * Whether directives are extracted from the header only, instead
     * of the whole file.
     */
    override val parseOnlyHeader: Boolean = true
) : ExecutionOptions {
    /**
     * Holds the conversion to the copyable implementation.
     */
    companion object {
        /**
         * As the interface does not have a copy method, we provide this
         * conversion method.
         */
        fun from(options: ExecutionOptions):
            org.islandoftex.arara.core.configuration.ExecutionOptions {
            return ExecutionOptions(
                maxLoops = options.maxLoops,
                timeoutValue = options.timeoutValue,
                parallelExecution = options.parallelExecution,
                haltOnErrors = options.haltOnErrors,
                databaseName = options.databaseName,
                verbose = options.verbose,
                executionMode = options.executionMode,
                rulePaths = options.rulePaths,
                fileTypes = options.fileTypes,
                parseOnlyHeader = options.parseOnlyHeader,
                warnDeprecatedYaml = options.warnDeprecatedYaml
            )
        }
    }
}
