// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType

@ExperimentalTime
data class ExecutionOptions(
    override val maxLoops: Int = 10,
    override val timeoutValue: Duration = 0.milliseconds,
    override val parallelExecution: Boolean = false,
    override val haltOnErrors: Boolean = true,
    override val databaseName: Path = Paths.get("arara.yaml"),
    override val verbose: Boolean = false,
    override val executionMode: ExecutionMode = ExecutionMode.NORMAL_RUN,
    override val rulePaths: Set<Path> = setOf(ConfigurationUtils.applicationPath.resolve("rules")),
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
