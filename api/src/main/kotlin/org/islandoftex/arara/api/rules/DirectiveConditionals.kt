// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * The conditional class, it represents the type of conditional available
 * for a directive and its corresponding expression to be evaluated.
 */
interface DirectiveConditional {
    /**
     * The type of the condition indicates the meaning when evaluated.
     * Defaults to [DirectiveConditionalType.NONE].
     */
    val type: DirectiveConditionalType

    /**
     * The expression to be evaluated according to its type. Defaults
     * to no evaluation (empty string).
     */
    val condition: String
}

/**
 * The types of conditionals arara is able to recognize.
 */
enum class DirectiveConditionalType {
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
