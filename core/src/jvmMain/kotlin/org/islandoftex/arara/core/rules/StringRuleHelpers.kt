// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.util.MissingFormatArgumentException
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/** String construction and transformations for rule interpreters. */
object StringRuleHelpers {
    /**
     * Trim spaces from the string.
     *
     * @param string The string.
     * @return A trimmed string.
     */
    fun trimSpaces(string: String): String = string.trim()

    /**
     * Attempts to retrieve an index-based element from a list of strings.
     *
     * @param list The list.
     * @param index The index.
     * @return The element in the position defined by the provided index or a
     * null value if the index is out of bounds.
     *
     */
    fun getOrNull(list: List<String>, index: Int = 0) = list.getOrNull(index)

    /**
     * Replicates the pattern to each element of a list.
     *
     * @param pattern The pattern.
     * @param values The list.
     * @return A list of strings containing the pattern applied to the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun replicatePattern(pattern: String, values: List<Any>): List<Any> = try {
        values.map { pattern.format(it) }
    } catch (exception: MissingFormatArgumentException) {
        throw AraraException(
            LanguageController.messages.errorReplicateListMissingFormatArgumentsException,
            exception
        )
    }

    /**
     * Build a string based on an array of objects.
     *
     * @param objects Array of objects.
     * @return A string built from the array.
     */
    fun buildString(objects: List<Any?>): String =
        objects.filterNotNull().map { it.toString() }
            .filter { it.isNotEmpty() }.joinToString(" ")
}
