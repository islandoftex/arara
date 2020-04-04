// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.islandoftex.arara.Arara
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.rules.DirectiveConditional
import org.islandoftex.arara.rules.DirectiveConditionalType
import org.islandoftex.arara.session.Command
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.InvalidExitValueException
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer

/**
 * Implements interpreter utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object InterpreterUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(InterpreterUtils::class.java)

    /**
     * Checks if the current conditional has a prior evaluation.
     *
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    fun runPriorEvaluation(conditional: DirectiveConditional): Boolean {
        return if (Arara.config[AraraSpec.Execution.dryrun]) {
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

    private fun getProcessExecutorForCommand(
        command: Command,
        buffer: OutputStream
    ): ProcessExecutor {
        val timeOutValue = Arara.config[AraraSpec.Execution.timeoutValue]
        val workingDirectory = command.workingDirectory
                ?: Arara.config[AraraSpec.Execution.workingDirectory]
        var executor = ProcessExecutor().command((command).elements)
                .directory(workingDirectory.toFile().absoluteFile)
                .addDestroyer(ShutdownHookProcessDestroyer())
        if (Arara.config[AraraSpec.Execution.timeout]) {
            executor = executor.timeout(timeOutValue.toLongNanoseconds(),
                    TimeUnit.NANOSECONDS)
        }
        val tee = if (Arara.config[AraraSpec.Execution.verbose]) {
            executor = executor.redirectInput(System.`in`)
            TeeOutputStream(System.out, buffer)
        } else {
            TeeOutputStream(buffer)
        }
        executor = executor.redirectOutput(tee).redirectError(tee)
        return executor
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
    fun run(command: Command): Int {
        val buffer = ByteArrayOutputStream()
        val executor = getProcessExecutorForCommand(command, buffer)
        return executor.runCatching {
            val exit = execute().exitValue
            logger.info(DisplayUtils.displayOutputSeparator(
                    messages.getMessage(Messages.LOG_INFO_BEGIN_BUFFER)))
            logger.info(buffer.toString())
            logger.info(DisplayUtils.displayOutputSeparator(
                    messages.getMessage(Messages.LOG_INFO_END_BUFFER)))
            exit
        }.getOrElse {
            throw AraraException(
                    messages.getMessage(
                            when (it) {
                                is IOException -> Messages.ERROR_RUN_IO_EXCEPTION
                                is InterruptedException ->
                                    Messages.ERROR_RUN_INTERRUPTED_EXCEPTION
                                is InvalidExitValueException ->
                                    Messages.ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                                is TimeoutException ->
                                    Messages.ERROR_RUN_TIMEOUT_EXCEPTION
                                else -> Messages.ERROR_RUN_GENERIC_EXCEPTION
                            }
                    ), it
            )
        }
    }

    /**
     * Builds the rule path based on the rule name and returns the corresponding
     * file location.
     *
     * @param name The rule name.
     * @return The rule file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun buildRulePath(name: String): File? {
        Arara.config[AraraSpec.Execution.rulePaths].forEach { path ->
            val location = File(construct(path, name))
            if (location.exists())
                return location
        }
        return null
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
    fun construct(path: String, name: String): String {
        val fileName = "$name.yaml"
        val location = File(path)
        return if (location.isAbsolute) {
            location.resolve(fileName).toString()
        } else {
            Arara.config[AraraSpec.Execution.workingDirectory]
                    // first resolve the path (rule path) against the working
                    // directory, then the rule name we want to resolve
                    .resolve(path).resolve(fileName).toAbsolutePath().toString()
        }
    }
}
