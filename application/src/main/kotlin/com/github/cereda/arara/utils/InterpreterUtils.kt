/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.utils

import com.github.cereda.arara.controller.ConfigurationController
import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.model.*
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.InvalidExitValueException
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Implements interpreter utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object InterpreterUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(InterpreterUtils::class.java)

    /**
     * Gets a list of all rule arguments.
     *
     * @param rule The provided rule.
     * @return A list of strings containing all rule arguments.
     */
    // TODO: should be a function of rule, shouldn't it?
    fun getRuleArguments(rule: Rule): List<String> {
        return rule.arguments.mapNotNull { it.identifier }
    }

    /**
     * Checks if the current conditional has a prior evaluation.
     *
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    fun runPriorEvaluation(conditional: Conditional): Boolean {
        if (ConfigurationController["execution.dryrun"] as Boolean) {
            return false
        }
        return when (conditional.type) {
            Conditional.ConditionalType.IF,
            Conditional.ConditionalType.WHILE,
            Conditional.ConditionalType.UNLESS -> true
            else -> false
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
    fun run(command: Any): Int {
        val verbose = ConfigurationController["execution.verbose"] as Boolean
        val timeout = ConfigurationController["execution.timeout"] as Boolean
        val value = ConfigurationController["execution.timeout.value"] as Long
        val unit = ConfigurationController["execution.timeout.unit"] as TimeUnit
        val buffer = ByteArrayOutputStream()
        var executor = ProcessExecutor()
        if (command is Command) {
            executor = executor.command((command).elements)
            if (command.hasWorkingDirectory()) {
                executor = executor.directory(
                        command.workingDirectory
                )
            }
        } else {
            executor = executor.commandSplit(command as String)
        }
        if (timeout) {
            if (value == 0L) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_RUN_TIMEOUT_INVALID_RANGE
                        )
                )
            }
            executor = executor.timeout(value, unit)
        }
        val tee: TeeOutputStream
        if (verbose) {
            tee = TeeOutputStream(System.out, buffer)
            executor = executor.redirectInput(System.`in`)
        } else {
            tee = TeeOutputStream(buffer)
        }
        executor = executor.redirectOutput(tee).redirectError(tee)
        val hook = ShutdownHookProcessDestroyer()
        executor = executor.addDestroyer(hook)
        try {
            val exit = executor.execute().exitValue
            logger.info(DisplayUtils.displayOutputSeparator(
                    messages.getMessage(
                            Messages.LOG_INFO_BEGIN_BUFFER
                    )
            ))
            logger.info(buffer.toString())
            logger.info(DisplayUtils.displayOutputSeparator(
                    messages.getMessage(
                            Messages.LOG_INFO_END_BUFFER
                    )
            ))
            return exit
        } catch (ioexception: IOException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_IO_EXCEPTION
                    ),
                    ioexception
            )
        } catch (iexception: InterruptedException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_INTERRUPTED_EXCEPTION
                    ),
                    iexception
            )
        } catch (ievexception: InvalidExitValueException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                    ),
                    ievexception
            )
        } catch (texception: TimeoutException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_TIMEOUT_EXCEPTION
                    ),
                    texception
            )
        } catch (exception: Exception) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_GENERIC_EXCEPTION
                    ),
                    exception
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
        val paths = ConfigurationController["execution.rule.paths"] as List<String>
        for (path in paths) {
            val location = File(construct(path, name))
            if (location.exists()) {
                return location
            }
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
            CommonUtils.buildPath(path, fileName)
        } else {
            val reference = ConfigurationController["execution.reference"] as File
            val parent = CommonUtils.buildPath(
                    CommonUtils.getParentCanonicalPath(reference), path)
            CommonUtils.buildPath(parent, fileName)
        }
    }

}
