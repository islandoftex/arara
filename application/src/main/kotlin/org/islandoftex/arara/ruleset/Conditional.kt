// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

/**
 * The conditional class, it represents the type of conditional available
 * for a directive and its corresponding expression to be evaluated.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
data class Conditional(
    /**
     * The type of the condition indicates the meaning when evaluated.
     * Defaults to [ConditionalType.NONE].
     */
    val type: ConditionalType = ConditionalType.NONE,
    /**
     * The expression to be evaluated according to its type. Defaults
     * to no evaluation (empty string).
     */
    val condition: String = ""
) {
    /**
     * The types of conditionals arara is able to recognize.
     */
    enum class ConditionalType {
        /**
         * Evaluated beforehand, directive is interpreted if and only if the
         * result is true.
         */
        IF,
        /**
         * There is no evaluation, directive is interpreted, no extra effort is
         * needed.
         */
        NONE,
        /**
         * Evaluated beforehand, directive is interpreted if and only if the
         * result is false.
         */
        UNLESS,
        /**
         * Directive is interpreted the first time, then the evaluation is
         * done; while the result is false, the directive is interpreted again
         * and again.
         */
        UNTIL,
        /**
         * Evaluated beforehand, directive is interpreted if and only if the
         * result is true, and the process is repeated while the result still
         * holds true.
         */
        WHILE
    }

    /**
     * Provides a textual representation of the conditional object.
     * @return A string representation of this object.
     */
    override fun toString(): String {
        return "{ $type" +
                if (type != ConditionalType.NONE)
                    ", expression: ${condition.trim()}"
                else "" + " }"
    }
}
