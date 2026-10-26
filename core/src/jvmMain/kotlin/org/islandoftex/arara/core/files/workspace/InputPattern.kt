// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import org.eclipse.jgit.ignore.FastIgnoreRule
import org.islandoftex.arara.api.AraraException

/** Root-relative, full-path matching through JGit's maintained wildcard implementation. */
internal class InputPattern(pattern: String) {
    private val rule = FastIgnoreRule("/" + pattern).also {
        if (it.isEmpty) throw AraraException(messages.errorBuildInputInvalid(pattern))
    }

    fun matches(path: String): Boolean = rule.isMatch(path, false, true)
}

internal fun selectionPattern(selection: String): String {
    val pattern = selection.replace('\\', '/').removePrefix("./")
    val absolute = pattern.startsWith('/') || Regex("^[a-zA-Z]:").containsMatchIn(pattern)
    if (absolute || pattern.split('/').any { it == ".." }) {
        throw AraraException(messages.errorBuildInputInvalid(pattern))
    }
    return pattern
}
