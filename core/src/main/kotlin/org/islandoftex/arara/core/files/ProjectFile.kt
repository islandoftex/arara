// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.ProjectFile

data class ProjectFile(
    override val path: Path,
    override val fileType: FileType,
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
