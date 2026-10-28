// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlin.time.Duration.Companion.milliseconds
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.core.configuration.ExecutionOptions

/** Parsed execution options; registration remains owned by the command. */
internal class ExecutionFlags : OptionGroup() {
    val verbose by option("-v", "--verbose")
        .help("Print the command output")
        .flag()

    val silent by option("-s", "--silent")
        .help("Suppress command output, overriding configuration and --verbose")
        .flag()

    val dryRun by option("-n", "--dry-run")
        .help(
            "Go through all the motions of running a command, but " +
                "with no actual calls"
        )
        .flag()

    val safeRun by option("-S", "--safe-run")
        .help(
            "Run in safe mode and disable potentially harmful features. " +
                "Make sure your projects uses only allowed features."
        )
        .flag()

    val wholeFile by option("-w", "--whole-file")
        .help("Extract directives in the file, not only in the header")
        .flag()

    val preamble by option("-p", "--preamble")
        .help("Set the file preamble based on the configuration file")

    val timeout by option("-t", "--timeout")
        .help("Set the execution timeout (in milliseconds)")
        .int().restrictTo(min = 1)

    val maxLoops by option("-m", "--max-loops")
        .help("Set the maximum number of loops (> 0)")
        .int().restrictTo(min = 1)

    fun apply(base: org.islandoftex.arara.api.configuration.ExecutionOptions) = ExecutionOptions
        .from(base)
        .copy(
            maxLoops = maxLoops
                ?: base.maxLoops,
            timeoutValue = timeout?.let { it.milliseconds }
                ?: base.timeoutValue,
            verbose = when {
                silent -> false
                verbose -> true
                else -> base.verbose
            },
            executionMode = when {
                dryRun -> ExecutionMode.DRY_RUN
                safeRun -> ExecutionMode.SAFE_RUN
                else -> base.executionMode
            },
            parseOnlyHeader = if (wholeFile)
                false
            else
                base.parseOnlyHeader
        )

}
