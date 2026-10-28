// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException

/** Boolean conversion shared by rule interpreters. */
object BooleanRuleHelpers {
    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String): Boolean =
        !EmptyRuleHelpers.isEmpty(string) && InputHandling.checkBoolean(string)

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String): Boolean =
        !EmptyRuleHelpers.isEmpty(string) && !InputHandling.checkBoolean(string)

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any?, no: Any? = ""): Any? =
        if (isTrue(string)) yes else no

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any?, no: Any? = ""): Any? =
        if (isFalse(string)) yes else no

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any?, no: Any?, fallback: Any?): Any? =
        if (EmptyRuleHelpers.isEmpty(string)) fallback else if (isTrue(string)) yes else no

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any?, no: Any?, fallback: Any?): Any? =
        if (EmptyRuleHelpers.isEmpty(string)) fallback else if (isFalse(string)) yes else no

    /**
     * Checks if the expression resolves to true.
     *
     * @param value The expression.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    fun isTrue(value: Boolean, yes: Any?, no: Any? = ""): Any? =
        if (value) yes else no

    /**
     * Checks if the expression resolves to false.
     *
     * @param value The expression.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    fun isFalse(value: Boolean, yes: Any?, no: Any? = ""): Any? =
        if (!value) yes else no
}
