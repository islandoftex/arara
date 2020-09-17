// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import org.islandoftex.arara.api.session.Command
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer
import org.zeroturnaround.exec.stream.TeeOutputStream

/**
 * An object to handle interaction with the operating system.
 */
object Environment {
    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(Environment::class.java)

    /**
     * Gets the system property according to the provided key, or resort to the
     * fallback value if an exception is thrown or if the key is invalid.
     *
     * @param key The system property key.
     * @param fallback The fallback value.
     * @return A string containing the system property value or the fallback.
     */
    @JvmStatic
    fun getSystemProperty(key: String, fallback: String): String =
            System.getProperties().runCatching {
                getOrDefault(key, fallback).toString().takeIf { it != "" }
            }.getOrNull() ?: fallback

    /**
     * Access a system property.
     *
     * @param key The key of the property.
     * @return The value of the system property or null if there is an
     *   exception.
     */
    @JvmStatic
    fun getSystemPropertyOrNull(key: String): String? =
            System.getProperties().runCatching { getValue(key).toString() }
                    .getOrNull()

    /**
     * When executing a system call goes wrong, this status code is returned.
     */
    const val errorExitStatus = -99

    /**
     * Executes a system command from the underlying operating system and
     * returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @param silenceSystemOut If true, the system output will not be appended
     *   to. Otherwise, a split is performed between [System.out] and an
     *   internal capture buffer.
     * @param timeout An optional timeout. Non-zero values will be applied to
     *   the executor.
     * @return A pair containing the exit status and the system command output
     *   as a string. In case of an error, return a pair of [errorExitStatus]
     *   and the string representation of the throwable.
     */
    @JvmStatic
    fun executeSystemCommand(
        command: Command,
        silenceSystemOut: Boolean = true,
        timeout: Duration = Duration.ZERO
    ): Pair<Int, String> = ByteArrayOutputStream().use { buffer ->
        ProcessExecutor(command.elements).runCatching {
            addDestroyer(ShutdownHookProcessDestroyer())

            // use the command's working directory as the preferred working
            // directory; although it may be missing in which case we will use
            // arara's execution directory by default
            val workingDirectory = command.workingDirectory
                    ?: Paths.get("")
            directory(workingDirectory.toFile().absoluteFile)

            // implement output redirection if necessary for verbose
            // output
            val tee = if (silenceSystemOut) {
                buffer
            } else {
                redirectInput(System.`in`)
                TeeOutputStream(System.out, buffer)
            }
            redirectOutput(tee).redirectError(tee)

            // add non-zero timeout to the executor to restrict runtime
            if (timeout != Duration.ZERO) {
                timeout(timeout.toLongNanoseconds(), TimeUnit.NANOSECONDS)
            }

            execute().exitValue to buffer.toString()
        }.getOrElse {
            // quack, quack, do nothing, just
            // return a default error code
            logger.debug("Caught an exception when executing " +
                    "$command returning $errorExitStatus")
            errorExitStatus to "${it::class.java.name}: ${it.message}"
        }
    }
}
