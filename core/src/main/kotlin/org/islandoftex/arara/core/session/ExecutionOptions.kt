// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import org.islandoftex.arara.api.session.ExecutionMode
import org.islandoftex.arara.api.session.ExecutionOptions

@ExperimentalTime
data class ExecutionOptions(
    override val maxLoops: Int = 10,
    override val timeoutValue: Duration = 0.milliseconds,
    override val parallelExecution: Boolean = true,
    override val haltOnErrors: Boolean = true,
    override val databaseName: Path = Paths.get("arara.yaml"),
    override val verbose: Boolean = false,
    override val executionMode: ExecutionMode = ExecutionMode.NORMAL_RUN,
    override val parseOnlyHeader: Boolean = false
) : ExecutionOptions
