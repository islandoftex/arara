// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeoutException
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.SystemCommandResult
import org.zeroturnaround.exec.InvalidExitValueException

/**
 * Implements the system command runner: the single execution surface the
 * interpreter dispatches through, pairing every run with its duration
 * and the output buffer, and mapping failures to their localized errors.
 *
 * @author Island of TeX
 */
internal object CommandRunner {
    private val logger = KotlinLogging.logger { }

    /**
     * Writes the captured output of a command to the log: a transcript
     * is framed between the buffer markers - the tool's own words, on
     * either outcome - and a silent tool is a recorded fact, not an
     * absent one, so the emptiness is stated instead of framed.
     *
     * @param output The captured output of the command.
     */
    private fun logOutputBuffer(output: String) {
        logger.info {
            if (output.isBlank()) {
                "The tool produced no output."
            } else {
                """
                    ${RunLog.outputSeparator("BEGIN OUTPUT BUFFER")}
                    ${output}
                    ${RunLog.outputSeparator("END OUTPUT BUFFER")}
                """.trimIndent()
            }
        }
    }

    /**
     * Runs the command in the underlying operating system.
     *
     * @param command An object representing the command.
     * @return An integer value representing the exit code.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun run(command: Command): Int {
        // the monotonic clock keeps the recorded duration immune to wall
        // clock corrections that can strike while a command runs
        val start = TimeSource.Monotonic.markNow()
        val result = Environment.executeSystemCommand(
            command,
            !LinearExecutor.executionOptions.verbose,
            LinearExecutor.executionOptions.timeoutValue
        )
        val duration = start.elapsedNow()
        return when (result) {
            is SystemCommandResult.Failed -> {
                logOutputBuffer(result.output)
                throw AraraException(
                    when (result.exception) {
                        is IOException ->
                            LanguageController.messages.errorRunIoException
                        is TimeoutException ->
                            LanguageController.messages
                                .errorRunTimeoutException
                        is InterruptedException ->
                            LanguageController.messages
                                .errorRunInterruptedException
                        is InvalidExitValueException ->
                            LanguageController.messages
                                .errorRunInvalidExitValueException
                        else -> LanguageController.messages
                            .errorRunGenericException
                    },
                    result.exception
                )
            }
            is SystemCommandResult.Completed -> {
                logger.info {
                    // the exit value is recorded here because the caller
                    // reduces it to a plain boolean right afterwards, and
                    // the duration pairs the two facts in one place
                    "System command exited with value ${result.exitCode} in " +
                        String.format(
                            Locale.ROOT, "%.3f",
                            duration.toDouble(DurationUnit.SECONDS)
                        ) + " seconds"
                }
                logOutputBuffer(result.output)
                result.exitCode
            }
        }
    }
}
