// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.File
import java.nio.file.Paths
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.ruleset.CommandImpl
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment

/**
 * Implements a system call controller.
 *
 * This class wraps a map that holds the result of system specific variables
 * not directly available at runtime and makes unsafe calling of system
 * commands available to rules.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object SystemCallUtils {
    /**
     * Determine whether arara is executed in a Cygwin environment (lazily to
     * save expensive system calls).
     *
     * The test executes a new system call to `uname -s` and determines by its
     * output whether we are running a Cygwin kernel.
     */
    private val inCygwinEnvironment by lazy {
        Environment.executeSystemCommand(
                CommandImpl(listOf("uname", "-s")),
                // execute the Cygwin detection from arara's execution directory
                // instead of the project's working directory as the information
                // whether one runs on Cygwin does not depend on the project
                Paths.get("")
        ).second.toLowerCase().startsWith("cygwin")
    }

    /**
     * Checks if the provided operating system string holds according to the
     * underlying operating system.
     *
     * Supported operating systems:
     *
     *   * Windows
     *   * Linux
     *   * Mac OS X
     *   * Unix (Linux || Mac OS)
     *   * Cygwin
     *
     * @param value A string representing an operating system.
     * @return A boolean value indicating if the provided string refers to the
     * underlying operating system.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun checkOS(value: String): Boolean {
        fun checkOSProperty(key: String): Boolean =
                Environment.getSystemPropertyOrNull("os.name")
                        ?.toLowerCase()?.startsWith(key.toLowerCase()) ?: false

        val values = mutableMapOf<String, Boolean>()
        values["windows"] = checkOSProperty("Windows")
        values["linux"] = checkOSProperty("Linux")
        values["mac"] = checkOSProperty("Mac OS X")
        values["unix"] = checkOSProperty("Mac OS X") ||
                checkOSProperty("Linux")
        values["cygwin"] = inCygwinEnvironment
        if (!values.containsKey(value.toLowerCase())) {
            throw AraraException(
                    LanguageController.messages
                            .ERROR_CHECKOS_INVALID_OPERATING_SYSTEM
                            .format(value)
            )
        }
        // will never throw, see check above
        return values.getValue(value.toLowerCase())
    }

    /**
     * Generates a list of filenames from the provided command based on a list
     * of extensions for each underlying operating system.
     *
     * @param command A string representing the command.
     * @return A list of filenames.
     */
    private fun appendExtensions(command: String): List<String> {
        // list of extensions, specific for
        // each operating system (in fact, it
        // is more Windows specific)
        val extensions = if (checkOS("windows")) {
            // the application is running on
            // Windows, so let's look for the
            // following extensions in order

            // this list is actually a sublist from
            // the original Windows PATHEXT environment
            // variable which holds the list of executable
            // extensions that Windows supports
            listOf(".com", ".exe", ".bat", ".cmd")
        } else {
            // no Windows, so the default
            // extension will be just an
            // empty string
            listOf("")
        }

        // return the resulting list holding the
        // filenames generated from the
        // provided command
        return extensions.map { "$command$it" }
    }

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    @JvmStatic
    fun isOnPath(command: String): Boolean {
        // first and foremost, let's build the list
        // of filenames based on the underlying
        // operating system
        val filenames = appendExtensions(command)
        return kotlin.runCatching {
            // break the path into several parts
            // based on the path separator symbol
            System.getenv("PATH").split(File.pathSeparator)
                    .asSequence()
                    .mapNotNull { File(it).listFiles() }
                    // if the search does not return an empty
                    // list, one of the filenames got a match,
                    // and the command is available somewhere
                    // in the system path
                    .firstOrNull {
                        it.any { file ->
                            filenames.contains(file.name) && !file.isDirectory
                        }
                    }?.let { true }
        }.getOrNull() ?: false
        // otherwise (and in case of an exception) it is not in the path
    }
}
