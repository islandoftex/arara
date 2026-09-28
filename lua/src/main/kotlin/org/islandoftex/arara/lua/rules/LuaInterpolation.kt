// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaError
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Interpolate the tags of the Lua engine in directive options.
 *
 * A tag has the form `{{ expression }}`; everything outside the tags is
 * kept as literal text, and every expression is evaluated as Lua in
 * the directive conditional environment (see
 * [LuaConditionalEnvironment]). The delimiters belong to the Lua engine
 * alone: the orb tags of the deprecated YAML engine are a different
 * syntax for a different engine, so meeting one here is reported
 * instead of being silently kept as literal text or misread. A closing
 * delimiter only matches at zero brace depth, so expressions may
 * contain braces of their own, such as table constructors; braces
 * inside quoted strings never count either way.
 */
object LuaInterpolation {
    /**
     * Interpolate all tags in the provided template.
     *
     * @param template The template string.
     * @return The interpolated string.
     * @throws AraraException An expression could not be evaluated, or
     * the template uses the other engine's tag syntax.
     */
    @Throws(AraraException::class)
    fun interpolate(template: String): String {
        // the orb tag is never literal text in arara, so under this
        // engine it can only mean the wrong engine's syntax was used
        if ("@{" in template) {
            throw AraraException(
                LanguageController.messages
                    .errorExtractorOrbTagInLuaEngine
            )
        }
        val result = StringBuilder()
        var index = 0
        while (index < template.length) {
            val start = template.indexOf("{{", index)
            if (start < 0) {
                result.append(template.substring(index))
                return result.toString()
            }
            result.append(template.substring(index, start))
            val end = findTagEnd(template, start + 2)
                ?: throw AraraException(
                    LanguageController.messages
                        .errorExtractorInterpolationFailure,
                    AraraException(
                        "The interpolation tag starting at position " +
                            "$start is not terminated."
                    )
                )
            val expression = template.substring(start + 2, end)
            result.append(evaluate(expression))
            index = end + 2
        }
        return result.toString()
    }

    /**
     * Find the closing delimiter of the tag starting after [from]:
     * the first `}}` outside quoted strings and outside nested braces.
     * Returns the index of its first brace.
     */
    private fun findTagEnd(template: String, from: Int): Int? {
        var quote: Char? = null
        var depth = 0
        var index = from
        while (index < template.length) {
            val character = template[index]
            when {
                quote != null ->
                    if (character == quote)
                        quote = null
                character == '\'' || character == '"' ->
                    quote = character
                character == '{' -> depth++
                character == '}' ->
                    // a lone closing brace at zero depth is not a
                    // delimiter; it stays part of the expression, and
                    // the Lua evaluation rejects the unbalanced script
                    if (depth > 0) {
                        depth--
                    } else if (
                        index + 1 < template.length &&
                        template[index + 1] == '}'
                    ) {
                        return index
                    }
            }
            index++
        }
        return null
    }

    /**
     * Evaluate a single tag expression as Lua and return its string
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
                    .errorExtractorInterpolationFailure,
                error
            )
        }
}
