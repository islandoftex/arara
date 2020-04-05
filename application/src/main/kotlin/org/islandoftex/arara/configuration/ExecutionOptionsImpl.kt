// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.configuration

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.milliseconds
import org.islandoftex.arara.session.ExecutionMode
import org.islandoftex.arara.session.ExecutionOptions

data class ExecutionOptionsImpl(
    override val maxLoops: Int = 10,
    override val timeoutValue: Duration = 0.milliseconds,
    override val haltOnErrors: Boolean = true,
    override val databaseName: Path = Paths.get("arara.yaml"),
    override val verbose: Boolean = false,
    override val executionMode: ExecutionMode = ExecutionMode.NORMAL_RUN,
    override val parseOnlyHeader: Boolean = false
) : ExecutionOptions
