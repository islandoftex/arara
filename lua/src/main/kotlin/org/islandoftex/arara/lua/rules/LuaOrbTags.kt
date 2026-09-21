// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaError
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Interpolate Lua orb tags in directive options when the Lua rule
 * engine is selected, mirroring the MVEL template evaluation.
 *
 * An orb tag has the form `@{ expression }`; everything outside the tags
 * is kept as literal text, and every expression is evaluated as Lua in
 * the directive conditional environment (see
 * [LuaConditionalEnvironment]). A `}` inside a quoted string does not
 * terminate the tag.
 */
object LuaOrbTags {
    /**
     * Interpolate all orb tags in the provided template.
     *
     * @param template The template string.
     * @return The interpolated string.
     * @throws AraraException An expression could not be evaluated.
     */
    @Throws(AraraException::class)
    fun interpolate(template: String): String {
        val result = StringBuilder()
        var index = 0
        while (index < template.length) {
            val start = template.indexOf("@{", index)
            if (start < 0) {
                result.append(template.substring(index))
                return result.toString()
            }
            result.append(template.substring(index, start))
            val end = findTagEnd(template, start + 2)
                ?: throw AraraException(
                    LanguageController.messages
                        .ERROR_EXTRACTOR_INTERPOLATION_FAILURE,
                    AraraException(
                        "The orb tag starting at position $start is " +
                            "not terminated."
                    )
                )
            val expression = template.substring(start + 2, end)
            result.append(evaluate(expression))
            index = end + 1
        }
        return result.toString()
    }

    /**
     * Find the closing brace of the orb tag starting after [from],
     * ignoring braces inside quoted strings.
     */
    private fun findTagEnd(template: String, from: Int): Int? {
        var quote: Char? = null
        var index = from
        while (index < template.length) {
            val character = template[index]
            when {
                quote != null ->
                    if (character == quote)
                        quote = null
                character == '\'' || character == '"' ->
                    quote = character
                character == '}' ->
                    return index
            }
            index++
        }
        return null
    }

    /**
     * Evaluate a single orb tag expression as Lua and return its string
     * representation; nil results interpolate as empty strings.
     */
    @Throws(AraraException::class)
    private fun evaluate(expression: String): String =
        try {
            val result = LuaConditionalEnvironment.newGlobals()
                .load("return (" + expression + ")")
                .call()
            // a nil result interpolates as an empty string, matching the
            // string representation the MVEL template engine splices in
            if (result.isnil()) "" else result.tojstring()
        } catch (error: LuaError) {
            throw AraraException(
                LanguageController.messages
                    .ERROR_EXTRACTOR_INTERPOLATION_FAILURE,
                error
            )
        }
}
