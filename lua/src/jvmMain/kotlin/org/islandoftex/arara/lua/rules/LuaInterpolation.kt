// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Interpolates Lua expressions in `{{ ... }}` tags, preserving surrounding text.
 * Tags support nested tables, quoted strings, long strings and comments. A nil
 * result becomes empty text; MVEL's `@{ ... }` tags are rejected.
 */
object LuaInterpolation {
    /** Evaluate each Lua interpolation tag in fresh conditional globals; preserve the surrounding text. */
    fun interpolate(template: String): String =
        LuaConversion.withLuaErrorHandling(LanguageController.messages.errorLuaInterpolationFailure) {
            if ("@{" in template) fail(LanguageController.messages.errorYamlTagsInLua)
            val output = StringBuilder()
            var index = 0
            while (index < template.length) {
                val start = template.indexOf("{{", index)
                if (start < 0) {
                    output.append(template.substring(index))
                    break
                }
                output.append(template.substring(index, start))
                val end = tagEnd(template, start + 2)
                val expression = template.substring(start + 2, end)
                val value = LuaConditionalEnvironment.newGlobals()
                    .load("return (" + expression + "\n)", "directive-options").call()
                if (!value.isnil()) output.append(value.tojstring())
                index = end + 2
            }
            output.toString()
        }

    private fun tagEnd(text: String, from: Int): Int {
        var index = from
        var depth = 0
        while (index < text.length) {
            val character = text[index]
            val longEnd = longStringEnd(text, index)
            when {
                longEnd != null -> index = longEnd
                text.startsWith("--", index) -> {
                    val commentEnd = longStringEnd(text, index + 2)
                    index = commentEnd ?: text.indexOf('\n', index).takeIf { it >= 0 }
                        ?: fail(LanguageController.messages.errorLuaUnterminatedTag)
                }
                character == '\'' || character == '"' -> index = quotedEnd(text, index)
                character == '{' -> { depth++; index++ }
                character == '}' && depth > 0 -> { depth--; index++ }
                text.startsWith("}}", index) -> return index
                else -> index++
            }
        }
        fail(LanguageController.messages.errorLuaUnterminatedTag)
    }

    private fun quotedEnd(text: String, start: Int): Int {
        var index = start + 1
        while (index < text.length) {
            when (text[index]) {
                '\\' -> index += 2
                text[start] -> return index + 1
                else -> index++
            }
        }
        fail(LanguageController.messages.errorLuaUnterminatedQuote)
    }

    private fun longStringEnd(text: String, start: Int): Int? {
        val opening = Regex("\\[(=*)\\[").find(text, start)?.takeIf { it.range.first == start } ?: return null
        val closing = "]" + opening.groupValues[1] + "]"
        val end = text.indexOf(closing, opening.range.last + 1)
        if (end < 0) fail(LanguageController.messages.errorLuaUnterminatedLongString)
        return end + closing.length
    }

    private fun fail(message: String): Nothing = throw LuaHelperException(message)
}
