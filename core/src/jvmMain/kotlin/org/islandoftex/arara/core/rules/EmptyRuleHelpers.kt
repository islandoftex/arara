// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

/** Empty-value predicates shared by rule interpreters. */
object EmptyRuleHelpers {
    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    fun isEmpty(string: String): Boolean = string.isEmpty()

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    fun isNotEmpty(string: String): Boolean = !isEmpty(string)

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    fun isEmpty(string: String, yes: Any?, no: Any? = ""): Any? =
        if (isEmpty(string)) yes else no

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    fun isNotEmpty(string: String, yes: Any?, no: Any? = ""): Any? =
        if (isNotEmpty(string)) yes else no
}
