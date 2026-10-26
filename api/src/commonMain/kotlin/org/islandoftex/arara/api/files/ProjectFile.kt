// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import org.islandoftex.arara.api.rules.Directive

/**
 * A file in a [Project]. This layer is a [MPPPath] providing a [FileType]
 * object to ensure arara is able to choose the right behavior dependent on the
 * file's type.
 */
public interface ProjectFile {
    /**
     * The execution path, pointing to a staged copy when a build directory is used.
     * Project loaders resolve source file names against [Project.workingDirectory].
     */
    public val path: MPPPath

    /** Original input identity, retained when [path] points into a build workspace. */
    public val sourcePath: MPPPath get() = path

    /**
     * The file's type. This will identify extension and search pattern.
     */
    public val fileType: FileType

    /**
     * Within a project, a file may have a priority. Files with highest
     * priority are compiled before files with lower priority. Files with
     * equal priority are ordered by normalized path by the default executor.
     */
    public val priority: Int

    /**
     * Get the file's directives. This does not put them into any context,
     * just resolves the literal directives.
     *
     * @param parseOnlyHeader Stop when a line no longer matches the file's comment pattern.
     * @return The list of directives in the file, in order.
     */
    public fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive>
}
