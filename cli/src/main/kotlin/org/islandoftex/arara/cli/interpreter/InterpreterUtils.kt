// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeoutException
import kotlin.io.path.div
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.ruleset.RuleFormat
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.slf4j.LoggerFactory

/**
 * Implements interpreter auxiliary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
internal object InterpreterUtils {
    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(InterpreterUtils::class.java)

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
                            IOException::class.java.name ->
                                ERROR_RUN_IO_EXCEPTION
                            TimeoutException::class.java.name ->
                                ERROR_RUN_TIMEOUT_EXCEPTION
                            InterruptedException::class.java.name ->
                                ERROR_RUN_INTERRUPTED_EXCEPTION
                            "org.zeroturnaround.exec.InvalidExitValueException" ->
                                ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                            else -> ERROR_RUN_GENERIC_EXCEPTION
                        }
                    }, AraraException(output)
            )
        }
        logger.info(
                DisplayUtils.displayOutputSeparator(
                        LanguageController.messages.LOG_INFO_BEGIN_BUFFER
                )
        )
        logger.info(output)
        logger.info(
                DisplayUtils.displayOutputSeparator(
                        LanguageController.messages.LOG_INFO_END_BUFFER
                )
        )
        exitCode
    }

    /**
     * Constructs the path given the current path and the rule name.
     *
     * @param path The current path.
     * @param name The rule name.
     * @return The constructed path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun construct(
        path: Path,
        name: String,
        format: RuleFormat,
        workingDirectory: Path
    ): Path {
        val fileName = "$name.${format.extension}"
        return FileHandling.normalize(
                if (path.isAbsolute) {
                    path / fileName
                } else {
                    // when retrieving rules the current project is never null
                    // because the executor always acts on a project file;
                    // first resolve the path (rule path) against the working
                    // directory, then the rule name we want to resolve
                    workingDirectory / path / fileName
                }
        )
    }
}
