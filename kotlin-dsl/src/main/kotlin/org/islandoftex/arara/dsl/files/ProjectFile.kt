// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.files

import java.nio.file.Path
import kotlin.time.ExperimentalTime
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.files.ProjectFile

/**
 * Implementation of a project file with the option to specify directives
 * explicitly and avoid fetching.
 */
internal class ProjectFile(
    path: Path,
    fileType: FileType,
    priority: Int = DEFAULT_PRIORITY
) : ProjectFile(path, fileType, priority) {
    /**
     * The directives to be used instead of fetching them from file. Used
     * whenever non-null.
     */
    internal var directives: List<String>? = null

    /**
     * Get the file's directives. This does not put them into any context,
     * just resolves the literal directives.
     *
     * @return The list of directives in the file, in order.
     */
    @ExperimentalTime
    override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> {
        // TODO: proper implementation
        return listOf()
    }
}
