// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlin.time.Duration
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath

/** Runtime execution settings with defaults for a single sequential run. */
data class ExecutionOptions(
    /** Maximum iterations of a while or until conditional. */
    override val maxLoops: Int = 10,
    /** Wall-clock limit per system command; [Duration.ZERO] disables the timeout. */
    override val timeoutValue: Duration = Duration.ZERO,
    /** Whether parallel execution is requested; the linear executor rejects this option. */
    override val parallelExecution: Boolean = false,
    /** Whether execution stops after an unsuccessful task or file. */
    override val haltOnErrors: Boolean = true,
    /** Database path, resolved from the process working directory when relative. */
    override val databaseName: MPPPath = MPPPath("arara.yaml"),
    /** Whether subprocess output is echoed to the terminal. */
    override val verbose: Boolean = false,
    /** Normal, dry-run or safe execution. */
    override val executionMode: ExecutionMode = ExecutionMode.NORMAL_RUN,
    /** Directories searched for rules, in iteration order. */
    override val rulePaths: Set<MPPPath> = setOf(
        ConfigurationUtils.applicationPath / "rules"
    ),
    /** Recognized extensions and their directive comment patterns. */
    override val fileTypes: List<FileType> = ConfigurationUtils.defaultFileTypes,
    /** Whether directive extraction stops at the first line outside the comment pattern. */
    override val parseOnlyHeader: Boolean = true
) : ExecutionOptions {
    /** Conversions from the public execution-options interface. */
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
                parseOnlyHeader = options.parseOnlyHeader
            )
        }
    }
}
