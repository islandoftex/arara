// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.ruleset

/**
 * The conditional class, it represents the type of conditional available
 * for a directive and its corresponding expression to be evaluated.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
data class Conditional(
        // the conditional type, specified above; the
        // default fallback, as seen in the constructor,
        // is set to NONE, that is, no conditional at all
        val type: ConditionalType = ConditionalType.NONE,
        // the expression to be evaluated according to its
        // type; the default fallback, as seen in the
        // constructor, is set to an empty string
        var condition: String = "") {

    // these are all types of conditionals arara
    // is able to recognize; personally, I believe
    // they are more than sufficient to cover the
    // majority of test cases
    enum class ConditionalType {
        // evaluated beforehand, directive is interpreted
        // if and only if the result is true
        IF,

        // there is no evaluation, directive is interpreted,
        // no extra effort is needed
        NONE,

        // evaluated beforehand, directive is interpreted
        // if and only if the result is false
        UNLESS,

        // directive is interpreted the first time, then the
        // evaluation is done; while the result is false,
        // the directive is interpreted again and again
        UNTIL,

        // evaluated beforehand, directive is interpreted if
        // and oly if the result is true, and the process is
        // repeated while the result still holds true
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
