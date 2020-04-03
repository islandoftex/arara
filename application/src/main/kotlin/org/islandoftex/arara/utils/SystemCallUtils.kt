// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.utils

import org.islandoftex.arara.AraraException
import org.islandoftex.arara.ruleset.Command
import org.zeroturnaround.exec.ProcessExecutor

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
     * When executing a system call goes wrong, this status code is returned.
     */
    const val errorExitStatus = -99
    /**
     * When executing a system call goes wrong and the caller asked for output,
     * this output will be returned.
     */
    const val errorCommandOutput = ""

    // the system call map which holds the result of
    // system specific variables not directly available
    // at runtime; the idea here is to provide wrappers
    // to the map getter, so it could be easily manipulated
    // create the new map instance to be
    // populated on demand
    private val map: MutableMap<String, Any> = mutableMapOf()

    // the commands map will allow the system call map being
    // populated only on demand, that is, if the key is not
    // found, this map will provide the corresponding method
    // and update the value
    // create the new map of commands and
    // add the corresponding system calls
    private val commands: MutableMap<String, () -> Any> = mutableMapOf(
            "cygwin" to {
                // Implements the body of the command. In this particular
                // instance, it checks if we are inside a Cygwin environment.
                // Returns a boolean value indicating if we are inside a Cygwin
                // environment.

                // execute a new system call to 'uname -s', read the output
                // as an UTF-8 string, lowercase it and check if it starts
                // with the 'cygwin' string; if so, we are inside Cygwin
                executeSystemCommand(Command("uname", "-s"))
                        .second.toLowerCase().startsWith("cygwin")
            })

    /**
     * Gets the object indexed by the provided key. This method actually holds
     * the map method of the very same name.
     *
     * @param key The provided map key.
     * @return The object indexed by the provided map key.
     */
    @Throws(NoSuchElementException::class, AraraException::class)
    operator fun get(key: String): Any {
        // if key is not found, meaning that
        // the value wasn't required before
        if (!map.containsKey(key)) {
            if (commands.containsKey(key))
            // perform the system call and
            // populate the corresponding value
                map[key] = commands[key]!!.invoke()
            else
                throw AraraException(
                        "The requested key could not be " +
                                "translated into a command to get the call value."
                )
        }

        // simply return the corresponding
        // value based on the provided key
        return map.getValue(key)
    }

    /**
     * Executes a system command from the underlying operating system and
     * returns a pair containing the exit status and the command output as a
     * string.
     * @param command The system command to be executed.
     * @return A pair containing the exit status and the system command output
     * as a string.
     */
    fun executeSystemCommand(command: Command): Pair<Int, String> {
        return ProcessExecutor(command.elements).runCatching {
            directory(command.workingDirectory.absoluteFile)
            readOutput(true)
            execute().run {
                exitValue to outputUTF8()
            }
        }.getOrElse {
            // quack, quack, do nothing, just
            // return a default error code
            errorExitStatus to errorCommandOutput
        }
    }
}
