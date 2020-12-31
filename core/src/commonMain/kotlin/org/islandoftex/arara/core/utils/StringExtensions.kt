// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

/**
 * Format a string using [stringReplacements] to replace occurrences of
 * `%s` in the string. As such mostly compatible with the usual
 * `kotlin.text.format` function for strings.
 */
fun String.formatString(vararg stringReplacements: String): String {
    var final = this
    for (replacement in stringReplacements) {
        final = final.replaceFirst("%s", replacement)
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
 * per [his comment](https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns#comment108405664_17369948).
 *
 * @param pattern A glob pattern.
 * @return A regex to recognize the given glob pattern.
 */
internal fun String.globToRegex(): Regex =
    buildString(length) {
        var inGroup = 0
        var inClass = 0
        var firstIndexInClass = -1
        val arr = toCharArray()
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
    }.toRegex()
