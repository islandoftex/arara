// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.files

import java.nio.file.Path

/**
 * A file in a [Project]. This layer is a [Path] providing a [FileType]
 * object to ensure arara is able to choose the right behavior dependent on the
 * file's type.
 */
data class ProjectFile(
        /**
         * The path to this file. If relative, it will be resolved against the
         * project's [Project.workingDirectory].
         */
        val path: Path,
        /**
         * The file's type. This will identify extension and search pattern.
         */
        val fileType: FileType
) : Path by path
