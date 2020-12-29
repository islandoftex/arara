// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.formatString

/**
 * Handle user and evaluation input in directives and rules.
 */
object InputHandling {
    /**
     * Checks if the input string is equal to a valid boolean value.
     *
     * @param value The input string.
     * @return A boolean value represented by the provided string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkBoolean(value: String): Boolean {
        val yes = listOf("yes", "true", "1", "on")
        val no = listOf("no", "false", "0", "off")
        return if (!yes.union(no).contains(value.toLowerCase())) {
            throw AraraException(
                    LanguageController.messages.ERROR_CHECKBOOLEAN_NOT_VALID_BOOLEAN
                            .formatString(value)
            )
        } else {
            yes.contains(value.toLowerCase())
        }
    }

    /**
     * Flattens a potential list of lists into a list of objects.
     *
     * @param list The list to be flattened.
     * @return The flattened list.
     */
    // TODO: check nullity
    fun flatten(list: List<*>): List<Any> =
        list.flatMap { item ->
            if (item is List<*>)
                flatten(item)
            else
                listOf(item as Any)
        }
}
