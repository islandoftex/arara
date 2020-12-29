// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

/**
 * Format a string using [stringReplacements] to replace occurrences of
 * `%s` in the string. As such mostly compatible with the usual
 * `kotlin.text.format` function for strings.
 */
internal fun String.formatString(vararg stringReplacements: String): String {
    var final = this
    for (replacement in stringReplacements) {
        final = final.replaceFirst("%s", replacement)
    }
    return final
}
