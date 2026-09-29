// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeoutException
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.ruleset.RuleFormat
import org.islandoftex.arara.cli.utils.Display
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.SystemCommandResult
import org.zeroturnaround.exec.InvalidExitValueException

/**
 * Implements interpreter auxiliary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
internal object InterpreterUtils {
    private val logger = KotlinLogging.logger { }

    /**
     * Checks if the current conditional has a prior evaluation.
     *
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    internal fun runPriorEvaluation(conditional: DirectiveConditional): Boolean {
        return if (LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            false
        } else {
            when (conditional.type) {
                DirectiveConditionalType.IF,
                DirectiveConditionalType.WHILE,
                DirectiveConditionalType.UNLESS -> true
                else -> false
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
            is SystemCommandResult.Failed ->
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
                logger.info {
                    """
                        ${Display.displayOutputSeparator(
                        "BEGIN OUTPUT BUFFER"
                    )}
                        ${result.output}
                        ${Display.displayOutputSeparator(
                        "END OUTPUT BUFFER"
                    )}
                    """.trimIndent()
                }
                result.exitCode
            }
        }
    }

    /**
     * Constructs the path given the current path and the rule name.
     *
     * @param path The current path.
     * @param name The rule name.
     * @param format The rule format determining the file extension.
     * @param workingDirectory The working directory to resolve relative
     *   rule paths against.
     * @return The constructed path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun construct(
        path: MPPPath,
        name: String,
        format: RuleFormat,
        workingDirectory: MPPPath
    ): MPPPath = "$name.${format.extension}".let { fileName ->
        if (path.isAbsolute) {
            path / fileName
        } else {
            // when retrieving rules the current project is never null
            // because the executor always acts on a project file;
            // first resolve the path (rule path) against the working
            // directory, then the rule name we want to resolve
            workingDirectory / path / fileName
        }
    }.normalize()
}
