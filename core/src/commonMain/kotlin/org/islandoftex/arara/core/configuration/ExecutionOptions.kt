// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlin.time.Duration
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath

data class ExecutionOptions(
    override val maxLoops: Int = 10,
    override val timeoutValue: Duration = Duration.ZERO,
    override val parallelExecution: Boolean = false,
    override val haltOnErrors: Boolean = true,
    override val databaseName: MPPPath = MPPPath("arara.yaml"),
    override val verbose: Boolean = false,
    override val executionMode: ExecutionMode = ExecutionMode.NORMAL_RUN,
    override val rulePaths: Set<MPPPath> = setOf(
            ConfigurationUtils.applicationPath / "rules"),
    override val fileTypes: List<FileType> = ConfigurationUtils.defaultFileTypes,
    override val parseOnlyHeader: Boolean = false
) : ExecutionOptions {
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
