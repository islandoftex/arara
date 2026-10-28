// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.api.ui.ConsoleTasks
import org.islandoftex.arara.cli.utils.abbreviate

/** Focused plain presentation component. */
internal class PlainTaskRenderer(state: PlainRenderState) : PlainComponent(state), ConsoleTasks {
    /**
     * Displays the short version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildShortEntry(name: String, task: String) {
        val result = if (longestMatch >= outputWidth)
            shortenedLongestMatch
        else
            longestMatch
        val space = outputWidth - result - 1
        val line = "($name) $task ".abbreviate(space - "... ".length)
        print(line.padEnd(space, '.') + " ")
    }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    override fun printEntryResult(value: Boolean) {
        displayLine = false
        displayResult = true

        if (!isDryRunMode) {
            println(
                if (!isVerboseMode) {
                    getResult(value).padStart(longestMatch)
                } else {
                    "\n" + (" " + getResult(value))
                        .padStart(outputWidth, '-')
                }
            )
        }
    }

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    override fun printEntry(name: String, task: String) {
        displayLine = true
        displayResult = false
        if (!isDryRunMode) {
            if (!isVerboseMode) {
                buildShortEntry(name, task)
            } else {
                buildLongEntry(name, task)
            }
        } else {
            buildDryRunEntry(name, task)
        }
    }

    /**
     * Displays a long version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildLongEntry(name: String, task: String) {
        if (displayRolling) {
            println()
        } else {
            displayRolling = true
        }
        println(displaySeparator())
        println(
            "($name) $task"
                .abbreviate(outputWidth)
        )
        println(displaySeparator())
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    private fun buildDryRunEntry(name: String, task: String) {
        if (displayRolling) {
            println()
        } else {
            displayRolling = true
        }
        println("[DR] ($name) $task".abbreviate(outputWidth))
        println(displaySeparator())
    }
}
