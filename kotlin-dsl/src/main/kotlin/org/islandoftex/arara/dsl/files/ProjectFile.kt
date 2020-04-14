// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.files

import java.nio.file.Path
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.files.ProjectFile

class ProjectFile(
    path: Path,
    fileType: FileType,
    priority: Int = DEFAULT_PRIORITY
) : ProjectFile(path, fileType, priority) {
    internal var directives: List<String>? = null

    /**
     * Get the file's directives. This does not put them into any context,
     * just resolves the literal directives.
     *
     * @return The list of directives in the file, in order.
     */
    override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> {
        // TODO: proper implementation
        return listOf()
    }
}
