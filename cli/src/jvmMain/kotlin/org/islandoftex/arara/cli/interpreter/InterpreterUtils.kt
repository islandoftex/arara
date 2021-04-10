// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import mu.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor

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
    internal fun run(command: Command): Int = Environment.executeSystemCommand(
            command,
            !LinearExecutor.executionOptions.verbose,
            LinearExecutor.executionOptions.timeoutValue
    ).let {
        val (exitCode, output) = it
        if (exitCode == Environment.errorExitStatus) {
            throw AraraException(
                    LanguageController.messages.run {
                        when (output.substringBefore(":")) {
                            "java.io.IOException" ->
                                ERROR_RUN_IO_EXCEPTION
                            "java.util.concurrent.TimeoutException" ->
                                ERROR_RUN_TIMEOUT_EXCEPTION
                            "java.lang.InterruptedException" ->
                                ERROR_RUN_INTERRUPTED_EXCEPTION
                            "org.zeroturnaround.exec.InvalidExitValueException" ->
                                ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                            else -> ERROR_RUN_GENERIC_EXCEPTION
                        }
                    }, AraraException(output)
            )
        }
        logger.info {
            """
                ${DisplayUtils.displayOutputSeparator(
                    LanguageController.messages.LOG_INFO_BEGIN_BUFFER)}
                $output
                ${DisplayUtils.displayOutputSeparator(
                    LanguageController.messages.LOG_INFO_END_BUFFER)}
            """.trimIndent()
        }
        exitCode
    }

    /**
     * Constructs the path given the current path and the rule name.
     *
     * @param path The current path.
     * @return The constructed path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun resolveAgainstDirectory(
        path: MPPPath,
        workingDirectory: MPPPath
    ): MPPPath =
            (path.takeIf { it.isAbsolute } ?: workingDirectory.resolve(path))
                    .normalize()
}
