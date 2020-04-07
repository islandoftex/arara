// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.nio.file.Path
import org.islandoftex.arara.api.rules.Directive

/**
 * A file in a [Project]. This layer is a [Path] providing a [FileType]
 * object to ensure arara is able to choose the right behavior dependent on the
 * file's type.
 */
interface ProjectFile {
    /**
     * The path to this file. If relative, it will be resolved against the
     * project's [Project.workingDirectory].
     */
    val path: Path

    /**
     * The file's type. This will identify extension and search pattern.
     */
    val fileType: FileType

    /**
     * Within a project, a file may have a priority. Files with highest
     * priority are compiled before files with lower priority. Files with
     * equal priority will be compiled in random order.
     */
    val priority: Int

    /**
     * Get the file's directives. This does not put them into any context,
     * just resolves the literal directives.
     *
     * @return The list of directives in the file, in order.
     */
    fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive>
}
