// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import java.util.regex.Matcher
import org.islandoftex.arara.api.AraraException

/**
 * Format a string using [stringReplacements] to replace occurrences of
 * `%s` in the string. As such mostly compatible with the usual
 * `kotlin.text.format` function for strings.
 *
 * The replacements are inserted verbatim — unlike the replacement
 * handling of the underlying regular expression engine, a `$` or a
 * backslash inside a value cannot corrupt the result.
 */
fun String.formatString(vararg stringReplacements: Any?): String {
    var final = this
    for (replacement in stringReplacements) {
        final = final.replaceFirst(
            "%s",
            Matcher.quoteReplacement(replacement.toString())
        )
    }
    return final
}

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
 * @return A regex to recognize the glob pattern held by the receiver.
 */
internal fun String.globToRegex(): Regex =
    buildString(length) {
        appendGlobPattern(this@globToRegex)
    }.let { pattern ->
        // a malformed glob (e.g. an unbalanced brace) would otherwise
        // surface as a raw pattern syntax exception deep inside a rule
        // evaluation
        runCatching { pattern.toRegex() }.getOrElse {
            throw AraraException(
                "The glob pattern '$this' could not be converted into a " +
                    "regular expression: ${it.message}"
            )
        }
    }

/**
 * Appends the regular expression equivalent of the glob pattern held by
 * the receiver to this builder.
 */
// the algorithm is a faithful, branchy port of the public-domain
// original; its control flow reflects the number of glob metacharacters
@Suppress("ComplexMethod", "NestedBlockDepth")
private fun StringBuilder.appendGlobPattern(glob: String) {
    var inGroup = 0
    var inClass = 0
    var firstIndexInClass = -1
    val arr = glob.toCharArray()
    var i = 0
    while (i < arr.size) {
        when (val ch = arr[i]) {
                '\\' -> {
                    i += 1
                    if (i >= arr.size) {
                        append('\\')
                    } else {
                        val next = arr[i]
                        when (next) {
                            ',' -> {
                                // escape not needed
                                // do nothing
                            }
                            'Q', 'E' ->
                                // extra escape needed
                                append("\\\\")
                            else ->
                                append('\\')
                        }
                        append(next)
                    }
                }
                '*' -> append(if (inClass == 0) ".*" else '*')
                '?' -> append(if (inClass == 0) '.' else '?')
                '[' -> {
                    inClass++
                    firstIndexInClass = i + 1
                    append('[')
                }
                ']' -> {
                    inClass--
                    append(']')
                }
                '.', '(', ')', '+', '|', '^', '$', '@', '%' -> {
                    if (inClass == 0 || (firstIndexInClass == i && ch == '^')) {
                        append('\\')
                    }
                    append(ch)
                }
                '!' -> append(if (firstIndexInClass == i) '^' else '!')
                '{' -> {
                    inGroup++
                    append('(')
                }
                '}' -> {
                    inGroup--
                    append(')')
                }
                ',' -> append(if (inGroup > 0) '|' else ',')
                else -> append(ch)
            }
            i++
        }
}
