// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Converts a standard POSIX Shell globbing pattern into a regular expression
 * pattern. The result can be used with the standard [Regex] API to recognize
 * strings which match the glob pattern.
 *
 * See also, the POSIX Shell language:
 * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
 *
 * Code by [Neil Traft](https://stackoverflow.com/users/213525/neil-traft)
 * (see https://stackoverflow.com/a/17369948, released into public domain as
 * per [his comment](https://stackoverflow.com/questions/1247772/is-there-an-
 * equivalent-of-java-util-regex-for-glob-type-patterns#comment108405664_17369948).
 *
 * @receiver The glob pattern to convert.
 * @return A regex to recognize the given glob pattern.
 */

internal fun String.globToRegex(): Regex {
    val pattern = GlobPattern(this).render()
    return try {
        pattern.toRegex()
    } catch (exception: IllegalArgumentException) {
        throw AraraException(
            LanguageController.messages.errorGlobPatternInvalid(this, exception.message.orEmpty()), exception
        )
    }
}

private class GlobPattern(private val source: String) {
    private val output = StringBuilder(source.length)
    private var groupDepth = 0
    private var classDepth = 0
    private var firstClassIndex = -1
    private var index = 0

    fun render(): String {
        while (index < source.length) {
            append(source[index])
            index++
        }
        return output.toString()
    }

    private fun append(character: Char) {
        when (character) {
            '\\' -> escape()
            '*' -> output.append(if (classDepth == 0) ".*" else '*')
            '?' -> output.append(if (classDepth == 0) '.' else '?')
            '[' -> {
                classDepth++
                firstClassIndex = index + 1
                output.append('[')
            }
            ']' -> {
                classDepth--
                output.append(']')
            }
            else -> appendGroupOrLiteral(character)
        }
    }

    private fun appendGroupOrLiteral(character: Char) {
        when (character) {
            '.', '(', ')', '+', '|', '^', '$', '@', '%' -> {
                if (classDepth == 0 || (firstClassIndex == index && character == '^')) output.append('\\')
                output.append(character)
            }
            '!' -> output.append(if (firstClassIndex == index) '^' else '!')
            '{' -> {
                groupDepth++
                output.append('(')
            }
            '}' -> {
                groupDepth--
                output.append(')')
            }
            ',' -> output.append(if (groupDepth > 0) '|' else ',')
            else -> output.append(character)
        }
    }

    private fun escape() {
        index++
        if (index >= source.length) {
            output.append('\\')
        } else {
            val next = source[index]
            when (next) {
                ',' -> Unit
                'Q', 'E' -> output.append("\\\\")
                else -> output.append('\\')
            }
            output.append(next)
        }
    }
}
