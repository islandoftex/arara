// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor

/** Command construction and guarded process execution for rule interpreters. */
object CommandRuleHelpers {
    /**
     * Gets the command based on a list of strings.
     *
     * @param elements The list of strings.
     * @return A command.
     */
    fun getCommand(elements: List<String>): Command =
        org.islandoftex.arara.core.session.Command(
            elements,
            LinearExecutor.currentProject!!.executionDirectory
        )

    /**
     * Gets the command based on an array of objects.
     *
     * @param elements Array of objects.
     * @return A command.
     */
    fun commandFromValues(elements: List<Any?>): Command =
        getCommand(
            InputHandling.flatten(elements.filterNotNull())
                .map { it.toString() }
                .filter { it.isNotEmpty() }
        )

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as string.
     *
     * @param path String path representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    fun commandFromValues(
        path: String,
        elements: List<Any?>
    ): Command = org.islandoftex.arara.core.session.Command(
        InputHandling.flatten(elements.filterNotNull())
            .map { it.toString() }
            .filter { it.isNotEmpty() },
        MPPPath(path)
    )

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as file.
     *
     * @param file File representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    fun commandFromValues(
        file: MPPPath,
        elements: List<Any?>
    ): Command = org.islandoftex.arara.core.session.Command(
        InputHandling.flatten(elements.filterNotNull())
            .map { it.toString() }
            .filter { it.isNotEmpty() },
        file
    )

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as string.
     *
     * @param path String path representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    fun getCommandWithWorkingDirectory(
        path: String,
        elements: List<String>
    ): Command = org.islandoftex.arara.core.session.Command(
        elements, MPPPath(path)
    )

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as file.
     *
     * @param file File representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    fun getCommandWithWorkingDirectory(
        file: MPPPath,
        elements: List<String>
    ): Command = org.islandoftex.arara.core.session.Command(elements, file)

    /**
     * Unsafely executes a system command from the underlying operating system
     * and returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @return A pair containing the exit status and the system command output
     * as a string.
     */
    fun unsafelyExecuteSystemCommand(command: Command): Pair<Int, String> =
        if (LinearExecutor.executionOptions.executionMode == ExecutionMode.SAFE_RUN)
            throw SafeRunViolationException(
                LanguageController.messages.errorSafeModeSystemCommand
            )
        else
            Environment.executeSystemCommand(command)

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    fun isOnPath(command: String): Boolean = Environment.isOnPath(command)
}
