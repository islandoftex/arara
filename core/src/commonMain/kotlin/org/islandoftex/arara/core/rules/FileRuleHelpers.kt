// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.MPPPath

/** Text file reads and writes for rule interpreters. */
object FileRuleHelpers {
    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param file The file.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(file: MPPPath, text: String, append: Boolean): Boolean = try {
        file.writeText(text, append)
        true
    } catch (_: AraraIOException) {
        false
    }

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param path The path.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(path: String, text: String, append: Boolean): Boolean =
        writeToFile(MPPPath(path), text, append)

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param file The file.
     * @param lines The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(file: MPPPath, lines: List<String>, append: Boolean): Boolean =
        writeToFile(file, lines.joinToString(platformLineSeparator), append)

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param path The path.
     * @param lines The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(path: String, lines: List<String>, append: Boolean): Boolean =
        writeToFile(MPPPath(path), lines.joinToString(platformLineSeparator), append)

    /**
     * Reads the provided file into a list of strings.
     *
     * @param file The file.
     * @return A list of strings.
     */
    fun readFromFile(file: MPPPath): List<String> = try {
        file.readLines()
    } catch (_: AraraIOException) {
        emptyList()
    }

    /**
     * Reads the provided file into a list of strings.
     *
     * @param path The path.
     * @return A list of strings.
     */
    fun readFromFile(path: String): List<String> = readFromFile(MPPPath(path))
}
