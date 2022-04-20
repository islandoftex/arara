// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives

/**
 * Implementation of a project file with the option to specify directives
 * explicitly and avoid fetching.
 */
internal class LuaProjectFile(
    path: MPPPath,
    fileType: FileType,
    priority: Int = DEFAULT_PRIORITY
) : ProjectFile(path, fileType, priority) {
    /**
     * The directives to be used instead of fetching them from file. Used
     * whenever non-null.
     */
    var directives: List<String>? = null

    /**
     * Get the file's directives. This does not put them into any context,
     * just resolves the literal directives.
     *
     * @return The list of directives in the file, in order.
     */
    override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> {
        // TODO: exception handling
        return directives?.let {
            Directives.extractDirectives(it, true, fileType)
        } ?: super.fetchDirectives(parseOnlyHeader)
    }
}
