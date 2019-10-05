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

import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.ruleset.Command
import org.zeroturnaround.exec.ProcessExecutor

/**
 * Implements a system call controller.
 *
 * This class wraps a map that holds the result of system specific variables
 * not directly available at runtime and makes unsafe calling of system
 * commands available to rules.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object SystemCallUtils {
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
            // add the check for a Cygwin
            // environment in here
            "cygwin" to {
                /*
                 * Implements the body of the command. In this particular
                 * instance, it checks if we are inside a Cygwin environment.
                 * @return A boolean value indicating if we are inside a Cygwin
                 * environment.
                 */
                try {
                    // execute a new system call to 'uname -s', read the output
                    // as an UTF-8 string, lowercase it and check if it starts
                    // with the 'cygwin' string; if so, we are inside Cygwin
                    executeSystemCommand(Command("uname", "-s"))
                            .second.toLowerCase().startsWith("cygwin")
                } catch (exception: Exception) {
                    // gracefully fallback in case of any nasty and evil
                    // exception, e.g, if the command is unavailable
                    false
                }
            })

    /**
     * Gets the object indexed by the provided key. This method actually holds
     * the map method of the very same name.
     *
     * @param key The provided map key.
     * @return The object indexed by the provided map key.
     */
    operator fun get(key: String): Any {
        // if key is not found, meaning that
        // the value wasn't required before
        if (!map.containsKey(key)) {
            if (commands.containsKey(key))
            // perform the system call and
            // populate the corresponding value
                map[key] = commands[key]!!.invoke()
            else
                throw AraraException("The requested key could not be " +
                        "translated into a command to get the call value.")
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
        try {
            // create a process result with the provided
            // command, capturing the output
            val result = ProcessExecutor(command.elements)
                    .readOutput(true).execute()

            // return the pair containing the exit status
            // and the output string as UTF-8
            return Pair(result.exitValue, result.outputUTF8())
        } catch (exception: Exception) {
            // quack, quack, do nothing, just
            // return a default error code

            // if something goes wrong, the default
            // error branch returns an exit status of
            // -99 and an empty string
            return Pair(-99, "")
        }
    }
}
