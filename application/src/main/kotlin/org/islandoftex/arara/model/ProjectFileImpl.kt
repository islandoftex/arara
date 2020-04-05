// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import java.nio.file.Path
import org.islandoftex.arara.files.FileType
import org.islandoftex.arara.files.ProjectFile

/**
 * A file in a [Project]. This layer is a [Path] providing a [FileType]
 * object to ensure arara is able to choose the right behavior dependent on the
 * file's type.
 */
data class ProjectFileImpl(
    /**
     * The path to this file. If relative, it will be resolved against the
     * project's [Project.workingDirectory].
     */
    override val path: Path,
    /**
     * The file's type. This will identify extension and search pattern.
     */
    override val fileType: FileType,
    /**
     * Within a project, a file may have a priority. Files with highest
     * priority are compiled before files with lower priority. Files with
     * equal priority will be compiled in random order.
     */
    override val priority: Int = DEFAULT_PRIORITY
) : ProjectFile {
    companion object {
        /**
         * This value represents the default priority of a project file. It
         * should be used whenever it is irrelevant which priority a file has
         * (within a project).
         */
        const val DEFAULT_PRIORITY = 0
    }
}
