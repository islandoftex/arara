// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.time.milliseconds
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.InvalidExitValueException
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer
import org.zeroturnaround.exec.stream.TeeOutputStream

/**
 * Implements interpreter utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object InterpreterUtils {
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
        return if (Arara.config[AraraSpec.executionOptions].executionMode ==
                ExecutionMode.DRY_RUN) {
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
        val timeOutValue = Arara.config[AraraSpec.executionOptions].timeoutValue
        val workingDirectory = command.workingDirectory
                ?: Arara.config[AraraSpec.Execution.currentProject].workingDirectory
        var executor = ProcessExecutor().command((command).elements)
                .directory(workingDirectory.toFile().absoluteFile)
                .addDestroyer(ShutdownHookProcessDestroyer())
        if (timeOutValue != 0.milliseconds) {
            executor = executor.timeout(timeOutValue.toLongNanoseconds(),
                    TimeUnit.NANOSECONDS)
        }
        val tee = if (Arara.config[AraraSpec.executionOptions].verbose) {
            executor = executor.redirectInput(System.`in`)
            TeeOutputStream(System.out, buffer)
        } else {
            buffer
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
                    LanguageController.messages.LOG_INFO_BEGIN_BUFFER))
            logger.info(buffer.toString())
            logger.info(DisplayUtils.displayOutputSeparator(
                    LanguageController.messages.LOG_INFO_END_BUFFER))
            exit
        }.getOrElse {
            throw AraraException(
                    LanguageController.messages.run {
                        when (it) {
                            is IOException -> ERROR_RUN_IO_EXCEPTION
                            is InterruptedException -> ERROR_RUN_INTERRUPTED_EXCEPTION
                            is InvalidExitValueException -> ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                            is TimeoutException -> ERROR_RUN_TIMEOUT_EXCEPTION
                            else -> ERROR_RUN_GENERIC_EXCEPTION
                        }
                    }, it
            )
        }
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
    fun construct(path: Path, name: String): Path {
        val fileName = "$name.yaml"
        return FileHandling.normalize(
                if (path.isAbsolute) {
                    path.resolve(fileName)
                } else {
                    Arara.config[AraraSpec.Execution.currentProject].workingDirectory
                            // first resolve the path (rule path) against the working
                            // directory, then the rule name we want to resolve
                            .resolve(path).resolve(fileName)
                }
        )
    }
}
