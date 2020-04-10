// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.nio.file.Path
import org.islandoftex.arara.api.session.Command
import org.zeroturnaround.exec.ProcessExecutor

/**
 * An object to handle interaction with the operating system.
 */
object Environment {
    /**
     * Gets the system property according to the provided key, or resort to the
     * fallback value if an exception is thrown or if the key is invalid.
     *
     * @param key The system property key.
     * @param fallback The fallback value.
     * @return A string containing the system property value or the fallback.
     */
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
    fun getSystemPropertyOrNull(key: String): String? =
            System.getProperties().runCatching { getValue(key).toString() }
                    .getOrNull()

    /**
     * When executing a system call goes wrong, this status code is returned.
     */
    const val errorExitStatus = -99
    /**
     * When executing a system call goes wrong and the caller asked for output,
     * this output will be returned.
     */
    const val errorCommandOutput = ""

    /**
     * Executes a system command from the underlying operating system and
     * returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @return A pair containing the exit status and the system command output
     *   as a string. In case of an error, return the pair [errorExitStatus]
     *   and [errorCommandOutput].
     */
    fun executeSystemCommand(
        command: Command,
        workingDirectory: Path
    ): Pair<Int, String> {
        return ProcessExecutor(command.elements).runCatching {
            directory(workingDirectory.toFile().absoluteFile)
            readOutput(true)
            execute().run {
                exitValue to outputUTF8()
            }
        }.getOrElse {
            // quack, quack, do nothing, just
            // return a default error code
            errorExitStatus to errorCommandOutput
            // TODO: debug log entry
        }
    }
}
